package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;

public class TableBreakInsnNode extends AbstractInsnNode {
    public int[] labels;
    public int defaultLabel;

    public TableBreakInsnNode(int[] labels, int defaultLabel) {
        super(Opcodes.BR_TABLE);
        this.labels = labels;
        this.defaultLabel = defaultLabel;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitTableBreakInsn(labels, defaultLabel);
    }
}
