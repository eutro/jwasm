package io.github.eutro.jwasm;

import java.util.function.Consumer;

public class DataSegmentsWriter extends DataSegmentsVisitor implements VectorWriter {
    private final ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();
    private int count;
    public Consumer<DataSegmentsWriter> onEnd;

    public DataSegmentsWriter() {
    }

    public DataSegmentsWriter(Consumer<DataSegmentsWriter> onEnd) {
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
    public DataVisitor visitData() {
        ++count;
        return new DataWriter(out::put);
    }

    @Override
    public void visitEnd() {
        if (onEnd != null) onEnd.accept(this);
    }
}
