package io.github.eutro.jwasm;

import org.jetbrains.annotations.Nullable;

/**
 * A visitor that visits the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#data-section">data section</a>
 * of a module.
 * <p>
 * Methods are expected to be called in the order:
 * <p>
 * ( {@code visitData} )*
 * {@code visitEnd}
 */
public class DataSegmentsVisitor extends BaseVisitor<DataSegmentsVisitor> {
    /**
     * Construct a visitor with no delegate.
     */
    public DataSegmentsVisitor() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    public DataSegmentsVisitor(@Nullable DataSegmentsVisitor dl) {
        super(dl);
    }

    /**
     * Visit a data segment of the module.
     *
     * @return A {@link DataVisitor} to visit with the contents of the data segment,
     * or {@code null} if this visitor is not interested in the contents of the data segment.
     */
    public DataVisitor visitData() {
        if (dl != null) return dl.visitData();
        return null;
    }
}
