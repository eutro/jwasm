package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;

/**
 * A node that represents a {@code ref.null} instruction.
 *
 * @see ExprVisitor#visitNullInsn(byte)
 */
public class NullInsnNode extends AbstractInsnNode {
    /**
     * The <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-reftype">reftype</a>
     * of the null value.
     */
    public byte type;

    /**
     * Construct a {@link NullInsnNode} with the given type.
     *
     * @param type The <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-reftype">reftype</a>
     *             of the null value.
     */
    public NullInsnNode(byte type) {
        super(Opcodes.REF_NULL);
        this.type = type;
    }

    /**
     * {@inheritDoc}
     *
     * @param ev The visitor to visit.
     * @see ExprVisitor#visitNullInsn(byte)
     */
    @Override
    public void accept(ExprVisitor ev) {
        ev.visitPc(pc);
        ev.visitNullInsn(type);
    }
}
