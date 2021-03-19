package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class IndexedMemInsnNode extends PrefixInsnNode {
    public int index;

    public IndexedMemInsnNode(int intOpcode, int index) {
        super(intOpcode);
        this.index = index;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitIndexedMemInsn(intOpcode, index);
    }
}
