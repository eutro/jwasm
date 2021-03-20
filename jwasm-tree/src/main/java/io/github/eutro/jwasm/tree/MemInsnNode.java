package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

/**
 * A node that represents a memory instruction with a single memory argument.
 *
 * @see ExprVisitor#visitMemInsn(byte, int, int)
 */
public class MemInsnNode extends AbstractInsnNode {
    /**
     * The {@code align} of the argument.
     */
    public int align;

    /**
     * The {@code offset} of the argument.
     */
    public int offset;

    /**
     * Construct a {@link MemInsnNode} with the given opcode, align and offset.
     *
     * @param opcode The opcode of the instruction.
     * @param align  The {@code align} of the argument.
     * @param offset The {@code offset} of the argument.
     */
    public MemInsnNode(byte opcode, int align, int offset) {
        super(opcode);
        this.align = align;
        this.offset = offset;
    }

    /**
     * {@inheritDoc}
     *
     * @param ev The visitor to visit.
     * @see ExprVisitor#visitMemInsn(byte, int, int)
     */
    @Override
    void accept(ExprVisitor ev) {
        ev.visitMemInsn(opcode, align, offset);
    }
}
