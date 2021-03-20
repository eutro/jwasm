package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ImportsVisitor;
import io.github.eutro.jwasm.Limits;
import io.github.eutro.jwasm.Opcodes;

/**
 * A node that represents a memory import.
 *
 * @see ImportsVisitor#visitMemImport(String, String, int, Integer)
 */
public class MemImportNode extends AbstractImportNode {
    /**
     * The limits of the imported memory.
     */
    public Limits limits;

    /**
     * Construct a {@link MemImportNode} with the given module, name and limits.
     *
     * @param module The module to import from.
     * @param name   The name to import.
     * @param limits The limits of the imported memory.
     */
    public MemImportNode(String module, String name, Limits limits) {
        super(module, name);
        this.limits = limits;
    }

    @Override
    public byte importType() {
        return Opcodes.IMPORTS_MEM;
    }

    @Override
    void accept(ImportsVisitor iv) {
        iv.visitMemImport(module, name, limits.min, limits.max);
    }
}
