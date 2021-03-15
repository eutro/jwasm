package io.github.eutro.jwasm;

public interface VectorWriter extends ByteArrayConvertible {
    @Override
    default byte[] toByteArray() {
        ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();
        out.putVarUInt(count());
        out.put(raw());
        return out.toByteArray();
    }

    int count();

    byte[] raw();
}
