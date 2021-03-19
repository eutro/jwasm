package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class TableInsnNode extends AbstractInsnNode {
    public int table;

    public TableInsnNode(byte opcode, int table) {
        super(opcode);
        this.table = table;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitTableInsn(opcode, table);
    }
}
