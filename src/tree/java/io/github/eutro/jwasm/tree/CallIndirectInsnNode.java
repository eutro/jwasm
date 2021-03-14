package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class CallIndirectInsnNode extends AbstractInsnNode {
    public int table;
    public int index;

    public CallIndirectInsnNode(int table, int index) {
        this.table = table;
        this.index = index;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitCallIndirectInsn(table, index);
    }
}
