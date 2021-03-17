package io.github.eutro.jwasm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A {@link TypesVisitor} that generates the corresponding WebAssembly bytecode as it is visited.
 * This can be retrieved using {@link #toByteArray()} after {@link #visitEnd()}.
 */
public class TypesWriter extends TypesVisitor implements VectorWriter {
    /**
     * The {@link ByteOutputStream} that this visitor will write raw bytes to.
     */
    private final ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();

    /**
     * Records number of elements in the vector.
     */
    private int count;

    /**
     * A callback that is called from {@link #visitEnd()}, or {@code null}.
     */
    public @Nullable Consumer<TypesWriter> onEnd;

    /**
     * Constructs a writer with no {@link #onEnd end callback}.
     */
    public TypesWriter() {
    }

    /**
     * Constructs a writer with an optional {@link #onEnd end callback}.
     *
     * @param onEnd A callback that is called from {@link #visitEnd()}, or {@code null}.
     */
    public TypesWriter(@Nullable Consumer<TypesWriter> onEnd) {
        this.onEnd = onEnd;
    }

    @Override
    public int count() {
        return count;
    }

    @Override
    public byte[] raw() {
        return out.toByteArray();
    }

    @Override
    public void visitFuncType(byte @NotNull [] params, byte @NotNull [] returns) {
        ++count;
        out.put(Opcodes.TYPES_FUNCTION);
        out.putByteArray(params);
        out.putByteArray(returns);
    }

    @Override
    public void visitEnd() {
        if (onEnd != null) onEnd.accept(this);
    }
}
