package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class PrefixBinaryTableInsnNode extends AbstractInsnNode {
    public int opcode;
    public int firstIndex;
    public int secondIndex;

    public PrefixBinaryTableInsnNode(int opcode, int firstIndex, int secondIndex) {
        this.opcode = opcode;
        this.firstIndex = firstIndex;
        this.secondIndex = secondIndex;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitPrefixBinaryTableInsn(opcode, firstIndex, secondIndex);
    }
}
