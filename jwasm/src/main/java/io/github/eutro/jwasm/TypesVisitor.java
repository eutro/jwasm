package io.github.eutro.jwasm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A visitor that visits the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#type-section">type section</a>
 * of a module.
 * <p>
 * Methods are expected to be called in the order:
 * <p>
 * ( {@code visitFuncType} )*
 * {@code visitEnd}
 */
public class TypesVisitor extends BaseVisitor<TypesVisitor> {
    /**
     * Construct a visitor with no delegate.
     */
    public TypesVisitor() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    public TypesVisitor(@Nullable TypesVisitor dl) {
        super(dl);
    }

    /**
     * Visit a
     * <a href="https://webassembly.github.io/spec/core/binary/types.html#function-types">{@code functype}</a>
     * element of the type vector.
     *
     * @param params  The parameter
     *                <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-valtype">valtype</a>s
     *                of the
     *                <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-functype">functype</a>.
     * @param returns The return
     *                <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-valtype">valtype</a>s
     *                of the
     *                <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-functype">functype</a>.
     */
    public void visitFuncType(byte @NotNull [] params, byte @NotNull [] returns) {
        if (dl != null) dl.visitFuncType(params, returns);
    }
}
