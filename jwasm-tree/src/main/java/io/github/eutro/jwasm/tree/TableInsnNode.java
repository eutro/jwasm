package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class TableInsnNode extends AbstractInsnNode {
    public int index;

    public TableInsnNode(byte opcode, int index) {
        super(opcode);
        this.index = index;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitTableInsn(opcode, index);
    }
}
