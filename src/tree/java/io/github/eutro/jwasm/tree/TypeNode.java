package io.github.eutro.jwasm.tree;

public class TypeNode {
    public byte[] params;
    public byte[] returns;

    public TypeNode(byte[] params, byte[] returns) {
        this.params = params;
        this.returns = returns;
    }
}
