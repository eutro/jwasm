package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

/**
 * A node that represents a table instruction with two immediate index arguments.
 *
 * @see ExprVisitor#visitPrefixBinaryTableInsn(int, int, int)
 */
public class PrefixBinaryTableInsnNode extends PrefixInsnNode {
    /**
     * The
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-tableidx">index</a>
     * of the target table.
     */
    public int firstIndex;

    /**
     * The second
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#indices">index</a>
     * argument.
     */
    public int secondIndex;

    /**
     * Construct a {@link PrefixBinaryTableInsnNode} with the given opcode and immediate arguments.
     *
     * @param intOpcode   The opcode of the instruction.
     * @param firstIndex  The
     *                    <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-tableidx">index</a>
     *                    of the target table.
     * @param secondIndex The second
     *                    <a href="https://webassembly.github.io/spec/core/binary/modules.html#indices">index</a>
     *                    argument.
     */
    public PrefixBinaryTableInsnNode(int intOpcode, int firstIndex, int secondIndex) {
        super(intOpcode);
        this.firstIndex = firstIndex;
        this.secondIndex = secondIndex;
    }

    /**
     * {@inheritDoc}
     *
     * @param ev The visitor to visit.
     * @see ExprVisitor#visitPrefixBinaryTableInsn(int, int, int)
     */
    @Override
    public void accept(ExprVisitor ev) {
        ev.visitPc(pc);
        ev.visitPrefixBinaryTableInsn(intOpcode, firstIndex, secondIndex);
    }
}
