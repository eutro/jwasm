package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class TableBreakInsnNode extends AbstractInsnNode {
    public int[] table;
    public int index;

    public TableBreakInsnNode(int[] table, int index) {
        this.table = table;
        this.index = index;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitTableBreakInsn(table, index);
    }
}
