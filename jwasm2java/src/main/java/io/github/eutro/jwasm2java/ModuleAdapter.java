package io.github.eutro.jwasm2java;

import io.github.eutro.jwasm.ModuleVisitor;
import io.github.eutro.jwasm.tree.ModuleNode;
import io.github.eutro.jwasm.tree.*;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.tree.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.eutro.jwasm.Opcodes.*;
import static org.objectweb.asm.Opcodes.*;

public class ModuleAdapter extends ModuleVisitor {
    public @NotNull ModuleNode node;

    public ModuleAdapter(@NotNull ModuleNode node) {
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
        cn.version = V1_8;
        cn.access = ACC_PUBLIC | ACC_SUPER;
        cn.name = internalName;
        cn.superName = "java/lang/Object";

        Externs externs = new Externs();

        // region Exportables
        List<MethodNode> funcs = new ArrayList<>();
        List<FieldNode> mems = new ArrayList<>();
        List<FieldNode> globals = new ArrayList<>();
        List<FieldNode> tables = new ArrayList<>();
        if (node.funcs != null) {
            for (FuncNode func : node.funcs) {
                MethodNode mn = new MethodNode();
                mn.access = ACC_PRIVATE;
                TypeNode funcType = getType(func.type);
                mn.desc = Types.methodDesc(funcType).toString();
                mn.name = "func" + cn.methods.size();
                funcs.add(mn);
                cn.methods.add(mn);
                externs.funcs.add(new FuncExtern.ModuleFuncExtern(cn, mn, funcType));
            }
        }
        if (node.mems != null) {
            for (MemoryNode ignored : node.mems) {
                FieldNode fn = new FieldNode(ACC_PRIVATE,
                        "mem" + mems.size(),
                        Type.getType(ByteBuffer.class).toString(),
                        null,
                        null);
                mems.add(fn);
                cn.fields.add(fn);
                externs.mems.add(new Extern.ModuleFieldExtern(fn, internalName));
            }
        }
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
                externs.globals.add(new Extern.ModuleFieldExtern(fn, internalName));
            }
        }
        if (node.tables != null) {
            for (TableNode table : node.tables) {
                FieldNode fn = new FieldNode(ACC_PRIVATE,
                        "table" + tables.size(),
                        "[" + Types.toJava(table.type).getDescriptor(),
                        null,
                        null);
                tables.add(fn);
                cn.fields.add(fn);
                externs.tables.add(new TableExtern.ModuleTableExtern(fn, internalName, table.type));
            }
        }
        if (node.exports != null) {
            for (ExportNode export : node.exports) {
                switch (export.type) {
                    case EXPORTS_FUNC:
                        MethodNode method = funcs.get(export.index);
                        method.name = export.name;
                        method.access &= ~ACC_PRIVATE;
                        method.access |= ACC_PUBLIC;
                        break;
                    case EXPORTS_TABLE:
                        FieldNode table = tables.get(export.index);
                        table.name = export.name;
                        table.access &= ~ACC_PRIVATE;
                        table.access |= ACC_PUBLIC;
                        break;
                    case EXPORTS_MEM:
                        FieldNode mem = mems.get(export.index);
                        mem.name = export.name;
                        mem.access &= ~ACC_PRIVATE;
                        mem.access |= ACC_PUBLIC;
                        break;
                    case EXPORTS_GLOBAL:
                        FieldNode global = globals.get(export.index);
                        global.name = export.name;
                        global.access &= ~ACC_PRIVATE;
                        global.access |= ACC_PUBLIC;
                        break;
                }
            }
        }
        // endregion

        // region Imports
        if (node.imports != null) {
            //noinspection StatementWithEmptyBody
            for (AbstractImportNode ignored : node.imports) {
                // TODO
            }
        }
        // endregion

        // region Constructor
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
                        "(I)Ljava/nio/ByteBuffer;",
                        false);
                mn.visitFieldInsn(GETSTATIC,
                        "java/nio/ByteOrder",
                        "LITTLE_ENDIAN",
                        "Ljava/nio/ByteOrder;");
                mn.visitMethodInsn(INVOKEVIRTUAL,
                        "java/nio/ByteBuffer",
                        "order",
                        "(Ljava/nio/ByteOrder;)Ljava/nio/ByteBuffer;",
                        false);
                mn.visitFieldInsn(PUTFIELD, internalName, mems.get(i).name, Type.getDescriptor(ByteBuffer.class));
                ++i;
            }
        }

        if (node.start != null) {
            mn.visitVarInsn(ALOAD, 0);
            MethodNode start = funcs.get(node.start.func);
            if (!"()V".equals(start.desc)) throw new IllegalArgumentException();
            mn.visitMethodInsn(INVOKEVIRTUAL, internalName, start.name, start.desc, false);
        }
        mn.visitInsn(Opcodes.RETURN);
        cn.methods.add(mn);
        // endregion

        // region Method Bodies
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
                Context ctx = new Context(internalName,
                        method,
                        method.access,
                        method.name,
                        method.desc,
                        Objects.requireNonNull(node.types).types,
                        funcType,
                        externs,
                        indeces,
                        types);
                ExprAdapter.translateInto(code.expr, ctx);
                method.instructions.add(end);
                ctx.compress(funcType.returns);
                ctx.returnValue();
                ++ci;
            }
        }
        // endregion

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

}
