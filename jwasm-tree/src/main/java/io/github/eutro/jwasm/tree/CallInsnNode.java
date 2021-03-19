package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;

public class CallInsnNode extends AbstractInsnNode {
    public int function;

    public CallInsnNode(int function) {
        super(Opcodes.CALL);
        this.function = function;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitCallInsn(function);
    }
}
