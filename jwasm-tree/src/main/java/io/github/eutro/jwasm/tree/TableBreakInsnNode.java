package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class TableBreakInsnNode extends AbstractInsnNode {
    public int[] labels;
    public int defaultLabel;

    public TableBreakInsnNode(int[] labels, int defaultLabel) {
        this.labels = labels;
        this.defaultLabel = defaultLabel;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitTableBreakInsn(labels, defaultLabel);
    }
}
