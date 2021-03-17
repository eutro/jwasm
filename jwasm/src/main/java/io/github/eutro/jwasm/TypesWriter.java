package io.github.eutro.jwasm;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

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
    public void visitFuncType(byte @NotNull [] params, byte @NotNull [] returns) {
        ++count;
        out.put(Opcodes.TYPES_FUNCTION);
        out.putByteArray(params);
        out.putByteArray(returns);
    }

    @Override
    public void visitEnd() {
        if (onEnd != null) onEnd.accept(this);
    }
}
