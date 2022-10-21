package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class VectorConstOrShuffleInsnNode extends VectorInsnNode {
    public byte[] bytes;

    public VectorConstOrShuffleInsnNode(int opcode, byte[] bytes) {
        super(opcode);
        this.bytes = bytes;
    }

    @Override
    public void accept(ExprVisitor ev) {
        ev.visitVectorConstOrShuffleInsn(opcode, bytes);
    }
}
