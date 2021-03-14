package io.github.eutro.jwasm.tree;

public class GlobalNode {
    public GlobalTypeNode type;
    public ExprNode init;

    public GlobalNode(GlobalTypeNode type, ExprNode init) {
        this.type = type;
        this.init = init;
    }
}
