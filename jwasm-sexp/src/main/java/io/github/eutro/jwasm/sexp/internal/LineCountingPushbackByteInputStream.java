package io.github.eutro.jwasm.sexp.internal;

import io.github.eutro.jwasm.ByteInputStream;
import io.github.eutro.jwasm.sexp.SrcLoc;

public class LineCountingPushbackByteInputStream<E extends Exception> {
    private final ByteInputStream<E> bis;
    private boolean hasPushback = false;
    private int pushback;

    private int line = 1, col = 0, lastCol = 0;

    public int getLine() {
        return line;
    }

    public int getCol() {
        return col;
    }

    public LineCountingPushbackByteInputStream(ByteInputStream<E> bis) {
        this.bis = bis;
    }

    public long position() {
        return bis.position();
    }

    public int get() throws E {
        if (hasPushback) {
            hasPushback = false;
            return pushback;
        }
        int c = bis.get();
        if (c == '\n') {
            line++;
            lastCol = col;
            col = 0;
        } else {
            col++;
        }
        return c;
    }

    public void unread(int b) {
        if (hasPushback) throw new IllegalStateException();
        if (b == '\n') {
            line--;
            col = lastCol;
            lastCol = -1;
        } else {
            col--;
        }
        hasPushback = true;
        pushback = b;
    }

    public SrcLoc srcLoc() {
        return new SrcLoc(position(), line, col);
    }
}
