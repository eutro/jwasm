package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class PrefixTableInsnNode extends PrefixInsnNode {
    public int table;

    public PrefixTableInsnNode(int intOpcode, int table) {
        super(intOpcode);
        this.table = table;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitPrefixTableInsn(intOpcode, table);
    }
}
