package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class CallInsnNode extends AbstractInsnNode {
    public int function;

    public CallInsnNode(int function) {
        this.function = function;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitCallInsn(function);
    }
}
