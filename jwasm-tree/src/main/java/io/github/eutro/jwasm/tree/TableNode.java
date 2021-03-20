package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.Limits;
import io.github.eutro.jwasm.TablesVisitor;

/**
 * A node that represents a table of a module.
 *
 * @see TablesVisitor#visitTable(int, Integer, byte)
 */
public class TableNode {
    /**
     * The limits of the table.
     */
    public Limits limits;

    /**
     * The <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-reftype">reftype</a>
     * of values in the table.
     */
    public byte type;

    /**
     * Construct a {@link TableNode} with the given limits and type.
     *
     * @param limits The limits of the table.
     * @param type   The <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-reftype">reftype</a>
     *               of values in the table.
     */
    public TableNode(Limits limits, byte type) {
        this.limits = limits;
        this.type = type;
    }

    /**
     * Make the given {@link TablesVisitor} visit this table.
     *
     * @param tv The visitor to visit.
     */
    public void accept(TablesVisitor tv) {
        tv.visitTable(limits.min, limits.max, type);
    }
}
