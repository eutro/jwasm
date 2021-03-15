package io.github.eutro.jwasm;

import java.util.function.Consumer;

public class ExportsWriter extends ExportsVisitor implements VectorWriter {
    private final ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();
    private int count;
    public Consumer<ExportsWriter> onEnd;

    public ExportsWriter() {
    }

    public ExportsWriter(Consumer<ExportsWriter> onEnd) {
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
    public void visitExport(String name, byte type, int index) {
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
