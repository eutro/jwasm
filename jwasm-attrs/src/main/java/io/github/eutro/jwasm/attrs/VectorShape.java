package io.github.eutro.jwasm.attrs;

import static io.github.eutro.jwasm.Opcodes.*;

/**
 * The shape of a {@code v128} vector.
 */
public enum VectorShape {
    /**
     * Sixteen 8-bit integers.
     */
    I8X16(I32, 16),
    /**
     * Eight 16-bit integers.
     */
    I16X8(I32, 8),
    /**
     * Four 32-bit integers.
     */
    I32X4(I32, 4),
    /**
     * Two 64-bit integers.
     */
    I64X2(I64, 2),
    /**
     * Four 32-bit floats.
     */
    F32X4(F32, 4),
    /**
     * Two 64-bit floats.
     */
    F64X2(F64, 2),
    ;

    /**
     * The WebAssembly obtained when unpacking a vector of this shape.
     */
    public final byte unpacked;
    /**
     * The number of lanes in this shape.
     */
    public final int dim;

    VectorShape(byte unpacked, int dim) {
        this.unpacked = unpacked;
        this.dim = dim;
    }
}
