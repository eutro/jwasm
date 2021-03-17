package io.github.eutro.jwasm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A {@link ExportsVisitor} that generates the corresponding WebAssembly bytecode as it is visited.
 * This can be retrieved using {@link #toByteArray()} after {@link #visitEnd()}.
 */
public class ExportsWriter extends ExportsVisitor implements VectorWriter {
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
    public @Nullable Consumer<ExportsWriter> onEnd;

    /**
     * Constructs a writer with no {@link #onEnd end callback}.
     */
    public ExportsWriter() {
    }

    /**
     * Constructs a writer with an optional {@link #onEnd end callback}.
     *
     * @param onEnd A callback that is called from {@link #visitEnd()}, or {@code null}.
     */
    public ExportsWriter(@Nullable Consumer<ExportsWriter> onEnd) {
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
    public void visitExport(@NotNull String name, byte type, int index) {
        ++count;
        out.putName(name);
        out.put(type);
        out.putVarUInt(index);
    }

    @Override
    public void visitEnd() {
        if (onEnd != null) onEnd.accept(this);
    }
}
