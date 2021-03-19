package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class VariableInsnNode extends AbstractInsnNode {
    public byte opcode;
    public int index;

    public VariableInsnNode(byte opcode, int index) {
        super(opcode);
        this.index = index;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitVariableInsn(opcode, index);
    }
}
