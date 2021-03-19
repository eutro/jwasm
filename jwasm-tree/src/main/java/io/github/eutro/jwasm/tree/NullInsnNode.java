package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;

public class NullInsnNode extends AbstractInsnNode {
    public byte type;

    public NullInsnNode(byte type) {
        super(Opcodes.REF_NULL);
        this.type = type;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitNullInsn(type);
    }
}
