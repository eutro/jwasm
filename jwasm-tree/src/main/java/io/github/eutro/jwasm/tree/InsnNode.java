package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

/**
 * A node that represents an instruction with no immediate arguments.
 *
 * @see ExprVisitor#visitInsn(byte)
 */
public class InsnNode extends AbstractInsnNode {
    /**
     * Construct an {@link InsnNode} with the given opcode.
     *
     * @param opcode The opcode of the instruction.
     */
    public InsnNode(byte opcode) {
        super(opcode);
    }

    /**
     * {@inheritDoc}
     *
     * @param ev The visitor to visit.
     * @see ExprVisitor#visitInsn(byte)
     */
    @Override
    public void accept(ExprVisitor ev) {
        ev.visitInsn(opcode);
    }
}
