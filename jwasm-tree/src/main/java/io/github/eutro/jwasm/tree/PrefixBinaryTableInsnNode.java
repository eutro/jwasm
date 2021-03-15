package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class PrefixBinaryTableInsnNode extends AbstractInsnNode {
    public int opcode;
    public int sourceIndex;
    public int targetIndex;

    public PrefixBinaryTableInsnNode(int opcode, int sourceIndex, int targetIndex) {
        this.opcode = opcode;
        this.sourceIndex = sourceIndex;
        this.targetIndex = targetIndex;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitPrefixBinaryTableInsn(opcode, sourceIndex, targetIndex);
    }
}
