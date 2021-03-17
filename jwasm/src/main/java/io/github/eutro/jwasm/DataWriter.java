package io.github.eutro.jwasm;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A {@link DataVisitor} that generates the corresponding WebAssembly bytecode as it is visited.
 * This can be retrieved using {@link #toByteArray()} after {@link #visitEnd()}.
 */
public class DataWriter extends DataVisitor implements ByteArrayConvertible {
    /**
     * The {@link ByteOutputStream} that this visitor will write raw bytes to.
     */
    private final ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();

    /**
     * Whether {@link #visitActive(int)} has been called.
     */
    private boolean active = false;

    /**
     * A callback that is called from {@link #visitEnd()}, or {@code null}.
     */
    public Consumer<byte[]> onEnd;

    /**
     * Constructs a writer with no {@link #onEnd end callback}.
     */
    public DataWriter() {
    }

    /**
     * Constructs a writer with an optional {@link #onEnd end callback}.
     *
     * @param onEnd A callback that is called from {@link #visitEnd()}, or {@code null}.
     */
    public DataWriter(Consumer<byte[]> onEnd) {
        this.onEnd = onEnd;
    }

    @Override
    public byte[] toByteArray() {
        return out.toByteArray();
    }

    @Override
    public @Nullable ExprVisitor visitActive(int memory) {
        active = true;
        if (memory == 0) {
            out.put((byte) 0x00);
        } else {
            out.put((byte) Opcodes.DATA_EXPLICIT);
            out.putVarUInt(memory);
        }
        return new ExprWriter(out::put);
    }

    @Override
    public void visitInit(byte[] init) {
        if (!active) out.put((byte) Opcodes.DATA_PASSIVE);
        out.putByteArray(init);
    }

    @Override
    public void visitEnd() {
        if (onEnd != null) onEnd.accept(toByteArray());
    }
}
