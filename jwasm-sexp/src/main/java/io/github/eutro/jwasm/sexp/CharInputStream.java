package io.github.eutro.jwasm.sexp;

import io.github.eutro.jwasm.ValidationException;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * An interface for character streams, yielding by unicode code point.
 *
 * @param <E> The exception that may be thrown by reads.
 */
public interface CharInputStream<E extends Throwable> {
    /**
     * Returns the next character in the stream.
     *
     * @return The Unicode code point of the next character, or -1 if the end of the stream has been reached.
     * @throws E If a read error occured.
     */
    int get() throws E;

    abstract class UTF16AdapterCharInputStream<E extends Throwable> implements CharInputStream<E> {
        protected abstract int nextChar() throws E;

        @Override
        public int get() throws E {
            int c1 = nextChar();
            if (c1 == -1) return -1;
            int codePoint;
            if (Character.isHighSurrogate((char) c1)) {
                int c2 = nextChar();
                if (c2 == -1) {
                    throw new ValidationException(
                            String.format("unmatched surrogate pair (0x%04x) in input stream", c1)
                    );
                }
                codePoint = Character.toCodePoint((char) c1, (char) c2);
            } else {
                if (Character.isLowSurrogate((char) c1)) {
                    throw new ValidationException("code point is not a scalar, but a low surrogate");
                }
                codePoint = c1;
            }
            if (!Character.isValidCodePoint(codePoint)) {
                throw new ValidationException(String.format("value (0x%08x) is not a valid code point", codePoint));
            }
            return codePoint;
        }
    }

    class ReaderCharInputStream extends UTF16AdapterCharInputStream<IOException> {
        private final Reader reader;

        public ReaderCharInputStream(Reader reader) {
            this.reader = reader;
        }

        @Override
        protected int nextChar() throws IOException {
            return reader.read();
        }
    }

    class StringCharInputStream extends UTF16AdapterCharInputStream<RuntimeException> {
        private final String string;
        private int next = 0;
        private final int length;

        public StringCharInputStream(String string) {
            this.string = string;
            length = string.length();
        }

        @Override
        protected int nextChar() throws RuntimeException {
            if (next >= length) return -1;
            return string.charAt(next++);
        }
    }

    class CharBufferCharInputStream extends UTF16AdapterCharInputStream<RuntimeException> {
        private final CharBuffer buf;

        public CharBufferCharInputStream(CharBuffer buf) {
            this.buf = buf;
        }

        @Override
        protected int nextChar() throws RuntimeException {
            if (!buf.hasRemaining()) return -1;
            return buf.get();
        }
    }
}
