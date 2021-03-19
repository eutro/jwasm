package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class InsnNode extends AbstractInsnNode {

    public InsnNode(byte opcode) {
        super(opcode);
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitInsn(opcode);
    }
}
