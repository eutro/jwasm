package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;

public class SelectInsnNode extends AbstractInsnNode {
    public byte[] type;

    public SelectInsnNode(byte[] type) {
        super(Opcodes.SELECTT);
        this.type = type;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitSelectInsn(type);
    }
}
