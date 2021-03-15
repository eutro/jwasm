package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class TableInsnNode extends AbstractInsnNode {
    public byte opcode;
    public int index;

    public TableInsnNode(byte opcode, int index) {
        this.opcode = opcode;
        this.index = index;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitTableInsn(opcode, index);
    }
}
