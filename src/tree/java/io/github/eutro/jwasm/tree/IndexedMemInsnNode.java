package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class IndexedMemInsnNode extends AbstractInsnNode {
    public int opcode;
    public int index;

    public IndexedMemInsnNode(int opcode, int index) {
        this.opcode = opcode;
        this.index = index;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitIndexedMemInsn(opcode, index);
    }
}
