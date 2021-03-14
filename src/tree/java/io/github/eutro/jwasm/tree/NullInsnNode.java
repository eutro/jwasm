package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class NullInsnNode extends AbstractInsnNode {
    public byte type;

    public NullInsnNode(byte type) {
        this.type = type;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitNullInsn(type);
    }
}
