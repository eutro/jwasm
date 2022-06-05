package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;

/**
 * A node that represents a prefix instruction.
 * <p>
 * This class has no immediate arguments, but subclasses might.
 *
 * @see ExprVisitor#visitPrefixInsn(int)
 */
public class PrefixInsnNode extends AbstractInsnNode {
    /**
     * The integer opcode of the instruction.
     */
    public int intOpcode;

    /**
     * Construct an {@link PrefixInsnNode} with the given integer opcode.
     *
     * @param intOpcode The integer opcode of the instruction.
     */
    public PrefixInsnNode(int intOpcode) {
        super(Opcodes.INSN_PREFIX);
        this.intOpcode = intOpcode;
    }

    /**
     * {@inheritDoc}
     *
     * @param ev The visitor to visit.
     * @see ExprVisitor#visitPrefixInsn(int)
     */
    @Override
    public void accept(ExprVisitor ev) {
        ev.visitPrefixInsn(intOpcode);
    }
}
