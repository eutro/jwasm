package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public class VectorLaneInsnNode extends VectorInsnNode {
    public byte lane;

    public VectorLaneInsnNode(int intOpcode, byte lane) {
        super(intOpcode);
        this.lane = lane;
    }

    @Override
    public void accept(ExprVisitor ev) {
        ev.visitVectorLaneInsn(intOpcode, lane);
    }
}
