package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExportsVisitor;
import io.github.eutro.jwasm.Opcodes;
import org.jetbrains.annotations.NotNull;

/**
 * A node that represents an export of a module.
 *
 * @see ExportsVisitor#visitExport(String, byte, int)
 * @see ExportsNode
 */
public class ExportNode {
    /**
     * The name of the export.
     */
    public @NotNull String name;

    /**
     * The type of the export, one of
     * {@link Opcodes#EXPORTS_FUNC},
     * {@link Opcodes#EXPORTS_TABLE},
     * {@link Opcodes#EXPORTS_MEM} or
     * {@link Opcodes#EXPORTS_GLOBAL}.
     */
    public byte type;

    /**
     * The <a href="https://webassembly.github.io/spec/core/binary/modules.html#indices">index</a>
     * of the exported value.
     */
    public int index;

    /**
     * Construct an {@link ExportNode} with the given name, type and index.
     *
     * @param name  The name of the export.
     * @param type  The type of the export, one of
     *              {@link Opcodes#EXPORTS_FUNC},
     *              {@link Opcodes#EXPORTS_TABLE},
     *              {@link Opcodes#EXPORTS_MEM} or
     *              {@link Opcodes#EXPORTS_GLOBAL}.
     * @param index The <a href="https://webassembly.github.io/spec/core/binary/modules.html#indices">index</a>
     *              of the exported value.
     */
    public ExportNode(@NotNull String name, byte type, int index) {
        this.name = name;
        this.type = type;
        this.index = index;
    }

    /**
     * Make the given {@link ExportsVisitor} visit this export.
     *
     * @param ev The visitor to visit.
     */
    public void accept(ExportsVisitor ev) {
        ev.visitExport(name, type, index);
    }
}
