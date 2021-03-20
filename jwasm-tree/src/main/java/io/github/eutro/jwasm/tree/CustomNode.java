package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ModuleVisitor;

/**
 * A node which represents a
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#custom-section">custom section</a>
 * of a module.
 *
 * @see ModuleVisitor#visitCustom(String, byte[])
 */
public class CustomNode {
    /**
     * The name of the custom section, for further identification.
     */
    public String name;

    /**
     * The raw contents of the custom section.
     */
    public byte[] data;

    /**
     * Construct a {@link CustomNode} with the given name and data.
     *
     * @param name The name of the custom section, for further identification.
     * @param data The raw contents of the custom section.
     */
    public CustomNode(String name, byte[] data) {
        this.name = name;
        this.data = data;
    }

    /**
     * Make the given {@link ModuleVisitor} visit the section of this node.
     *
     * @param mv The visitor to visit.
     */
    public void accept(ModuleVisitor mv) {
        mv.visitCustom(name, data);
    }
}
