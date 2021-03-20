package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.TypesVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * A node that represents a
 * <a href="https://webassembly.github.io/spec/core/binary/types.html#function-types">{@code functype}</a>.
 *
 * @see TypesVisitor#visitFuncType(byte[], byte[])
 */
public class TypeNode {
    /**
     * The parameter
     * <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-valtype">valtype</a>s
     * of the
     * <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-functype">functype</a>.
     */
    public byte @NotNull [] params;

    /**
     * The return
     * <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-valtype">valtype</a>s
     * of the
     * <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-functype">functype</a>.
     */
    public byte @NotNull [] returns;

    /**
     * Construct a {@link TypeNode} with the given params and returns.
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
    public TypeNode(byte @NotNull [] params, byte @NotNull [] returns) {
        this.params = params;
        this.returns = returns;
    }

    /**
     * Make the given {@link TypesVisitor} visit this type.
     *
     * @param tv The visitor to visit.
     */
    public void accept(TypesVisitor tv) {
        tv.visitFuncType(params, returns);
    }
}
