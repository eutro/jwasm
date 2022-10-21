package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;

/**
 * A node that represents the {@link Opcodes#END end} of a
 * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#control-instructions">block</a>
 * or
 * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#expressions">expression</a>.
 *
 * @see ExprVisitor#visitEndInsn()
 */
public class EndInsnNode extends AbstractInsnNode {
    /**
     * Constructs an {@link EndInsnNode}.
     */
    public EndInsnNode() {
        super(Opcodes.END);
    }

    /**
     * {@inheritDoc}
     *
     * @param ev The visitor to visit.
     * @see ExprVisitor#visitEndInsn()
     */
    @Override
    public void accept(ExprVisitor ev) {
        ev.visitEndInsn();
    }
}
