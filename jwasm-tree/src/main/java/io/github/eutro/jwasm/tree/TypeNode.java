package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.TypesVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

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
     * Like {@link #TypeNode(byte[], byte[])} but with {@link List} inputs.
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
    public TypeNode(List<Byte> params, List<Byte> returns) {
        this.params = byteListToArray(params);
        this.returns = byteListToArray(returns);
    }

    private static byte[] byteListToArray(List<Byte> params) {
        byte[] paramsI = new byte[params.size()];
        int i = 0;
        for (Byte param : params) {
            paramsI[i++] = param;
        }
        return paramsI;
    }

    /**
     * Make the given {@link TypesVisitor} visit this type.
     *
     * @param tv The visitor to visit.
     */
    public void accept(TypesVisitor tv) {
        tv.visitFuncType(params, returns);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeNode typeNode = (TypeNode) o;
        return Arrays.equals(params, typeNode.params) && Arrays.equals(returns, typeNode.returns);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(params);
        result = 31 * result + Arrays.hashCode(returns);
        return result;
    }
}
