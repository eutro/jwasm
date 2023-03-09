package io.github.eutro.jwasm.sexp;

public class SrcLoc {
    public final long position;
    public final int line, col;

    public SrcLoc(long position, int line, int col) {
        this.position = position;
        this.line = line;
        this.col = col;
    }

    @Override
    public String toString() {
        return "" + line + ":" + col;
    }
}
