package io.github.eutro.jwasm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A {@link CodesVisitor} that generates the corresponding WebAssembly bytecode as it is visited.
 * This can be retrieved using {@link #toByteArray()} after {@link #visitEnd()}.
 */
public class CodesWriter extends CodesVisitor implements VectorWriter {
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
    public @Nullable Consumer<CodesWriter> onEnd;

    /**
     * Constructs a writer with no {@link #onEnd end callback}.
     */
    public CodesWriter() {
    }

    /**
     * Constructs a writer with an optional {@link #onEnd end callback}.
     *
     * @param onEnd A callback that is called from {@link #visitEnd()}, or {@code null}.
     */
    public CodesWriter(@Nullable Consumer<CodesWriter> onEnd) {
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
    public @Nullable ExprVisitor visitCode(byte @NotNull [] locals) {
        ++count;
        ByteOutputStream.BaosByteOutputStream fout = new ByteOutputStream.BaosByteOutputStream();

        if (locals.length == 0) {
            fout.putVarUInt(0);
        } else {
            ByteOutputStream.BaosByteOutputStream lout = new ByteOutputStream.BaosByteOutputStream();
            int compressedCount = 1;
            byte last = locals[0];
            int lastc = 1;
            for (int i = 1; i < locals.length; i++) {
                if (locals[i] == last) {
                    ++lastc;
                } else {
                    ++compressedCount;
                    lout.putVarUInt(lastc);
                    lout.put(last);
                    last = locals[i];
                    lastc = 1;
                }
            }
            lout.putVarUInt(lastc);
            lout.put(last);
            fout.putVarUInt(compressedCount);
            fout.put(lout.toByteArray());
        }

        return new ExprWriter(b -> {
            fout.put(b);
            byte[] fbytes = fout.toByteArray();
            out.putVarUInt(fbytes.length);
            out.put(fbytes);
        });
    }

    @Override
    public void visitEnd() {
        if (onEnd != null) onEnd.accept(this);
    }
}
