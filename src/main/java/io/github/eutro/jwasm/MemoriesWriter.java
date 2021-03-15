package io.github.eutro.jwasm;

import java.util.function.Consumer;

public class MemoriesWriter extends MemoriesVisitor implements VectorWriter {
    private final ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();
    private int count;
    public Consumer<MemoriesWriter> onEnd;

    public MemoriesWriter() {
    }

    public MemoriesWriter(Consumer<MemoriesWriter> onEnd) {
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