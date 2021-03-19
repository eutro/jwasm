package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;

public class EndInsnNode extends AbstractInsnNode {
    protected EndInsnNode() {
        super(Opcodes.END);
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitEnd();
    }
}
