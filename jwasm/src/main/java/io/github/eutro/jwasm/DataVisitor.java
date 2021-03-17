package io.github.eutro.jwasm;

import org.jetbrains.annotations.Nullable;

/**
 * A visitor that visits a
 * <a href="https://webassembly.github.io/spec/core/syntax/modules.html#syntax-data">data segment</a>
 * in the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#data-section">data section</a>
 * of a module.
 * <p>
 * Methods are expected to be called in the order:
 * <p>
 * [ {@code visitActive} ]
 * {@code visitInit}
 * {@code visitEnd}
 */
public class DataVisitor extends BaseVisitor<DataVisitor> {
    /**
     * Construct a visitor with no delegate.
     */
    public DataVisitor() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    public DataVisitor(@Nullable DataVisitor dl) {
        super(dl);
    }

    /**
     * Visit the {@code memory} index and {@code offset} expr of the data segment, if it is active.
     *
     * @param memory The memory
     *               <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-memidx">index</a>
     *               of the data segment.
     * @return An {@link ExprVisitor} to visit with the contents of the {@code offset} expr,
     * or {@code null} if this visitor is not interested in the contents of the {@code offset} expr.
     */
    public @Nullable ExprVisitor visitActive(int memory) {
        if (dl != null) dl.visitActive(memory);
        return null;
    }

    /**
     * Visit the {@code init} bytes of the data segment.
     *
     * @param init The {@code init} bytes of the data segment.
     */
    public void visitInit(byte[] init) {
        if (dl != null) dl.visitInit(init);
    }
}
