package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;

/**
 * A node that represents a call instruction.
 *
 * @see ExprVisitor#visitCallInsn(int)
 */
public class CallInsnNode extends AbstractInsnNode {
    /**
     * The
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-funcidx">index</a>
     * of the function to call.
     */
    public int function;

    /**
     * Construct a {@link CallInsnNode} with the given function.
     *
     * @param function The
     *                 <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-funcidx">index</a>
     *                 of the function to call.
     */
    public CallInsnNode(int function) {
        super(Opcodes.CALL);
        this.function = function;
    }

    /**
     * {@inheritDoc}
     *
     * @see ExprVisitor#visitCallInsn(int)
     *
     * @param ev The visitor to visit.
     */
    @Override
    void accept(ExprVisitor ev) {
        ev.visitCallInsn(function);
    }
}
