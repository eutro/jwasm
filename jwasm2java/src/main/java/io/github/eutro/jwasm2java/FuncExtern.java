package io.github.eutro.jwasm2java;

import io.github.eutro.jwasm.tree.TypeNode;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;

import static org.objectweb.asm.Opcodes.*;

public interface FuncExtern extends Extern {
    void emitInvoke(MethodVisitor mv);
    TypeNode type();

    class ModuleFuncExtern implements FuncExtern {
        private final ClassNode cn;
        private final MethodNode mn;
        private final TypeNode type;
        private @Nullable MethodNode glue;

        public ModuleFuncExtern(ClassNode cn, MethodNode mn, TypeNode type) {
            this.cn = cn;
            this.mn = mn;
            this.type = type;
        }

        private MethodNode createGlue(ClassNode cn, MethodNode mn) {
            MethodNode glue = new MethodNode();
            cn.methods.add(glue);
            Type methodType = Type.getMethodType(mn.desc);
            Type[] argTypes = methodType.getArgumentTypes();
            Type[] glueArgTypes = Arrays.copyOf(argTypes, argTypes.length + 1);
            glueArgTypes[argTypes.length] = Type.getObjectType(cn.name);
            glue.access = ACC_SYNTHETIC | ACC_PRIVATE;
            glue.name = mn.name + "$glue";
            glue.desc = Type.getMethodType(methodType.getReturnType(), glueArgTypes).toString();
            GeneratorAdapter ga = new GeneratorAdapter(glue.access, new Method(glue.name, glue.desc), glue);
            ga.loadArg(argTypes.length);
            ga.loadArgs(0, argTypes.length);
            ga.visitMethodInsn(INVOKEVIRTUAL, cn.name, mn.name, mn.desc, false);
            ga.returnValue();
            return glue;
        }

        @Override
        public void emitGet(MethodVisitor mv) {
            // MethodHandles.insertArguments(<handle>, 0, new Object[] { this })
            mv.visitLdcInsn(new Handle(H_INVOKEVIRTUAL, cn.name, mn.name, mn.desc, false));
            mv.visitInsn(ICONST_0);
            mv.visitInsn(ICONST_1);
            mv.visitTypeInsn(NEWARRAY, "java/lang/Object");
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitInsn(AASTORE);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/invoke/MethodHandles", "insertArguments",
                    "(Ljava/lang/invoke/MethodHandle;I[Ljava/lang/Object;)Ljava/lang/invoke/MethodHandle;",
                    false);
        }

        @Override
        public void emitInvoke(MethodVisitor mv) {
            if (glue == null) glue = createGlue(cn, mn);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, cn.name, glue.name, glue.desc, false);
        }

        @Override
        public TypeNode type() {
            return type;
        }
    }
}
