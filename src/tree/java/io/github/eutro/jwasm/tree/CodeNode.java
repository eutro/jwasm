package io.github.eutro.jwasm.tree;

public class CodeNode {
    public byte[] locals;
    public ExprNode expr;

    public CodeNode(byte[] locals, ExprNode expr) {
        this.locals = locals;
        this.expr = expr;
    }
}
