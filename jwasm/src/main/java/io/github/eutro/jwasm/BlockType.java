package io.github.eutro.jwasm;

public class BlockType {
    public final Kind kind;
    public final int type;

    public BlockType(Kind kind, int type) {
        this.kind = kind;
        this.type = type;
    }

    public static BlockType valtype(byte type) {
        return new BlockType(Kind.VALTYPE, type);
    }

    public static BlockType functype(int type) {
        return new BlockType(Kind.FUNCTYPE, type);
    }

    public boolean isValtype() {
        return kind == Kind.VALTYPE;
    }

    public boolean isFunctype() {
        return kind == Kind.FUNCTYPE;
    }

    public int get() {
        return type;
    }

    public enum Kind {
        VALTYPE,
        FUNCTYPE,
    }
}
