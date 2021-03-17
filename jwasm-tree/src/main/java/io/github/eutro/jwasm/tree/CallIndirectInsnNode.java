package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class CallIndirectInsnNode extends AbstractInsnNode {
    public int table;
    public int type;

    public CallIndirectInsnNode(int table, int type) {
        this.table = table;
        this.type = type;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitCallIndirectInsn(table, type);
    }
}
