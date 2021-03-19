package io.github.eutro.jwasm2java;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.tree.*;

import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.*;

public class Util {
    public static InsnList makeList(AbstractInsnNode... nodes) {
        InsnList list = new InsnList();
        for (AbstractInsnNode node : nodes) {
            list.add(node);
        }
        return list;
    }

    public static InsnList concat(InsnList first, InsnList... rest) {
        for (InsnList l : rest) {
            first.add(l);
        }
        return first;
    }

    @NotNull
    public static MethodInsnNode staticNode(String owner, String name, String desc) {
        return new MethodInsnNode(INVOKESTATIC, owner, name, desc, false);
    }

    @NotNull
    public static MethodInsnNode virtualNode(String owner, String name, String desc) {
        return new MethodInsnNode(INVOKEVIRTUAL, owner, name, desc, false);
    }

    @NotNull
    public static AbstractInsnNode constant(Object v) {
        if (v instanceof Integer || v instanceof Long || v instanceof Float || v instanceof Double) {
            MethodNode mn = new MethodNode();
            InstructionAdapter ia = new InstructionAdapter(mn);
            if (v instanceof Integer) {
                ia.iconst((Integer) v);
            } else if (v instanceof Long) {
                ia.lconst((Long) v);
            } else if (v instanceof Float) {
                ia.fconst((Float) v);
            } else {
                ia.dconst((Double) v);
            }
            return mn.instructions.getFirst();
        }
        return v == null ? new InsnNode(ACONST_NULL) : new LdcInsnNode(v);
    }

    @NotNull
    public static InsnList fromAdapter(Consumer<InstructionAdapter> iac) {
        MethodNode mn = new MethodNode();
        iac.accept(new InstructionAdapter(mn));
        return mn.instructions;
    }
}