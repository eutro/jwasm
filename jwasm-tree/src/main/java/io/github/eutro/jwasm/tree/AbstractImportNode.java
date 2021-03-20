package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ImportsVisitor;
import io.github.eutro.jwasm.Opcodes;

/**
 * A note that represents an
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#import-section">import</a>
 * of a module.
 *
 * @see ImportsVisitor
 * @see ImportsNode
 */
public abstract class AbstractImportNode {
    /**
     * The module being imported from.
     */
    public String module;

    /**
     * The name of the imported value.
     */
    public String name;

    /**
     * Construct an {@link AbstractImportNode} with the given module and name.
     *
     * @param module The module being imported from.
     * @param name   The name of the imported value.
     */
    protected AbstractImportNode(String module, String name) {
        this.module = module;
        this.name = name;
    }

    /**
     * Get the numerical value of the import type.
     *
     * @return The type of the import, one of
     * {@link Opcodes#IMPORTS_FUNC},
     * {@link Opcodes#IMPORTS_TABLE},
     * {@link Opcodes#IMPORTS_MEM} or
     * {@link Opcodes#IMPORTS_GLOBAL}
     */
    public abstract byte importType();

    /**
     * Visit the given {@link ImportsVisitor} with this import.
     *
     * @param iv The visitor to visit.
     */
    abstract void accept(ImportsVisitor iv);
}
