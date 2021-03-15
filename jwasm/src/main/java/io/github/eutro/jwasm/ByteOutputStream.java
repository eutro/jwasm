package io.github.eutro.jwasm;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * An interface for writing WebAssembly bytecode, with methods for writing common WebAssembly types,
 * such as LEB128 encoded numbers.
 *
 * @param <E> The exception that may be thrown by writes.
 */
public interface ByteOutputStream<E extends Exception> {

    /**
     * A dummy {@link ByteOutputStream} whose writes do nothing.
     *
     * Can be used to predict how many bytes a given write will take.
     */
    ByteOutputStream<RuntimeException> DUMMY = new Dummy();

    /**
     * Write a byte to the stream.
     *
     * @param b The byte to write.
     * @throws E If a write error occurs.
     */
    void put(byte b) throws E;

    /**
     * Write all the bytes from an array to the stream.
     *
     * @param bytes The bytes to write.
     * @throws E If a write error occurs.
     */
    default void put(byte[] bytes) throws E {
        for (byte b : bytes) {
            put(b);
        }
    }

    /**
     * Write an unsigned 32 bit integer to the stream, in little endian byte order.
     *
     * @param v The integer to write.
     * @throws E If a write error occurs.
     */
    default void putUInt32(int v) throws E {
        put((byte) (v & 0x000000FF));
        put((byte) ((v & 0x0000FF00) >> 8));
        put((byte) ((v & 0x00FF0000) >> 16));
        put((byte) ((v & 0xFF000000) >> 24));
    }

    /**
     * Write a 32 bit floating point value to the stream, in little endian byte order.
     *
     * @param f The value to write.
     * @throws E If a write error occurs.
     */
    default void putFloat32(float f) throws E {
        putUInt32(Float.floatToRawIntBits(f));
    }

    /**
     * Write a 64 bit floating point value to the stream, in little endian byte order.
     *
     * @param f The value to write.
     * @throws E If a write error occurs.
     */
    default void putFloat64(double f) throws E {
        long l = Double.doubleToRawLongBits(f);
        putUInt32((int) (l & 0x0000FFFF));
        putUInt32((int) ((l & 0xFFFF0000) >> 32));
    }

    /**
     * Write an unsigned LEB128 encoded integer to the stream.
     *
     * @param i The integer to write.
     * @return The number of bytes written.
     * @throws E If a write error occurs.
     */
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

    /**
     * Write a signed LEB128 encoded integer to the stream.
     *
     * @param i The integer to write.
     * @return The number of bytes written.
     * @throws E If a write error occurs.
     */
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

    /**
     * Write an array of bytes to the stream, writing its length first as an {@link #putVarUInt(long) unsigned integer},
     * followed by the raw bytes.
     *
     * @param bytes The byte array to write.
     * @throws E If a write error occurs.
     */
    default void putByteArray(byte[] bytes) throws E {
        putVarUInt(bytes.length);
        put(bytes);
    }

    /**
     * Write a WebAssembly <code>limit</code> to the stream, consisting of a minimum and an optional maximum.
     *
     * @param min The minimum.
     * @param max The maximum, or null if there is none.
     * @throws E If a write error occurs.
     */
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

    /**
     * Write a string as a WebAssembly <code>name</code>: a vector of UTF-8 bytes.
     *
     * @param name The name to write.
     * @throws E If a write error occurs.
     */
    default void putName(String name) throws E {
        putByteArray(name.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * A {@link ByteOutputStream} that writes to a {@link ByteArrayOutputStream}.
     */
    class BaosByteOutputStream implements ByteOutputStream<RuntimeException>, ByteArrayConvertible {
        private final ByteArrayOutputStream baos;

        /**
         * Construct a {@link BaosByteOutputStream} with an existing {@link ByteArrayOutputStream stream}.
         *
         * @param baos The stream to write to.
         */
        public BaosByteOutputStream(ByteArrayOutputStream baos) {
            this.baos = baos;
        }

        /**
         * Construct a {@link BaosByteOutputStream} with an empty {@link ByteArrayOutputStream stream}.
         */
        public BaosByteOutputStream() {
            this(new ByteArrayOutputStream());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public byte[] toByteArray() {
            return this.baos.toByteArray();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void put(byte b) throws RuntimeException {
            baos.write(b);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void put(byte[] bytes) throws RuntimeException {
            baos.write(bytes, 0, bytes.length);
        }
    }

    /**
     * A dummy {@link ByteOutputStream} that ignores writes.
     */
    class Dummy implements ByteOutputStream<RuntimeException> {
        /**
         * {@inheritDoc}
         */
        @Override
        public void put(byte b) throws RuntimeException {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void put(byte[] bytes) throws RuntimeException {
        }
    }
}
