package io.github.eutro.jwasm;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public interface ByteOutputStream<E extends Exception> {

    ByteOutputStream<RuntimeException> DUMMY = new Dummy();

    void put(byte b) throws E;

    default void put(byte[] bytes) throws E {
        for (byte b : bytes) {
            put(b);
        }
    }

    default void putUInt32(int v) throws E {
        put((byte) (v & 0x000000FF));
        put((byte) ((v & 0x0000FF00) >> 8));
        put((byte) ((v & 0x00FF0000) >> 16));
        put((byte) ((v & 0xFF000000) >> 24));
    }

    default void putFloat32(float f) throws E {
        putUInt32(Float.floatToRawIntBits(f));
    }

    default void putFloat64(double f) throws E {
        long l = Double.doubleToRawLongBits(f);
        putUInt32((int) (l & 0x0000FFFF));
        putUInt32((int) ((l & 0xFFFF0000) >> 32));
    }

    default int putVarUInt(long i) throws E {
        int written = 1;
        long next = i >>> 7;
        while (next != 0) {
            put((byte) (i & 0x7F | 0x80));
            ++written;
            i = next;
            next >>>= 7;
        }
        put((byte) (i & 0x7F));
        return written;
    }

    default int putVarSInt(long i) throws E {
        int written = 0;
        boolean more;
        boolean neg = i < 0;
        long end = neg ? -1 : 0;
        do {
            byte b = (byte) (i & 0x7F);
            i >>= 7;
            more = i != end || neg == ((b & 0x40) == 0);
            if (more) b |= 0x80;
            put(b);
            ++written;
        } while (more);
        return written;
    }

    default void putByteArray(byte[] bytes) throws E {
        putVarUInt(bytes.length);
        put(bytes);
    }

    default void putLimit(int min, Integer max) throws E {
        if (max == null) {
            put(Opcodes.LIMIT_NOMAX);
            putVarUInt(min);
        } else {
            put(Opcodes.LIMIT_WMAX);
            putVarUInt(min);
            putVarUInt(max);
        }
    }

    default void putName(String name) throws E {
        putByteArray(name.getBytes(StandardCharsets.UTF_8));
    }

    class BaosByteOutputStream implements ByteOutputStream<RuntimeException>, ByteArrayConvertible {
        public final ByteArrayOutputStream baos;

        public BaosByteOutputStream(ByteArrayOutputStream baos) {
            this.baos = baos;
        }

        public BaosByteOutputStream() {
            this(new ByteArrayOutputStream());
        }

        @Override
        public byte[] toByteArray() {
            return this.baos.toByteArray();
        }

        @Override
        public void put(byte b) throws RuntimeException {
            baos.write(b);
        }

        @Override
        public void put(byte[] bytes) throws RuntimeException {
            baos.write(bytes, 0, bytes.length);
        }
    }

    class Dummy implements ByteOutputStream<RuntimeException> {
        @Override
        public void put(byte b) throws RuntimeException {
        }

        @Override
        public void put(byte[] bytes) throws RuntimeException {
        }
    }
}
