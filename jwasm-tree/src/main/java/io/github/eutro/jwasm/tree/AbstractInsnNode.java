package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

/**
 * A node that represents a bytecode
 * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-instr">instruction</a>.
 *
 * @see ExprVisitor
 */
public abstract class AbstractInsnNode {
    /**
     * The opcode of this instruction.
     */
    public final byte opcode;

    /**
     * Construct an {@link AbstractInsnNode} with the given opcode.
     *
     * @param opcode The opcode of the instruciton.
     */
    protected AbstractInsnNode(byte opcode) {
        this.opcode = opcode;
    }

    /**
     * Visit the given {@link ExprVisitor} with this instruction.
     *
     * @param ev The visitor to visit.
     */
    public abstract void accept(ExprVisitor ev);
}
