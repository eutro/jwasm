package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;

/**
 * A node that represents a ref.func instruction.
 *
 * @see ExprVisitor#visitFuncRefInsn(int)
 */
public class FuncRefInsnNode extends AbstractInsnNode {
    /**
     * The
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-funcidx">index</a>
     * of the function to reference.
     */
    public int function;

    /**
     * Construct a {@link FuncRefInsnNode} with the given function.
     *
     * @param function The
     *                 <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-funcidx">index</a>
     *                 of the function to reference.
     */
    public FuncRefInsnNode(int function) {
        super(Opcodes.REF_FUNC);
        this.function = function;
    }

    /**
     * {@inheritDoc}
     *
     * @param ev The visitor to visit.
     * @see ExprVisitor#visitFuncRefInsn(int)
     */
    @Override
    public void accept(ExprVisitor ev) {
        ev.visitFuncRefInsn(function);
    }
}
