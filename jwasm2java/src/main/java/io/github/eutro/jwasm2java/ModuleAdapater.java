package io.github.eutro.jwasm2java;

import io.github.eutro.jwasm.ModuleVisitor;
import io.github.eutro.jwasm.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static io.github.eutro.jwasm.Opcodes.*;
import static org.objectweb.asm.Opcodes.*;

public class ModuleAdapater extends ModuleVisitor {
    public static final Method GLOBALS_METHOD = new Method("__globals", "()V");
    private final ClassVisitor cv;
    private final String internalName;
    private final List<Limits> memories = new ArrayList<>();
    private final List<FuncType> types = new ArrayList<>();
    private final List<FuncType> funcTypes = new ArrayList<>();
    private final List<Function> functions = new ArrayList<>();
    private boolean hasGlobals = false;
    private Integer start = null;

    public ModuleAdapater(ClassVisitor cv, String internalName) {
        this.cv = cv;
        this.internalName = internalName;
    }

    @Override
    public void visitHeader(int version) {
        cv.visit(V1_8,
                ACC_PUBLIC | ACC_SUPER | ACC_FINAL,
                internalName,
                null,
                Type.getInternalName(Object.class),
                null);
    }

    @Override
    public void visitCustom(@NotNull String name, byte @NotNull [] payload) {
        AnnotationVisitor av = cv.visitAnnotation("io/github/eutro/jwasm/runtime/CustomSection", true);
        av.visit("name", name);
        av.visit("data", payload);
        av.visitEnd();
    }

    @Override
    public @NotNull MemoriesVisitor visitMems() {
        return new MemoriesVisitor() {
            @Override
            public void visitMemory(int min, @Nullable Integer max) {
                memories.add(new Limits(min, max));
            }
        };
    }

    @Override
    public @NotNull TypesVisitor visitTypes() {
        return new TypesVisitor() {
            @Override
            public void visitFuncType(byte @NotNull [] params, byte @NotNull [] returns) {
                types.add(new FuncType(params, returns));
            }
        };
    }

    @Override
    public @NotNull FunctionsVisitor visitFuncs() {
        return new FunctionsVisitor() {
            @Override
            public void visitFunc(int type) {
                FuncType funcType = types.get(type);
                funcTypes.add(funcType);
                Method method = new Method("func" + funcTypes.size(), funcType.toDescriptor());
                functions.add(new Function(mv -> {
                    int[] locals = new int[funcType.params.length];
                    for (int i = funcType.params.length - 1; i >= 0; i--) {
                        int local = locals[i] = mv.newLocal(Types.toJava(funcType.params[i]));
                        mv.storeLocal(local);
                    }
                    mv.loadThis();
                    for (int local : locals) {
                        mv.loadLocal(local);
                    }
                    mv.invokeVirtual(Type.getObjectType(internalName), method);
                }));
            }
        };
    }

    @Override
    public @NotNull GlobalsVisitor visitGlobals() {
        hasGlobals = true;
        List<Boolean> mutabilities = new ArrayList<>();
        LinkedList<Type> types = new LinkedList<>();
        GeneratorAdapter ga = new GeneratorAdapter(ACC_PRIVATE, GLOBALS_METHOD, null, null, cv);
        return new GlobalsVisitor() {
            @Override
            public @NotNull ExprVisitor visitGlobal(byte mut, byte type) {
                types.add(Types.toJava(type));
                mutabilities.add(mut == MUT_VAR);
                return new ExprAdapter(ga,
                        internalName,
                        new FuncType(new byte[0], new byte[0]),
                        new byte[0],
                        new Function[0]) {
                    @Override
                    public void visitEnd() {
                        ga.putField(Type.getObjectType(internalName), "glob" + types.size(), types.getLast());
                    }
                };
            }

            @Override
            public void visitEnd() {
                ga.endMethod();
                int i = 0;
                for (Type type : types) {
                    cv.visitField(ACC_PRIVATE | (mutabilities.get(i) ? ACC_FINAL : 0),
                            "glob" + i,
                            type.toString(),
                            null,
                            null);
                    ++i;
                }
            }
        };
    }

    @Override
    public @NotNull CodesVisitor visitCode() {
        return new CodesVisitor() {
            private int count;

            @Override
            public @NotNull ExprVisitor visitCode(byte @NotNull [] locals) {
                FuncType type = funcTypes.get(count);
                GeneratorAdapter mv = new GeneratorAdapter(ACC_PRIVATE,
                        new Method("func" + count, type.toDescriptor()),
                        null,
                        null,
                        cv);
                ++count;
                return new ExprAdapter(mv, internalName, type, locals, functions.toArray(new Function[0])) {
                    @Override
                    public void visitEnd() {
                        doReturn();
                        mv.endMethod();
                    }
                };
            }
        };
    }

    @Override
    public @NotNull ExportsVisitor visitExports() {
        return new ExportsVisitor() {
            @Override
            public void visitExport(@NotNull String name, byte type, int index) {
                switch (type) {
                    case EXPORTS_FUNC:
                        String desc = funcTypes.get(index).toDescriptor();
                        GeneratorAdapter ga = new GeneratorAdapter(ACC_PUBLIC,
                                new Method(name, desc),
                                null,
                                null,
                                cv);
                        ga.loadThis();
                        ga.loadArgs();
                        ga.invokeVirtual(Type.getObjectType(internalName), new Method("func" + index, desc));
                        ga.returnValue();
                        ga.endMethod();
                        break;
                    case EXPORTS_MEM:

                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            }
        };
    }

    @Override
    public void visitStart(int func) {
        start = func;
    }

    @Override
    public void visitEnd() {
        generateFields();
        generateConstructor();
        cv.visitEnd();
    }

    private void generateFields() {
        for (int i = 0; i < memories.size(); i++) {
            FieldVisitor field = cv.visitField(ACC_PUBLIC, "mem" + i, Type.getDescriptor(ByteBuffer.class), null, null);
            field.visitEnd();
        }
    }

    private void generateConstructor() {
        MethodVisitor ctor = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        ctor.visitVarInsn(ALOAD, 0);
        ctor.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V", false);
        for (int i = 0; i < memories.size(); i++) {
            ctor.visitVarInsn(ALOAD, 0);
            Limits memory = memories.get(i);
            ctor.visitLdcInsn(memory.min * PAGE_SIZE);
            // ByteBuffer.allocate(capacity)
            ctor.visitMethodInsn(INVOKESTATIC,
                    Type.getInternalName(ByteBuffer.class),
                    "allocate",
                    Type.getMethodDescriptor(Type.getType(ByteBuffer.class), Type.INT_TYPE),
                    false);
            ctor.visitFieldInsn(GETSTATIC,
                    Type.getInternalName(ByteOrder.class),
                    "LITTLE_ENDIAN",
                    Type.getDescriptor(ByteOrder.class));
            ctor.visitMethodInsn(INVOKEVIRTUAL,
                    Type.getInternalName(ByteBuffer.class),
                    "order",
                    Type.getMethodDescriptor(Type.getType(ByteBuffer.class), Type.getType(ByteOrder.class)),
                    false);
            ctor.visitFieldInsn(PUTFIELD, internalName, "mem" + i, Type.getDescriptor(ByteBuffer.class));
        }
        if (hasGlobals) {
            ctor.visitVarInsn(ALOAD, 0);
            ctor.visitMethodInsn(INVOKEVIRTUAL, internalName, GLOBALS_METHOD.getName(), GLOBALS_METHOD.getDescriptor(), false);
        }
        if (start != null) {
            ctor.visitVarInsn(ALOAD, 0);
            FuncType startType = funcTypes.get(start);
            ctor.visitMethodInsn(INVOKEVIRTUAL, internalName, "func" + start, startType.toDescriptor(), false);
            switch (Types.returnType(startType.returns).getSize()) {
                case 1:
                    ctor.visitInsn(POP);
                    break;
                case 2:
                    ctor.visitInsn(POP2);
                    break;
            }
        }
        ctor.visitInsn(Opcodes.RETURN);
        ctor.visitMaxs(0, 0);
        ctor.visitEnd();
    }
}
