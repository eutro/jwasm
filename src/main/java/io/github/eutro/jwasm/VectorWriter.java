package io.github.eutro.jwasm;

import io.github.eutro.jwasm.ByteOutputStream.BaosByteOutputStream;

public interface VectorWriter extends ByteArrayConvertible {
    @Override
    default byte[] toByteArray() {
        BaosByteOutputStream out = new BaosByteOutputStream();
        out.putVarUInt(count());
        out.put(raw());
        return out.toByteArray();
    }

    int count();

    byte[] raw();
}
