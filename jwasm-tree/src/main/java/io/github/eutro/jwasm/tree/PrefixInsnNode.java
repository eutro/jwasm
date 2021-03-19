package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;

public class PrefixInsnNode extends AbstractInsnNode {
    public int intOpcode;

    public PrefixInsnNode(int intOpcode) {
        super(Opcodes.INSN_PREFIX);
        this.intOpcode = intOpcode;
    }

    @Override
    void accept(ExprVisitor ev) {
        ev.visitPrefixInsn(intOpcode);
    }
}
