package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class FuncInsnNode extends AbstractInsnNode {
    public int index;

    public FuncInsnNode(int index) {
        this.index = index;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitFuncInsn(index);
    }
}
