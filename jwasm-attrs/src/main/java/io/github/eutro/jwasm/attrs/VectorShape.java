package io.github.eutro.jwasm.attrs;

import static io.github.eutro.jwasm.Opcodes.*;

public enum VectorShape {
    I8X16(I32, 16),
    I16X8(I32, 8),
    I32X4(I32, 4),
    I64X2(I64, 2),
    F32X4(F32, 4),
    F64X2(F64, 2),
    ;

    public final byte unpacked;
    public final int dim;

    VectorShape(byte unpacked, int dim) {
        this.unpacked = unpacked;
        this.dim = dim;
    }
}
