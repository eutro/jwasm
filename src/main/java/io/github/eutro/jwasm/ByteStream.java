package io.github.eutro.jwasm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import static io.github.eutro.jwasm.Opcodes.*;

public interface ByteStream<E extends Exception> {

    int get() throws E;

    default int get(byte[] buf, int offset, int len) throws E {
        if (offset < 0 || len <= offset || len + offset >= buf.length) throw new IndexOutOfBoundsException();
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

    default byte expect() throws E {
        int v = get();
        if (v == -1) throw new ValidationException("Unexpected EOF");
        return (byte) v;
    }

    default void expectEmpty() throws E {
        if (get() != -1) {
            throw new ValidationException("Expected less bytes");
        }
    }

    default int skip(int count) throws E {
        for (int i = 0; i < count; i++) {
            if (get() == -1) return i;
        }
        return count;
    }

    default void skipAll() throws E {
        skip(Integer.MAX_VALUE);
    }

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

    default float getFloat32() throws E {
        return Float.intBitsToFloat(getUInt32());
    }

    default double getFloat64() throws E {
        return Double.longBitsToDouble((long) getUInt32() << 32 | getUInt32());
    }

    default int getVarUInt32() throws E {
        return (int) getVarUIntX(5);
    }

    default long getVarUIntX(int bytes) throws E {
        long v = 0;
        int count = 0;
        for (; count < bytes; ++count) {
            byte b = expect();
            v |= (long) (b & 0x7F) << (count * 7);
            if ((b & 0x80) == 0) return v;
        }
        throw new ValidationException(String.format("varuint: 0x%02x... exceded %d bytes", v, bytes));
    }

    default int getVarSInt32() throws E {
        return (int) getVarSIntX(5);
    }

    default long getVarSInt64() throws E {
        return getVarSIntX(10);
    }

    default long getVarSIntX(int bytes) throws E {
        return getVarSIntX0(bytes, 0, 0);
    }

    default long getVarSIntX0(int bytes, long v, int count) throws E {
        byte b;
        long signBits = -1;
        loop:
        {
            for (; count < bytes; ++count) {
                b = expect();
                v |= (long) (b & 0x7F) << (count * 7);
                signBits <<= 7;
                if ((b & 0x80) == 0) break loop;
            }
            throw new ValidationException(String.format("varsint: 0x%02x... exceded %d bytes", v, bytes));
        }
        if (((signBits >> 1) & v) != 0) {
            v |= signBits;
        }
        return v;
    }

    default int getBlockType() throws E {
        byte first = expect();
        switch (first) {
            case EMPTY_TYPE:
            case I32:
            case I64:
            case F32:
            case F64:
            case FUNCREF:
            case EXTERNREF:
                return first;
            default:
                if ((first & 0x80) == 0)
                    throw new ValidationException(String.format("Unrecognised type 0x%02x", first));
                long x = getVarSIntX0(5, first & 0x7F, 1);
                if (x < 0) throw new ValidationException("Negative type index");
                return (int) x;
        }
    }

    default byte[] getByteArray() throws E {
        int size = getVarUInt32();
        byte[] ret = new byte[size];
        if (get(ret, 0, size) < size) throw new ValidationException("Unexpected EOF");
        return ret;
    }

    default String getName() throws E {
        return new String(getByteArray(), StandardCharsets.UTF_8);
    }

    default int[] getLimit() throws E {
        int min, max;
        byte type = expect();
        switch (type) {
            case Opcodes.LIMIT_NOMAX:
                min = getVarUInt32();
                max = 0;
                break;
            case Opcodes.LIMIT_WMAX:
                min = getVarUInt32();
                max = getVarUInt32();
                break;
            default:
                throw new ValidationException(String.format("Unrecognised limit type 0x%02x", type));
        }
        return new int[] { min, max };
    }

    default ByteStream<E> sectionBuffer() throws E {
        return sectionBuffer(getVarUInt32());
    }

    default ByteStream<E> sectionBuffer(int length) {
        ByteStream<E> us = this;
        return new ByteStream<E>() {
            int gotten = 0;

            @Override
            public int get() throws E {
                if (gotten >= length) return -1;
                ++gotten;
                return us.get();
            }

            @Override
            public int get(byte[] buf, int offset, int len) throws E {
                if (len == 0) return 0;
                int remaining = length - gotten;
                if (remaining == 0) return -1;
                int read = us.get(buf, offset, Math.min(remaining, len));
                gotten += read;
                return read;
            }

            @Override
            public int skip(int count) throws E {
                int skipped = us.skip(Math.min(length - gotten, count));
                gotten += skipped;
                return skipped;
            }

            @Override
            public void skipAll() throws E {
                gotten += us.skip(length - gotten);
                if (gotten < length) {
                    throw new ValidationException("Not enough bytes in section");
                }
            }
        };
    }

    class ByteBufferByteStream implements ByteStream<RuntimeException> {
        private final ByteBuffer bb;

        public ByteBufferByteStream(ByteBuffer bb) {
            if (bb.order() != ByteOrder.LITTLE_ENDIAN) throw new IllegalArgumentException();
            this.bb = bb;
        }

        @Override
        public int get() {
            return bb.hasRemaining() ? bb.get() : -1;
        }

        @Override
        public int get(byte[] buf, int offset, int len) {
            if (len == 0) return 0;
            int toGet = Math.min(len, bb.remaining());
            if (toGet == 0) return -1;
            bb.get(buf, offset, toGet);
            return toGet;
        }

        @Override
        public int skip(int count) throws RuntimeException {
            int skipped = bb.position() + Math.min(count, bb.remaining());
            bb.position(skipped);
            return skipped;
        }
    }

    class InputStreamByteStream implements ByteStream<IOException> {
        private final InputStream is;

        public InputStreamByteStream(InputStream is) {
            this.is = is;
        }

        @Override
        public int get() throws IOException {
            return is.read();
        }

        @Override
        public int get(byte[] buf, int offset, int len) throws IOException {
            return is.read(buf, offset, len);
        }

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
