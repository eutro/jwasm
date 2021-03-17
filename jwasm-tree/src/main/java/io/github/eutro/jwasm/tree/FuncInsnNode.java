package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class FuncInsnNode extends AbstractInsnNode {
    public int function;

    public FuncInsnNode(int function) {
        this.function = function;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitFuncInsn(function);
    }
}
