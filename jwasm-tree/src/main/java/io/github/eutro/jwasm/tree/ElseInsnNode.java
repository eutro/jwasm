package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;

/**
 * A node that represents an optional {@link Opcodes#ELSE else} pseudo-instruction in an {@link Opcodes#IF if} block.
 *
 * @see ExprVisitor#visitElseInsn()
 */
public class ElseInsnNode extends AbstractInsnNode {
    /**
     * Construct an {@link ElseInsnNode}.
     */
    public ElseInsnNode() {
        super(Opcodes.ELSE);
    }

    /**
     * {@inheritDoc}
     *
     * @param ev The visitor to visit.
     * @see ExprVisitor#visitElseInsn()
     */
    @Override
    void accept(ExprVisitor ev) {
        ev.visitElseInsn();
    }
}
