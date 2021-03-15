package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class PrefixInsnNode extends AbstractInsnNode {
    public int opcode;

    public PrefixInsnNode(int opcode) {
        this.opcode = opcode;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitPrefixInsn(opcode);
    }
}
