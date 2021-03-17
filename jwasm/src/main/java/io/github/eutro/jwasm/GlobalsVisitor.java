package io.github.eutro.jwasm;

import org.jetbrains.annotations.Nullable;

/**
 * A visitor that visits the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#global-section">global section</a>
 * of a module.
 * <p>
 * Methods are expected to be called in the order:
 * <p>
 * ( {@code visitGlobal} )*
 * {@code visitEnd}
 */
public class GlobalsVisitor extends BaseVisitor<GlobalsVisitor> {
    /**
     * Construct a visitor with no delegate.
     */
    public GlobalsVisitor() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    public GlobalsVisitor(@Nullable GlobalsVisitor dl) {
        super(dl);
    }

    /**
     * Visit a global of the module.
     *
     * @param mut  The
     *             <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-mut">mutability</a>
     *             of the
     *             <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-globaltype">globaltype</a>
     *             of the global.
     * @param type The
     *             <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-valtype">valtype</a>
     *             of the
     *             <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-globaltype">globaltype</a>
     *             of the global.
     * @return An {@link ExprVisitor} to visit with the {@code init} expression,
     * or {@code null} if this visitor is not interested in the {@code init} expression.
     */
    public @Nullable ExprVisitor visitGlobal(byte mut, byte type) {
        if (dl != null) return dl.visitGlobal(mut, type);
        return null;
    }
}
