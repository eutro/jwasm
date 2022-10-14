package io.github.eutro.jwasm.sexp;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

public class Writer {
    private final PrintStream ps;

    public Writer(PrintStream ps) {
        this.ps = ps;
    }

    public static String writeToString(Object obj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new Writer(new PrintStream(baos)).write(obj);
        return baos.toString();
    }

    public void write(Object obj) {
        if (obj instanceof List) write((List<?>) obj);
        else if (obj instanceof byte[]) write((byte[]) obj);
        else ps.print(obj);
    }

    public void write(List<?> obj) {
        ps.write('(');
        Iterator<?> iter = obj.iterator();
        while (iter.hasNext()) {
            write(iter.next());
            if (iter.hasNext()) ps.write(' ');
        }
        ps.write(')');
    }

    public void write(byte[] bytes) {
        ps.write('"');
        for (byte b : bytes) {
            if (' ' <= b && b < 0x7F) ps.write(b);
            else {
                ps.printf("\\%02X", b);
            }
        }
        ps.write('"');
    }
}
