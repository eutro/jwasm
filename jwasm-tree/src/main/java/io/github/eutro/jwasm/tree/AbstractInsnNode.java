package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public abstract class AbstractInsnNode {
    public final byte opcode;

    protected AbstractInsnNode(byte opcode) {
        this.opcode = opcode;
    }

    abstract void accept(ExprVisitor ev);
}
