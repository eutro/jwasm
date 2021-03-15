package io.github.eutro.jwasm;

import java.util.function.Consumer;

public class TablesWriter extends TablesVisitor implements VectorWriter {
    private final ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();
    private int count;
    public Consumer<TablesWriter> onEnd;

    public TablesWriter() {
    }

    public TablesWriter(Consumer<TablesWriter> onEnd) {
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
    public void visitTable(int min, Integer max, byte type) {
        ++count;
        out.put(type);
        out.putLimit(min, max);
    }

    @Override
    public void visitEnd() {
        if (onEnd != null) onEnd.accept(this);
    }
}