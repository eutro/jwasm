package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

/**
 * A node that represents a variable instruction.
 *
 * @see ExprVisitor#visitVariableInsn(byte, int)
 */
public class VariableInsnNode extends AbstractInsnNode {
    /**
     * The
     * <a href="https://webassembly.github.io/spec/core/syntax/modules.html#syntax-index">index</a>
     * of the variable.
     */
    public int variable;

    /**
     * Construct a {@link VariableInsnNode} with the given opcode and index.
     *
     * @param opcode   The opcode of the instruction.
     * @param variable The
     *                 <a href="https://webassembly.github.io/spec/core/syntax/modules.html#syntax-index">index</a>
     *                 of the variable.
     */
    public VariableInsnNode(byte opcode, int variable) {
        super(opcode);
        this.variable = variable;
    }

    /**
     * {@inheritDoc}
     *
     * @param ev The visitor to visit.
     * @see ExprVisitor#visitVariableInsn(byte, int)
     */
    @Override
    public void accept(ExprVisitor ev) {
        ev.visitPc(pc);
        ev.visitVariableInsn(opcode, variable);
    }
}
