package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.CodesVisitor;
import io.github.eutro.jwasm.DataSegmentsVisitor;
import io.github.eutro.jwasm.DataVisitor;
import io.github.eutro.jwasm.ExprVisitor;
import org.jetbrains.annotations.Nullable;

/**
 * A node that represents a
 * <a href="https://webassembly.github.io/spec/core/syntax/modules.html#syntax-data">data segment</a>
 * in the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#data-section">data section</a>
 * of a module.
 *
 * @see DataSegmentsVisitor#visitData()
 * @see DataSegmentsNode
 */
public class DataNode extends DataVisitor {
    /**
     * The {@code init} bytes of the data segment.
     */
    public byte[] init;

    /**
     * The memory
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-memidx">index</a>
     * of the data segment.
     */
    public int memory;

    /**
     * The {@code offset} expr.
     */
    public ExprNode offset;

    /**
     * Constructs a passive {@link DataNode} with only an init array.
     *
     * @param init The {@code init} bytes of the data segment.
     */
    public DataNode(byte[] init) {
        this.init = init;
    }

    /**
     * Construct an active {@link DataNode} with the given init array and offset.
     *
     * @param init   The {@code init} bytes of the data segment.
     * @param offset The {@code offset} expr.
     */
    public DataNode(byte[] init, ExprNode offset) {
        this.init = init;
        this.offset = offset;
    }

    /**
     * Construct an active {@link DataNode} with the given init array, memory index and offset.
     *
     * @param init   The {@code init} bytes of the data segment.
     * @param memory The memory
     *               <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-memidx">index</a>
     *               of the data segment.
     * @param offset The {@code offset} expr.
     */
    public DataNode(byte[] init, int memory, ExprNode offset) {
        this.init = init;
        this.memory = memory;
        this.offset = offset;
    }

    /**
     * Construct a visitor with no delegate.
     */
    public DataNode() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    public DataNode(@Nullable DataVisitor dl) {
        super(dl);
    }

    /**
     * Make the given {@link DataVisitor} visit this data segment.
     *
     * @param dv The visitor to visit.
     */
    public void accept(DataVisitor dv) {
        if (offset != null) {
            ExprVisitor ev = dv.visitActive(memory);
            if (ev != null) offset.accept(ev);
        }
        dv.visitEnd();
    }

    @Override
    public @Nullable ExprVisitor visitActive(int memory) {
        super.visitActive(memory);
        this.memory = memory;
        return offset = new ExprNode();
    }

    @Override
    public void visitInit(byte[] init) {
        super.visitInit(init);
        this.init = init;
    }
}
