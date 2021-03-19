package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class PrefixTableInsnNode extends PrefixInsnNode {
    public int index;

    public PrefixTableInsnNode(int intOpcode, int index) {
        super(intOpcode);
        this.index = index;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitPrefixTableInsn(intOpcode, index);
    }
}
