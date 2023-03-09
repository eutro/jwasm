package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class VectorConstOrShuffleInsnNode extends VectorInsnNode {
    public byte[] bytes;

    public VectorConstOrShuffleInsnNode(int intOpcode, byte[] bytes) {
        super(intOpcode);
        this.bytes = bytes;
    }

    @Override
    public void accept(ExprVisitor ev) {
        ev.visitPc(pc);
        ev.visitVectorConstOrShuffleInsn(intOpcode, bytes);
    }
}
