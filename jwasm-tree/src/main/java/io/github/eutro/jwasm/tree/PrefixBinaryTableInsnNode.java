package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class PrefixBinaryTableInsnNode extends PrefixInsnNode {
    public int firstIndex;
    public int secondIndex;

    public PrefixBinaryTableInsnNode(int intOpcode, int firstIndex, int secondIndex) {
        super(intOpcode);
        this.firstIndex = firstIndex;
        this.secondIndex = secondIndex;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitPrefixBinaryTableInsn(intOpcode, firstIndex, secondIndex);
    }
}
