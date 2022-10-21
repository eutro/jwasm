package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class VectorMemLaneInsnNode extends VectorInsnNode {
    public int align;
    public int offset;
    public byte lane;

    public VectorMemLaneInsnNode(int opcode, int align, int offset, byte lane) {
        super(opcode);
        this.align = align;
        this.offset = offset;
        this.lane = lane;
    }

    @Override
    public void accept(ExprVisitor ev) {
        ev.visitVectorMemLaneInsn(opcode, align, offset, lane);
    }
}
