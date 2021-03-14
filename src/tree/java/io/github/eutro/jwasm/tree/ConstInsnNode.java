package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class ConstInsnNode extends AbstractInsnNode {
    public Object value;

    public ConstInsnNode(Object value) {
        this.value = value;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitConstInsn(value);
    }
}
