package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ImportsVisitor;
import io.github.eutro.jwasm.Opcodes;

/**
 * A node that represents a global import.
 *
 * @see ImportsVisitor#visitGlobalImport(String, String, byte, byte)
 */
public class GlobalImportNode extends AbstractImportNode {
    /**
     * The type of the imported global.
     */
    public GlobalTypeNode type;

    /**
     * Construct a {@link GlobalImportNode} with the given module, name and type.
     *
     * @param module The module to import from.
     * @param name   The name to import.
     * @param type   The type of the imported global.
     */
    public GlobalImportNode(String module, String name, GlobalTypeNode type) {
        super(module, name);
        this.type = type;
    }

    @Override
    public byte importType() {
        return Opcodes.IMPORTS_GLOBAL;
    }

    @Override
    void accept(ImportsVisitor iv) {
        iv.visitGlobalImport(module, name, type.mut, type.type);
    }
}
