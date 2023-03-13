package io.github.eutro.jwasm.sexp;

/**
 * An offset in a file, as well as a line and column.
 */
public class SrcLoc {
    /**
     * The offset in the file.
     */
    public final long position;
    /**
     * The line number. Starts at 1.
     */
    public final int line;
    /**
     * The column number. Starts at 0.
     */
    public final int col;

    /**
     * Construct a source location.
     *
     * @param position The position.
     * @param line The line number.
     * @param col The column number.
     */
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
