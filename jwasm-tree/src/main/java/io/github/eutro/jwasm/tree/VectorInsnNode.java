package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;

public class VectorInsnNode extends AbstractInsnNode {
    /**
     * The integer opcode of the instruction.
     */
    public int intOpcode;

    public VectorInsnNode(int opcode) {
        super(Opcodes.VECTOR_PREFIX);
        intOpcode = opcode;
    }

    @Override
    public void accept(ExprVisitor ev) {
        ev.visitVectorInsn(intOpcode);
    }
}
