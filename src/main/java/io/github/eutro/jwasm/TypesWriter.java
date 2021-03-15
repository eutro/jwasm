package io.github.eutro.jwasm;

import java.util.function.Consumer;

import static io.github.eutro.jwasm.Opcodes.TYPES_FUNCTION;

public class TypesWriter extends TypesVisitor implements VectorWriter {
    private final ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();
    private int count;
    public Consumer<TypesWriter> onEnd;

    public TypesWriter() {
    }

    public TypesWriter(Consumer<TypesWriter> onEnd) {
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
    public void visitType(byte[] params, byte[] returns) {
        ++count;
        out.put(TYPES_FUNCTION);
        out.putByteArray(params);
        out.putByteArray(returns);
    }

    @Override
    public void visitEnd() {
        if (onEnd != null) onEnd.accept(this);
    }
}
