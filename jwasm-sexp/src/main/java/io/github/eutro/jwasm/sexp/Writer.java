package io.github.eutro.jwasm.sexp;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

/**
 * A class for writing s-expressions.
 *
 * @see Reader
 */
public class Writer {
    private final PrintStream ps;

    /**
     * Construct a new {@link Writer} that writes to the given {@link PrintStream}.
     *
     * @param ps The stream to write to.
     */
    public Writer(PrintStream ps) {
        this.ps = ps;
    }

    /**
     * Convert an object to a string, as if by {@link #write(Object)}.
     *
     * @param obj The object to write.
     * @return The written string.
     */
    public static String writeToString(Object obj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new Writer(new PrintStream(baos)).write(obj);
        return baos.toString();
    }

    /**
     * Write an object to the stream.
     * <p>
     * The object will be written as an s-expression, as read by {@link Reader}.
     * <p>
     * Note that not all objects can be {@link Reader#readAll(CharSequence) read} back after writing,
     * only those that {@link Reader read} supports. Notably, anything returned by {@link Reader#readAll(CharSequence)}
     * can be written back as something that can then be read as something equivalent.
     *
     * @param obj The object to write.
     */
    public void write(Object obj) {
        if (obj instanceof List) write((List<?>) obj);
        else if (obj instanceof byte[]) write((byte[]) obj);
        else ps.print(obj);
    }

    private void write(List<?> obj) {
        ps.write('(');
        Iterator<?> iter = obj.iterator();
        while (iter.hasNext()) {
            write(iter.next());
            if (iter.hasNext()) ps.write(' ');
        }
        ps.write(')');
    }

    private void write(byte[] bytes) {
        ps.write('"');
        for (byte b : bytes) {
            if (' ' <= b && b < 0x7F
                    && b != '"'
                    && b != '\\') ps.write(b);
            else {
                ps.printf("\\%02X", b);
            }
        }
        ps.write('"');
    }
}
