package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

/**
 * A node that represents a table instruction.
 *
 * @see ExprVisitor#visitTableInsn(byte, int)
 */
public class TableInsnNode extends AbstractInsnNode {
    /**
     * The
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-tableidx">index</a>
     * of the table.
     */
    public int table;

    /**
     * Construct a {@link TableInsnNode} with the given opcode and table.
     *
     * @param opcode The opcode of the instruction.
     * @param table  The
     *               <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-tableidx">index</a>
     *               of the table.
     */
    public TableInsnNode(byte opcode, int table) {
        super(opcode);
        this.table = table;
    }

    /**
     * {@inheritDoc}
     *
     * @param ev The visitor to visit.
     * @see ExprVisitor#visitTableInsn(byte, int)
     */
    @Override
    public void accept(ExprVisitor ev) {
        ev.visitTableInsn(opcode, table);
    }
}
