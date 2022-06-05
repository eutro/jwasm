package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;

/**
 * A node that represents a const instruction.
 *
 * @see ExprVisitor#visitConstInsn(Object)
 */
public class ConstInsnNode extends AbstractInsnNode {
    /**
     * The constant value of the instruction, which may be an
     * {@link Integer}, {@link Long}, {@link Float} or {@link Double},
     * representing {@code i32}, {@code i64}, {@code f32} and {@code f64}
     * {@code const} instructions respectively.
     */
    public Object value;

    /**
     * Construct a {@link ConstInsnNode} with the given value.
     *
     * @param value The constant value of the instruction, which may be an
     *              {@link Integer}, {@link Long}, {@link Float} or {@link Double},
     *              representing {@code i32}, {@code i64}, {@code f32} and {@code f64}
     *              {@code const} instructions respectively.
     */
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

    /**
     * {@inheritDoc}
     *
     * @param ev The visitor to visit.
     * @see ExprVisitor#visitConstInsn(Object)
     */
    @Override
    public void accept(ExprVisitor ev) {
        ev.visitConstInsn(value);
    }
}
