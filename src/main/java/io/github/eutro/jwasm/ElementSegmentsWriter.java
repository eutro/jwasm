package io.github.eutro.jwasm;

import java.util.function.Consumer;

public class ElementSegmentsWriter extends ElementSegmentsVisitor implements VectorWriter {
    private final ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();
    private int count;
    public Consumer<ElementSegmentsWriter> onEnd;

    public ElementSegmentsWriter() {
    }

    public ElementSegmentsWriter(Consumer<ElementSegmentsWriter> onEnd) {
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
    public ElementVisitor visitElem() {
        ++count;
        return new ElementWriter(out::put);
    }

    @Override
    public void visitEnd() {
        if (onEnd != null) onEnd.accept(this);
    }
}
