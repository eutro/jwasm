package io.github.eutro.jwasm;

import java.util.function.Consumer;

public class FunctionsWriter extends FunctionsVisitor implements VectorWriter {
    private final ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();
    private int count;
    public Consumer<FunctionsWriter> onEnd;

    public FunctionsWriter() {
    }

    public FunctionsWriter(Consumer<FunctionsWriter> onEnd) {
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
    public void visitFunc(int type) {
        ++count;
        out.putVarUInt(type);
    }

    @Override
    public void visitEnd() {
        if (onEnd != null) onEnd.accept(this);
    }
}
