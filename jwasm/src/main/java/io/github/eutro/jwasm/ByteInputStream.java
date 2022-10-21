package io.github.eutro.jwasm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

import static io.github.eutro.jwasm.Opcodes.*;

/**
 * An interface for WebAssembly bytecode sources, with methods for reading common WebAssembly types,
 * such as LEB128 encoded numbers.
 *
 * @param <E> The exception that may be thrown by reads.
 */
public interface ByteInputStream<E extends Exception> {

    /**
     * Get a single unsigned byte from the stream.
     *
     * @return An unsigned byte b ({@code 0x00 &lt;= b &lt;= 0xFF}), or -1 if the end of the stream has been reached.
     * @throws E If a read error occurred.
     */
    int get() throws E;

    /**
     * Get bytes in bulk from the stream.
     * <p>
     * Defaults to repeatedly getting bytes with {@link #get()}.
     *
     * @param buf    The byte array to copy to.
     * @param offset The offset in buf to start writing at.
     * @param len    The length of bytes to copy at most.
     * @return The number of bytes gotten, or -1 if the end of stream had been reached.
     * @throws E                         If a read error occurred.
     * @throws IndexOutOfBoundsException If {@code offset</code> is negative, or <code>len + offset}
     *                                   exceeds {@code buf.length}.
     */
    default int get(byte[] buf, int offset, int len) throws E {
        if (offset < 0 || len + offset >= buf.length) throw new IndexOutOfBoundsException();
        int b = get();
        if (b == -1) return -1;
        int written = 1;
        buf[offset] = (byte) b;
        for (int i = offset + 1; i < len; i++) {
            b = get();
            if (b == -1) return written;
            buf[i] = (byte) b;
            ++written;
        }
        return written;
    }

    /**
     * Skip a number of bytes from the stream, terminating early if the end of the stream is reached.
     * <p>
     * Defaults to repeatedly getting bytes with {@link #get()}.
     *
     * @param count The number of bytes to skip.
     * @return How many bytes were skipped.
     * @throws E If a read error occurred.
     */
    default int skip(int count) throws E {
        for (int i = 0; i < count; i++) {
            if (get() == -1) return i;
        }
        return count;
    }

    /**
     * Skip all the remaining bytes in the stream.
     * <p>
     * Defaults to calling {@link #skip(int)} with {@link Integer#MAX_VALUE}.
     *
     * @throws E If a read error occurred.
     */
    default void skipAll() throws E {
        skip(Integer.MAX_VALUE);
    }

    /**
     * Get a byte from the stream, throwing a {@link ValidationException} if the end of the stream was reached.
     *
     * @return The byte that was read.
     * @throws E                   If a read error occurred.
     * @throws ValidationException If the end of the stream was reached.
     */
    default byte expect() throws E {
        int v = get();
        if (v == -1) throw new ValidationException("Unexpected end of input",
                new RuntimeException("unexpected end"));
        return (byte) v;
    }

    /**
     * Expect the stream to be empty, throwing a {@link ValidationException} if it is not.
     *
     * @throws E                   If a read error occurred.
     * @throws ValidationException If the end of the had not been reached.
     */
    default void expectEmpty() throws E {
        if (get() != -1) {
            throw new ValidationException("Expected less bytes");
        }
    }

    /**
     * Gets a 32-bit unsigned integer from the stream, in little endian byte order, throwing an exception
     * if there aren't enough bytes in the stream.
     *
     * @return The unsigned integer interpreted from the next 4 bytes in the stream.
     * @throws E                   If a read error occurred.
     * @throws ValidationException If there are less than 4 bytes left in the stream.
     */
    default int getUInt32() throws E {
        int a = get();
        int b = get();
        int c = get();
        int d = expect();
        return d << 24 |
                c << 16 |
                b << 8 |
                a;
    }

    /**
     * Gets a 32 bit floating point value from the stream, in little endian byte order, throwing an exception
     * if there aren't enough bytes in the stream.
     *
     * @return The floating point value interpreted from the next 4 bytes in the stream.
     * @throws E                   If a read error occurred.
     * @throws ValidationException If there are less than 4 bytes left in the stream.
     */
    default float getFloat32() throws E {
        return Float.intBitsToFloat(getUInt32());
    }

    /**
     * Gets a 64 bit floating point value from the stream, in little endian byte order, throwing an exception
     * if there aren't enough bytes in the stream.
     *
     * @return The floating point value interpreted from the next 8 bytes in the stream.
     * @throws E                   If a read error occurred.
     * @throws ValidationException If there are less than 8 bytes left in the stream.
     */
    default double getFloat64() throws E {
        return Double.longBitsToDouble(getUInt32() | (long) getUInt32() << 32);
    }

    /**
     * Gets an LEB128 encoded unsigned integer from the stream, throwing an exception if the stream ends before
     * the full integer is read, or if the number exceeds 5 bytes.
     *
     * @return The integer that was read.
     * @throws E                   If a read error occurred.
     * @throws ValidationException If the stream ends before the number is fully read.
     * @throws ValidationException If the number exceeds 5 bytes.
     */
    default int getVarUInt32() throws E {
        long uInt35 = getVarUIntX(5);
        if (uInt35 > Integer.toUnsignedLong(-1)) {
            throw new ValidationException("Integer out of range for u32",
                    new RuntimeException("integer too large"));
        }
        return (int) uInt35;
    }

    /**
     * Gets an LEB128 encoded unsigned integer from the stream, throwing an exception if the stream ends before
     * the full integer is read, or if the number exceeds {@code bytes} bytes.
     *
     * @param bytes The maximum number of bytes to read.
     * @return The integer that was read.
     * @throws E                   If a read error occurred.
     * @throws ValidationException If the stream ends before the number is fully read.
     * @throws ValidationException If the number exceeds {@code bytes} bytes.
     */
    default long getVarUIntX(int bytes) throws E {
        final int MAX_NON_OVERFLOW_BYTES = Long.SIZE / 7;
        long v = 0;
        int count = 0;
        int maxCount = Integer.min(bytes, MAX_NON_OVERFLOW_BYTES);
        for (; count < maxCount; ++count) {
            byte b = expect();
            v |= (long) (b & 0x7F) << (count * 7);
            if ((b & 0x80) == 0) return v;
        }
        if (bytes > MAX_NON_OVERFLOW_BYTES) more:{
            if (bytes > MAX_NON_OVERFLOW_BYTES + 1) throw new IllegalArgumentException();
            byte b = expect();
            if ((b & 0x80) != 0) break more; // error with too long
            // error if it would overflow the long (imagine we are reading an u1)
            if (b >= 2) {
                throw new ValidationException("Integer out of range for u64",
                        new RuntimeException("integer too large"));
            }
            v |= (long) (b & 0x7F) << (count * 7);
        }
        throw new ValidationException(String.format("VarUInt: 0x%02x... exceeded %d bytes", v, bytes),
                new RuntimeException("integer representation too long"));
    }

    /**
     * Gets an LEB128 encoded signed integer from the stream, throwing an exception if the stream ends before
     * the full integer is read, or if the number exceeds 5 bytes.
     *
     * @return The integer that was read.
     * @throws E                   If a read error occurred.
     * @throws ValidationException If the stream ends before the number is fully read.
     * @throws ValidationException If the number exceeds 5 bytes.
     */
    default int getVarSInt32() throws E {
        long sInt35 = getVarSIntX(5);
        if (sInt35 < Integer.MIN_VALUE || sInt35 > Integer.MAX_VALUE) {
            throw new ValidationException("Integer out of range for s32",
                    new RuntimeException("integer too large"));
        }
        return (int) sInt35;
    }

    /**
     * Gets an LEB128 encoded signed integer from the stream, throwing an exception if the stream ends before
     * the full integer is read, or if the number exceeds 10 bytes.
     *
     * @return The integer that was read.
     * @throws E                   If a read error occurred.
     * @throws ValidationException If the stream ends before the number is fully read.
     * @throws ValidationException If the number exceeds 10 bytes.
     */
    default long getVarSInt64() throws E {
        return getVarSIntX(10);
    }

    /**
     * Gets an LEB128 encoded signed integer from the stream, throwing an exception if the stream ends before
     * the full integer is read, or if the number exceeds {@code bytes} bytes.
     *
     * @param bytes The maximum number of bytes to read.
     * @return The integer that was read.
     * @throws E                   If a read error occurred.
     * @throws ValidationException If the stream ends before the number is fully read.
     * @throws ValidationException If the number exceeds {@code bytes} bytes.
     */
    default long getVarSIntX(int bytes) throws E {
        return getVarSIntX0(bytes, 0, 0);
    }


    /**
     * Gets the rest of an LEB128 encoded signed integer from the stream,
     * throwing an exception if the stream ends before the full integer is read,
     * or if the number exceeds {@code bytes} bytes.
     * <p>
     * This may be used if part of the number is already read before this is called.
     *
     * @param bytes The maximum number of bytes to read.
     * @param v     The LEB128 encoded signed integer that has been read so far.
     * @param count How many bytes have already been read.
     * @return The integer that was read.
     * @throws E                   If a read error occurred.
     * @throws ValidationException If the stream ends before the number is fully read.
     * @throws ValidationException If the number exceeds {@code bytes} bytes.
     */
    default long getVarSIntX0(int bytes, long v, int count) throws E {
        final int MAX_NON_OVERFLOW_BYTES = Long.SIZE / 7;
        byte b;
        long signBits = -1;
        int maxCount = Integer.min(bytes, MAX_NON_OVERFLOW_BYTES);
        loop:
        {
            for (; count < maxCount; ++count) {
                b = expect();
                v |= (long) (b & 0x7F) << (count * 7);
                signBits <<= 7;
                if ((b & 0x80) == 0) break loop;
            }
            if (bytes > MAX_NON_OVERFLOW_BYTES) more:{
                if (bytes > MAX_NON_OVERFLOW_BYTES + 1) throw new IllegalArgumentException();
                b = expect();
                if ((b & 0x80) != 0) break more; // error with too long
                // error if it would overflow the long (imagine we are reading a s1)
                boolean isSigned = (1 << 6) <= b;
                if (isSigned
                        ? b < (1 << 7) - 1
                        : b >= 1) {
                    throw new ValidationException("Integer out of range for s64",
                            new RuntimeException("integer too large"));
                }
                v |= (long) (b & 0x7F) << (count * 7);
                signBits <<= 7;
                break loop;
            }
            throw new ValidationException(String.format("VarSInt: 0x%02x... exceeded %d bytes", v, bytes),
                    new RuntimeException("integer representation too long"));
        }
        if (((signBits >> 1) & v) != 0) {
            v |= signBits;
        }
        return v;
    }

    /**
     * Get a WebAssembly {@code blocktype} from the stream, which may be 0x40, a {@code valtype} or
     * a positive signed 33 bit integer type index.
     *
     * @return The blocktype that was read.
     * @throws E If a read error occurred.
     */
    default BlockType getBlockType() throws E {
        byte first = expect();
        switch (first) {
            case EMPTY_TYPE:
            case I32:
            case I64:
            case F32:
            case F64:
            case FUNCREF:
            case EXTERNREF:
                return BlockType.valtype(first);
            default:
                if ((first & 0x80) == 0) return BlockType.functype(first);
                long x = getVarSIntX0(5, first & 0x7F, 1);
                if (x < 0) throw new ValidationException("Negative type index");
                return BlockType.functype((int) x);
        }
    }

    /**
     * Read a vector of bytes from the stream.
     * <p>
     * This reads a {@link #getVarSInt32() size} followed immediately by {@code size} bytes.
     *
     * @return The byte array that was read.
     * @throws E                   If a read error occurred.
     * @throws ValidationException If there aren't {@code size} bytes to read
     *                             after {@code size} itself is read.
     */
    default byte[] getByteArray() throws E {
        int size = getVarUInt32();
        byte[] ret = new byte[size];
        if (get(ret, 0, size) < size) {
            throw new ValidationException("Unexpected end of input",
                    new RuntimeException("unexpected end"));
        }
        return ret;
    }

    /**
     * Read a UTF-8 string (WebAssembly {@code name}) from the stream.
     * <p>
     * This reads a {@link #getByteArray() byte array} and interprets it as a string of UTF-8 characters.
     *
     * @return The string that was read.
     * @throws E                   If a read error occurred.
     * @throws ValidationException If the bytes aren't valid UTF-8.
     */
    default String getName() throws E {
        return decodeName(getByteArray());
    }

    static String decodeName(byte[] bytes) {
        try {
            return StandardCharsets.UTF_8
                    .newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException e) {
            ValidationException ve = new ValidationException("Invalid UTF-8 bytes in name",
                    new RuntimeException("malformed UTF-8 encoding"));
            ve.addSuppressed(e);
            throw ve;
        }
    }

    /**
     * Read a WebAssembly {@code limit} from the stream, which is a minimum and an optional maximum.
     *
     * @return A {@link Limits} value read from the stream.
     * @throws E                   If a read error occurred.
     * @throws ValidationException If the limit type wasn't recognised.
     */
    default Limits getLimits() throws E {
        byte type = expect();
        switch (type) {
            case Opcodes.LIMIT_NOMAX:
                return new Limits(getVarUInt32(), null);
            case Opcodes.LIMIT_WMAX:
                return new Limits(getVarUInt32(), getVarUInt32());
            default:
                throw new ValidationException(String.format("Unrecognised limit type 0x%02x", type),
                        new RuntimeException("malformed limit"));
        }
    }

    /**
     * Get a view of this stream from which {@link #getVarUInt32()} bytes of this stream can be read.
     *
     * @return The new stream.
     * @throws E If a read error occurred.
     * @see #sectionStream(int)
     */
    default ByteInputStream<E> sectionStream() throws E {
        return sectionStream(getVarUInt32());
    }

    /**
     * Get a view of this stream from which {@code length} bytes of this stream can be read.
     * <p>
     * Reading bytes from the returned stream will read bytes from the original stream.
     * <p>
     * Reading beyond this limit will report that the end of the stream has been reached.
     *
     * @param length How many bytes the stream should see.
     * @return The new stream.
     */
    default ByteInputStream<E> sectionStream(int length) {
        return new SectionInputStream<>(this, length);
    }

    /**
     * A section of a stream, as returned by {@link #sectionStream(int)}.
     *
     * @param <E> The exception that may be thrown by gets.
     */
    class SectionInputStream<E extends Exception> implements ByteInputStream<E> {
        /**
         * The stream to be read from.
         */
        private final ByteInputStream<E> source;

        /**
         * How many bytes can be read through this stream.
         */
        private final int length;

        /**
         * How many bytes have been read through this stream.
         */
        int gotten = 0;

        /**
         * Construct a sectioned view of a stream.
         *
         * @param source The stream to read from.
         * @param length How many bytes can be read through the stream.
         */
        public SectionInputStream(ByteInputStream<E> source, int length) {
            this.source = source;
            this.length = length;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public byte expect() throws E {
            int v = get();
            if (v == -1) throw new ValidationException("Unexpected end of section",
                    new RuntimeException("unexpected end"));
            return (byte) v;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void expectEmpty() throws E {
            if (get() != -1) {
                throw new ValidationException("Expected less bytes",
                        new RuntimeException("section size mismatch"));
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int get() throws E {
            if (gotten >= length) return -1;
            ++gotten;
            return source.get();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int get(byte[] buf, int offset, int len) throws E {
            if (len == 0) return 0;
            int remaining = length - gotten;
            if (remaining == 0) return -1;
            int read = source.get(buf, offset, Math.min(remaining, len));
            if (read != -1) gotten += read;
            return read;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int skip(int count) throws E {
            int skipped = source.skip(Math.min(length - gotten, count));
            gotten += skipped;
            return skipped;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void skipAll() throws E {
            gotten += source.skip(length - gotten);
            if (gotten < length) {
                throw new ValidationException("Not enough bytes in section");
            }
        }
    }

    /**
     * A {@link ByteInputStream} that reads from a {@link ByteBuffer}.
     */
    class ByteBufferByteInputStream implements ByteInputStream<RuntimeException> {
        /**
         * The {@link ByteBuffer} to read from.
         */
        private final ByteBuffer bb;

        /**
         * Construct a {@link ByteBufferByteInputStream} from a byte buffer.
         *
         * @param bb The {@link ByteBuffer} to read from.
         */
        public ByteBufferByteInputStream(ByteBuffer bb) {
            this.bb = bb;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int get() {
            return bb.hasRemaining() ? Byte.toUnsignedInt(bb.get()) : -1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int get(byte[] buf, int offset, int len) {
            if (len == 0) return 0;
            int toGet = Math.min(len, bb.remaining());
            if (toGet == 0) return -1;
            bb.get(buf, offset, toGet);
            return toGet;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int skip(int count) throws RuntimeException {
            int skipped = bb.position() + Math.min(count, bb.remaining());
            bb.position(skipped);
            return skipped;
        }
    }

    /**
     * A {@link ByteInputStream} that reads from an {@link InputStream}.
     */
    class InputStreamByteInputStream implements ByteInputStream<IOException> {
        /**
         * The {@link InputStream} to read from.
         */
        private final InputStream is;

        /**
         * Construct a {@link InputStreamByteInputStream} from an input stream.
         *
         * @param is The {@link InputStream} to read from.
         */
        public InputStreamByteInputStream(InputStream is) {
            this.is = is;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int get() throws IOException {
            return is.read();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int get(byte[] buf, int offset, int len) throws IOException {
            int i = 0;
            while (true) {
                int read = is.read(buf, offset, len);
                if (read == -1) return i;
                i += read;
                if (read == len) return i;
                offset += read;
                len -= read;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int skip(int count) throws IOException {
            int skipped = (int) is.skip(count);
            if (skipped < count) {
                while (get() != -1) skipped++;
            }
            return skipped;
        }
    }
}
