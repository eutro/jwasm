package io.github.eutro.jwasm2java;

import io.github.eutro.jwasm.ModuleVisitor;
import io.github.eutro.jwasm.tree.*;
import io.github.eutro.jwasm.tree.ModuleNode;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.tree.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.eutro.jwasm.Opcodes.*;
import static org.objectweb.asm.Opcodes.*;

public class ModuleAdapter extends ModuleVisitor {
    final ModuleNode node;

    public ModuleAdapter(ModuleNode node) {
        super(node);
        this.node = node;
    }

    public ModuleAdapter() {
        this(new ModuleNode());
    }

    private TypeNode getType(int index) {
        return Objects.requireNonNull(Objects.requireNonNull(node.types).types).get(index);
    }

    public ClassNode toJava(String internalName) {
        ClassNode cn = new ClassNode();
        cn.visit(V1_8,
                ACC_PUBLIC | ACC_SUPER,
                internalName,
                null,
                Type.getInternalName(Object.class),
                null);
        cn.methods = new ArrayList<>();
        if (node.funcs != null) {
            for (FuncNode func : node.funcs) {
                MethodNode mn = new MethodNode();
                mn.access = ACC_PRIVATE;
                mn.desc = Types.methodDesc(getType(func.type)).toString();
                mn.name = "func" + cn.methods.size();
                cn.methods.add(mn);
            }
        }
        List<FieldNode> mems = new ArrayList<>();
        if (node.mems != null) {
            for (MemoryNode ignored : node.mems) {
                FieldNode fn = new FieldNode(ACC_PRIVATE,
                        "mem" + mems.size(),
                        Type.getType(ByteBuffer.class).toString(),
                        null,
                        null);
                mems.add(fn);
                cn.fields.add(fn);
            }
        }
        List<FieldNode> globals = new ArrayList<>();
        if (node.globals != null) {
            for (GlobalNode global : node.globals) {
                FieldNode fn = new FieldNode(ACC_PRIVATE,
                        "glob" + globals.size(),
                        Types.toJava(global.type.type).getDescriptor(),
                        null,
                        null);
                if (global.type.mut == MUT_CONST) fn.access |= ACC_FINAL;
                globals.add(fn);
                cn.fields.add(fn);
            }
        }
        if (node.exports != null) {
            for (ExportNode export : node.exports) {
                if (export.type == EXPORTS_FUNC) {
                    MethodNode method = cn.methods.get(export.index);
                    method.name = export.name;
                    method.access &= ~ACC_PRIVATE;
                    method.access |= ACC_PUBLIC;
                }
            }
        }
        if (node.codes != null) {
            int ci = 0;
            for (CodeNode code : node.codes) {
                MethodNode method = cn.methods.get(ci);
                InstructionAdapter ia = new InstructionAdapter(method);
                method.localVariables = new ArrayList<>();
                LabelNode start = new LabelNode();
                LabelNode end = new LabelNode();

                TypeNode funcType = getType(node.funcs.funcs.get(ci).type);

                int[] indeces = new int[funcType.params.length + code.locals.length];
                Type[] types = new Type[indeces.length];
                int localIndex = 1;
                method.parameters = new ArrayList<>();
                for (int ai = 0; ai < funcType.params.length; ai++) {
                    Type localType = types[ai] = Types.toJava(funcType.params[ai]);
                    indeces[ai] = localIndex;
                    method.parameters.add(new ParameterNode("arg" + ai, 0));
                    localIndex += localType.getSize();
                }

                method.instructions.add(start);
                for (int li = 0; li < code.locals.length; li++) {
                    byte local = code.locals[li];
                    Type localType = types[funcType.params.length + li] = Types.toJava(local);
                    indeces[funcType.params.length + li] = localIndex;
                    method.localVariables.add(new LocalVariableNode(
                            "loc" + li,
                            localType.getDescriptor(),
                            null,
                            start,
                            end,
                            localIndex
                    ));
                    defaultValue(ia, local);
                    method.visitVarInsn(localType.getOpcode(ISTORE), localIndex);
                    localIndex += localType.getSize();
                }
                new ExprAdapter(code.expr, method, internalName, indeces, types).translateInto();
                method.instructions.add(end);
                doReturn(method, funcType.returns);
                ++ci;
            }
        }
        { // ctor
            MethodNode mn = new MethodNode();
            mn.access = ACC_PUBLIC;
            mn.name = "<init>";
            mn.desc = "()V";
            mn.visitVarInsn(ALOAD, 0);
            mn.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            if (node.mems != null) {
                int i = 0;
                for (MemoryNode mem : node.mems) {
                    mn.visitVarInsn(ALOAD, 0);
                    mn.visitLdcInsn(mem.limits.min * PAGE_SIZE);
                    mn.visitMethodInsn(INVOKESTATIC,
                            "java/nio/ByteBuffer",
                            "allocate",
                            Type.getMethodDescriptor(Type.getType(ByteBuffer.class), Type.INT_TYPE),
                            false);
                    mn.visitFieldInsn(GETSTATIC,
                            Type.getInternalName(ByteOrder.class),
                            "LITTLE_ENDIAN",
                            Type.getDescriptor(ByteOrder.class));
                    mn.visitMethodInsn(INVOKEVIRTUAL,
                            "java/nio/ByteBuffer",
                            "order",
                            Type.getMethodDescriptor(Type.getType(ByteBuffer.class), Type.getType(ByteOrder.class)),
                            false);
                    mn.visitFieldInsn(PUTFIELD, internalName, "mem" + i, Type.getDescriptor(ByteBuffer.class));
                    ++i;
                }
            }
            if (node.globals != null) {
                int i = 0;
                for (GlobalNode global : node.globals) {
                    if (global.init != null) {
                        mn.visitVarInsn(ALOAD, 0);
                        new ExprAdapter(global.init, mn, internalName, new int[0], new Type[0]).translateInto();
                        FieldNode field = globals.get(i);
                        mn.visitFieldInsn(PUTFIELD, internalName, field.name, field.desc);
                    }
                    ++i;
                }
            }
            mn.visitInsn(Opcodes.RETURN);
            cn.methods.add(mn);
        }
        return cn;
    }

    private void defaultValue(InstructionAdapter ia, byte local) {
        switch (local) {
            case I32:
                ia.iconst(0);
                break;
            case I64:
                ia.lconst(0);
                break;
            case F32:
                ia.fconst(0);
                break;
            case F64:
                ia.dconst(0);
                break;
            case FUNCREF:
            case EXTERNREF:
                ia.aconst(null);
                break;
        }
    }

    private void doReturn(MethodNode method, byte[] returns) {
        if (returns.length == 2) throw new UnsupportedOperationException();
        method.visitInsn(Types.returnType(returns).getOpcode(IRETURN));
    }
}
