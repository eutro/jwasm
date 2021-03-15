package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class SelectInsnNode extends AbstractInsnNode {
    public byte[] type;

    public SelectInsnNode(byte[] type) {
        this.type = type;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitSelectInsn(type);
    }
}
