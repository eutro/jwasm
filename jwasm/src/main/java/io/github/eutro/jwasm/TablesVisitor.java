package io.github.eutro.jwasm;

import org.jetbrains.annotations.Nullable;

/**
 * A visitor that visits the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#table-section">table section</a>
 * of a module.
 * <p>
 * Methods are expected to be called in the order:
 * <p>
 * ( {@code visitTable} )*
 * {@code visitEnd}
 */
public class TablesVisitor extends BaseVisitor<TablesVisitor> {
    /**
     * Construct a visitor with no delegate.
     */
    public TablesVisitor() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    public TablesVisitor(@Nullable TablesVisitor dl) {
        super(dl);
    }

    /**
     * Visit the type of a table in the module.
     *
     * @param min    The minimum of the
     *               <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-limits">limit</a>
     *               of the table's
     *               <a href="https://webassembly.github.io/spec/core/binary/types.html#table-types">type</a>.
     * @param max    The maximum of the
     *               <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-limits">limit</a>
     *               of the table's
     *               <a href="https://webassembly.github.io/spec/core/binary/types.html#table-types">type</a>,
     *               or {@code null} if unspecified.
     * @param type   The <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-reftype">reftype</a>
     *               of values in the table.
     */
    public void visitTable(int min, @Nullable Integer max, byte type) {
        if (dl != null) dl.visitTable(min, max, type);
    }
}
