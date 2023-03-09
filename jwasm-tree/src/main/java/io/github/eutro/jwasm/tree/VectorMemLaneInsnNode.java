package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class VectorMemLaneInsnNode extends VectorInsnNode {
    public int align;
    public int offset;
    public byte lane;

    public VectorMemLaneInsnNode(int intOpcode, int align, int offset, byte lane) {
        super(intOpcode);
        this.align = align;
        this.offset = offset;
        this.lane = lane;
    }

    @Override
    public void accept(ExprVisitor ev) {
        ev.visitPc(pc);
        ev.visitVectorMemLaneInsn(intOpcode, align, offset, lane);
    }
}
