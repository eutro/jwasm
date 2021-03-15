package io.github.eutro.jwasm.tree;

public class GlobalTypeNode {
    public byte mut;
    public byte type;

    public GlobalTypeNode(byte mut, byte type) {
        this.mut = mut;
        this.type = type;
    }
}
