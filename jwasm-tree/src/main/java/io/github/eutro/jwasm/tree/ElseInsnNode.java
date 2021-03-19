package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;

public class ElseInsnNode extends AbstractInsnNode {
    public ElseInsnNode() {
        super(Opcodes.ELSE);
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitElseInsn();
    }
}
