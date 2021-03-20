package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

/**
 * A node that represents a break instruction.
 *
 * @see ExprVisitor#visitBreakInsn(byte, int)
 */
public class BreakInsnNode extends AbstractInsnNode {
    /**
     * The
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-labelidx">index</a>
     * of the label.
     */
    public int label;

    /**
     * Construct a {@link BreakInsnNode} with the given opcode and label.
     *
     * @param opcode The opcode of the instruction.
     * @param label  The
     *               <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-labelidx">index</a>
     *               of the label.
     */
    public BreakInsnNode(byte opcode, int label) {
        super(opcode);
        this.label = label;
    }

    /**
     * {@inheritDoc}
     *
     * @param ev The visitor to visit.
     * @see ExprVisitor#visitBreakInsn(byte, int)
     */
    @Override
    void accept(ExprVisitor ev) {
        ev.visitBreakInsn(opcode, label);
    }
}
