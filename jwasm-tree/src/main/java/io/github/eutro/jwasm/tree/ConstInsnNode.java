package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;

public class ConstInsnNode extends AbstractInsnNode {
    public Object value;

    public ConstInsnNode(Object value) {
        super(opcodeFor(value));
        this.value = value;
    }

    private static byte opcodeFor(Object value) {
        if (value instanceof Integer) {
            return Opcodes.I32_CONST;
        } else if (value instanceof Long) {
            return Opcodes.I64_CONST;
        } else if (value instanceof Float) {
            return Opcodes.F32_CONST;
        } else if (value instanceof Double) {
            return Opcodes.F64_CONST;
        }
        throw new IllegalArgumentException();
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitConstInsn(value);
    }
}
