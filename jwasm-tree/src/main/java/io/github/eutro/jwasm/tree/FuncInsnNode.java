package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;

public class FuncInsnNode extends AbstractInsnNode {
    public int function;

    public FuncInsnNode(int function) {
        super(Opcodes.REF_FUNC);
        this.function = function;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitFuncInsn(function);
    }
}
