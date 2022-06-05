package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ImportsVisitor;
import io.github.eutro.jwasm.Limits;
import io.github.eutro.jwasm.Opcodes;

/**
 * A node that represents a function import.
 *
 * @see ImportsVisitor#visitTableImport(String, String, int, Integer, byte)
 */
public class TableImportNode extends AbstractImportNode {
    /**
     * The limits of the imported table.
     */
    public Limits limits;

    /**
     * The <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-reftype">reftype</a>
     * of values in the imported table.
     */
    public byte type;

    /**
     * Construct a {@link TableImportNode} with the given module, name, limits and type.
     *
     * @param module The module to import from.
     * @param name   The name to import.
     * @param limits The limits of the imported table.
     * @param type   The <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-reftype">reftype</a>
     *               of values in the imported table.
     */
    public TableImportNode(String module, String name, Limits limits, byte type) {
        super(module, name);
        this.limits = limits;
        this.type = type;
    }

    @Override
    public byte importType() {
        return Opcodes.IMPORTS_TABLE;
    }

    @Override
    public void accept(ImportsVisitor iv) {
        iv.visitTableImport(module, name, limits.min, limits.max, type);
    }
}
