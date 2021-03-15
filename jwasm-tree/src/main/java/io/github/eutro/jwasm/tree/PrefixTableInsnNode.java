package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class PrefixTableInsnNode extends AbstractInsnNode {
    public int opcode;
    public int index;

    public PrefixTableInsnNode(int opcode, int index) {
        this.opcode = opcode;
        this.index = index;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitPrefixTableInsn(opcode, index);
    }
}
