package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;

/**
 * A node that represents a br_table instruction.
 *
 * @see ExprVisitor#visitTableBreakInsn(int[], int)
 */
public class TableBreakInsnNode extends AbstractInsnNode {
    /**
     * The table of label
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-labelidx">indeces</a>.
     */
    public int[] labels;

    /**
     * The
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-labelidx">index</a>
     * of the default label.
     */
    public int defaultLabel;

    /**
     * Construct a {@link TableBreakInsnNode} with the given labels and default label.
     *
     * @param labels       The table of label
     *                     <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-labelidx">indeces</a>.
     * @param defaultLabel The
     *                     <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-labelidx">index</a>
     *                     of the default label.
     */
    public TableBreakInsnNode(int[] labels, int defaultLabel) {
        super(Opcodes.BR_TABLE);
        this.labels = labels;
        this.defaultLabel = defaultLabel;
    }

    /**
     * {@inheritDoc}
     *
     * @param ev The visitor to visit.
     * @see ExprVisitor#visitTableBreakInsn(int[], int)
     */
    @Override
    public void accept(ExprVisitor ev) {
        ev.visitTableBreakInsn(labels, defaultLabel);
    }
}
