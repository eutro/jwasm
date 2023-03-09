package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

/**
 * A node that represents a prefixed table instruction.
 *
 * @see ExprVisitor#visitPrefixTableInsn(int, int)
 */
public class PrefixTableInsnNode extends PrefixInsnNode {
    /**
     * The
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-tableidx">index</a>
     * of the table.
     */
    public int table;

    /**
     * Construct a {@link PrefixTableInsnNode} with the given opcode and table.
     *
     * @param intOpcode The opcode of the instruction.
     * @param table     The
     *                  <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-tableidx">index</a>
     *                  of the table.
     */
    public PrefixTableInsnNode(int intOpcode, int table) {
        super(intOpcode);
        this.table = table;
    }

    /**
     * {@inheritDoc}
     *
     * @param ev The visitor to visit.
     * @see ExprVisitor#visitPrefixTableInsn(int, int)
     */
    @Override
    public void accept(ExprVisitor ev) {
        ev.visitPc(pc);
        ev.visitPrefixTableInsn(intOpcode, table);
    }
}
