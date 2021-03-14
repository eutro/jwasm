package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class BreakInsnNode extends AbstractInsnNode {
    public byte opcode;
    public int index;

    public BreakInsnNode(byte opcode, int index) {
        this.opcode = opcode;
        this.index = index;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitBreakInsn(opcode, index);
    }
}
