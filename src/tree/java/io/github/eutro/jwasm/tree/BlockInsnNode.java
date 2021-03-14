package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class BlockInsnNode extends AbstractInsnNode {
    public byte opcode;
    public int blockType;

    public BlockInsnNode(byte opcode, int blockType) {
        this.opcode = opcode;
        this.blockType = blockType;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitBlockInsn(opcode, blockType);
    }
}
