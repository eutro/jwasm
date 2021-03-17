package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class EndInsnNode extends AbstractInsnNode {
    @Override
    void accept(ExprVisitor ev) {
        ev.visitEnd();
    }
}