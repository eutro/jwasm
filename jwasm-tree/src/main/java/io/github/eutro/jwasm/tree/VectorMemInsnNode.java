package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class VectorMemInsnNode extends VectorInsnNode {
    public int align;
    public int offset;

    public VectorMemInsnNode(int opcode, int align, int offset) {
        super(opcode);
        this.align = align;
        this.offset = offset;
    }

    @Override
    public void accept(ExprVisitor ev) {
        ev.visitVectorMemInsn(opcode, align, offset);
    }
}
