package io.github.eutro.jwasm;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A {@link MemoriesVisitor} that generates the corresponding WebAssembly bytecode as it is visited.
 * This can be retrieved using {@link #toByteArray()} after {@link #visitEnd()}.
 */
public class MemoriesWriter extends MemoriesVisitor implements VectorWriter {
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
    public @Nullable Consumer<MemoriesWriter> onEnd;

    /**
     * Constructs a writer with no {@link #onEnd end callback}.
     */
    public MemoriesWriter() {
    }

    /**
     * Constructs a writer with an optional {@link #onEnd end callback}.
     *
     * @param onEnd A callback that is called from {@link #visitEnd()}, or {@code null}.
     */
    public MemoriesWriter(@Nullable Consumer<MemoriesWriter> onEnd) {
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
    public void visitMemory(int min, Integer max) {
        ++count;
        out.putLimit(min, max);
    }

    @Override
    public void visitEnd() {
        if (onEnd != null) onEnd.accept(this);
    }
}