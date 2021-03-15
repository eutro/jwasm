package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class InsnNode extends AbstractInsnNode {
    public byte opcode;

    public InsnNode(byte opcode) {
        this.opcode = opcode;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitInsn(opcode);
    }
}
