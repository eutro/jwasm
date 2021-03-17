package io.github.eutro.jwasm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class CodesWriter extends CodesVisitor implements VectorWriter {
    private final ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();
    private int count;
    public Consumer<CodesWriter> onEnd;

    public CodesWriter() {
    }

    public CodesWriter(Consumer<CodesWriter> onEnd) {
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
