package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;

/**
 * A node that represents a call_indirect instruction.
 *
 * @see ExprVisitor#visitCallIndirectInsn(int, int)
 */
public class CallIndirectInsnNode extends AbstractInsnNode {
    /**
     * The table
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-tableidx">index</a>
     * to look up the reference in.
     */
    public int table;

    /**
     * The
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-typeidx">index</a>
     * of the function's type.
     */
    public int type;

    /**
     * Construct a {@link CallIndirectInsnNode} with the given table and type.
     *
     * @param table The table
     *              <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-tableidx">index</a>
     *              to look up the reference in.
     * @param type  The
     *              <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-typeidx">index</a>
     *              of the function's type.
     */
    public CallIndirectInsnNode(int table, int type) {
        super(Opcodes.CALL_INDIRECT);
        this.table = table;
        this.type = type;
    }

    /**
     * {@inheritDoc}
     *
     * @see ExprVisitor#visitCallIndirectInsn(int, int)
     *
     * @param ev The visitor to visit.
     */
    @Override
    void accept(ExprVisitor ev) {
        ev.visitCallIndirectInsn(table, type);
    }
}
