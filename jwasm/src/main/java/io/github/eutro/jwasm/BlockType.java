package io.github.eutro.jwasm;

/**
 * The
 * <a href="https://webassembly.github.io/spec/core/syntax/instructions.html#syntax-blocktype">
 * block type</a> of a block instruction.
 */
public class BlockType {
    /**
     * The kind of block type, inline valtype or functype.
     */
    public final Kind kind;
    /**
     * The integer value, either a valtype or a function index.
     */
    public final int type;

    /**
     * Construct a block type with the given kind and value.
     *
     * @param kind The kind of block type.
     * @param type The integer value.
     */
    public BlockType(Kind kind, int type) {
        this.kind = kind;
        this.type = type;
    }

    /**
     * Create a block type from an inline valtype.
     *
     * @param type The valtype.
     * @return The block type.
     */
    public static BlockType valtype(byte type) {
        return new BlockType(Kind.VALTYPE, type);
    }

    /**
     * Create a block type from a type index.
     *
     * @param type The type index.
     * @return The block type.
     */
    public static BlockType functype(int type) {
        return new BlockType(Kind.FUNCTYPE, type);
    }

    /**
     * Returns whether this block type is an inline valtype.
     *
     * @return Whether this block type is an inline valtype.
     */
    public boolean isValtype() {
        return kind == Kind.VALTYPE;
    }

    /**
     * Returns whether this block type is a functype.
     *
     * @return Whether this block type is a functype.
     */
    public boolean isFunctype() {
        return kind == Kind.FUNCTYPE;
    }

    /**
     * Get the integer value of this block type.
     *
     * @return The integer value of this block type.
     */
    public int get() {
        return type;
    }

    /**
     * The kind of block type.
     */
    public enum Kind {
        /**
         * An inline valtype.
         * <p>
         * The block has no arguments and returns the one value,
         * or nothing if the valtype is {@link Opcodes#EMPTY_TYPE}.
         */
        VALTYPE,
        /**
         * A function type.
         * <p>
         * The block has the arguments and results that the
         * type it refers to specifies.
         */
        FUNCTYPE,
    }
}
