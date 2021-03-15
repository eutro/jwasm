package io.github.eutro.jwasm;

import java.util.function.Consumer;

public class GlobalsWriter extends GlobalsVisitor implements VectorWriter {
    private final ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();
    private int count;
    public Consumer<GlobalsWriter> onEnd;

    public GlobalsWriter() {
    }

    public GlobalsWriter(Consumer<GlobalsWriter> onEnd) {
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
    public ExprVisitor visitGlobal(byte mut, byte type) {
        ++count;
        out.put(type);
        out.put(mut);
        return new ExprWriter(out::put);
    }

    @Override
    public void visitEnd() {
        if (onEnd != null) onEnd.accept(this);
    }
}
