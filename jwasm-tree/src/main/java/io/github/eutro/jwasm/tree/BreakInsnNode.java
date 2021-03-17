package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class BreakInsnNode extends AbstractInsnNode {
    public byte opcode;
    public int label;

    public BreakInsnNode(byte opcode, int label) {
        this.opcode = opcode;
        this.label = label;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitBreakInsn(opcode, label);
    }
}
