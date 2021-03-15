package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class MemInsnNode extends AbstractInsnNode {
    public byte opcode;
    public int align;
    public int offset;

    public MemInsnNode(byte opcode, int align, int offset) {
        this.opcode = opcode;
        this.align = align;
        this.offset = offset;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitMemInsn(opcode, align, offset);
    }
}
