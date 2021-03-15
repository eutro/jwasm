package io.github.eutro.jwasm;

import java.util.function.Consumer;

public class DataWriter extends DataVisitor implements ByteArrayConvertible {
    private final ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();
    private boolean active = false;
    public Consumer<byte[]> onEnd;

    public DataWriter() {
    }

    public DataWriter(Consumer<byte[]> onEnd) {
        this.onEnd = onEnd;
    }

    @Override
    public byte[] toByteArray() {
        return out.toByteArray();
    }

    @Override
    public ExprVisitor visitActive(int memory) {
        active = true;
        if (memory == 0) {
            out.put((byte) 0x00);
        } else {
            out.put((byte) Opcodes.DATA_EXPLICIT);
            out.putVarUInt(memory);
        }
        return new ExprWriter(out::put);
    }

    @Override
    public void visitInit(byte[] init) {
        if (!active) out.put((byte) Opcodes.DATA_PASSIVE);
        out.putByteArray(init);
    }

    @Override
    public void visitEnd() {
        if (onEnd != null) onEnd.accept(toByteArray());
    }
}
