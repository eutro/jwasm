package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class CallInsnNode extends AbstractInsnNode {
    public int index;

    public CallInsnNode(int index) {
        this.index = index;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitCallInsn(index);
    }
}
