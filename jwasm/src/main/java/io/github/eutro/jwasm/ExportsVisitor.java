package io.github.eutro.jwasm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A visitor that visits the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#global-section">global section</a>
 * of a module.
 * <p>
 * Methods are expected to be called in the order:
 * <p>
 * ( {@code visitExport} )*
 * {@code visitEnd}
 */
public class ExportsVisitor extends BaseVisitor<ExportsVisitor> {
    /**
     * Construct a visitor with no delegate.
     */
    public ExportsVisitor() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    public ExportsVisitor(@Nullable ExportsVisitor dl) {
        super(dl);
    }

    /**
     * Visit an export of the module.
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
    public void visitExport(@NotNull String name, byte type, int index) {
        if (dl != null) dl.visitExport(name, type, index);
    }
}
