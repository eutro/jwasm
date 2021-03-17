package io.github.eutro.jwasm;

import org.jetbrains.annotations.Nullable;

/**
 * A visitor that visits the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#memory-section">memory section</a>
 * of a module.
 * <p>
 * Methods are expected to be called in the order:
 * <p>
 * ( {@code visitMemory} )*
 * {@code visitEnd}
 */
public class MemoriesVisitor extends BaseVisitor<MemoriesVisitor> {
    /**
     * Construct a visitor with no delegate.
     */
    public MemoriesVisitor() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    public MemoriesVisitor(@Nullable MemoriesVisitor dl) {
        super(dl);
    }

    /**
     * Visit a linear memory of the module.
     *
     * @param min The minimum of the
     *            <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-limits">limit</a>
     *            of the
     *            <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-memtype">memtype</a>.
     * @param max The maximum of the
     *            <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-limits">limit</a>
     *            of the
     *            <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-memtype">memtype</a>,
     *            or {@code null} if unspecified.
     */
    public void visitMemory(int min, @Nullable Integer max) {
        if (dl != null) dl.visitMemory(min, max);
    }
}
