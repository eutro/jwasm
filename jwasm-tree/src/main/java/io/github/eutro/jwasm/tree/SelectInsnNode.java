package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;

/**
 * A node that represents a select instruction with explicit type arguments.
 *
 * @see ExprVisitor#visitSelectInsn(byte[])
 */
public class SelectInsnNode extends AbstractInsnNode {
    /**
     * The
     * <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-valtype">valtype</a>s
     * of the instruction.
     */
    public byte[] type;

    /**
     * Construct a {@link SelectInsnNode} with the given types.
     *
     * @param type The
     *             <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-valtype">valtype</a>s
     *             of the instruction.
     */
    public SelectInsnNode(byte[] type) {
        super(Opcodes.SELECTT);
        this.type = type;
    }

    /**
     * {@inheritDoc}
     *
     * @param ev The visitor to visit.
     * @see ExprVisitor#visitSelectInsn(byte[])
     */
    @Override
    public void accept(ExprVisitor ev) {
        ev.visitPc(pc);
        ev.visitSelectInsn(type);
    }
}
