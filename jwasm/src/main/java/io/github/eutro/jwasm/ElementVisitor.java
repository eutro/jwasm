package io.github.eutro.jwasm;

import org.jetbrains.annotations.Nullable;

/**
 * A visitor that visits an
 * <a href="https://webassembly.github.io/spec/core/syntax/modules.html#syntax-elem">element segment</a>
 * in the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#element-section">element section</a>
 * of a module.
 * <p>
 * Methods are expected to be called in the order:
 * <p>
 * ( {@code visitNonActiveMode} | {@code visitActiveMode} )
 * {@code visitType}
 * ( {@code visitElemIndices} | ( {@code visitInit} )*)
 * {@code visitEnd}
 */
public class ElementVisitor extends BaseVisitor<ElementVisitor> {
    /**
     * Construct a visitor with no delegate.
     */
    public ElementVisitor() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    public ElementVisitor(@Nullable ElementVisitor dl) {
        super(dl);
    }

    /**
     * Visit the mode of an element segment that is not active.
     *
     * @param passive {@code true} if the mode is {@code passive}, or {@code false} if it is {@code declarative}.
     */
    public void visitNonActiveMode(boolean passive) {
        if (dl != null) dl.visitNonActiveMode(passive);
    }

    /**
     * Visit the {@code table} index and {@code offset} expr of an active element segment.
     *
     * @param table The table
     *              <a href="https://webassembly.github.io/spec/core/syntax/modules.html#syntax-tableidx">index</a>
     *              of the element segment.
     * @return An {@link ExprVisitor} to visit with the contents of the {@code offset} expr,
     * or {@code null} if this visitor is not interested in the contents of the {@code offset} expr.
     */
    public ExprVisitor visitActiveMode(int table) {
        if (dl != null) return dl.visitActiveMode(table);
        return null;
    }

    /**
     * Visit the {@code type} of the element segment.
     *
     * @param type The
     *             <a href="https://webassembly.github.io/spec/core/syntax/types.html#syntax-reftype">reftype</a>
     *             of the element segment.
     */
    public void visitType(byte type) {
        if (dl != null) dl.visitType(type);
    }

    /**
     * Visit a vector of function indices to reference in the init exprs.
     * <p>
     * This should be called instead of {@link #visitInit()}, not in conjunction with it.
     *
     * @param indices The vector of function
     *                <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-funcidx">indices</a>
     *                to reference in the init exprs.
     */
    public void visitElemIndices(int[] indices) {
        if (dl != null) dl.visitElemIndices(indices);
    }

    /**
     * Visit a single expr in the vector of {@code init} exprs.
     * <p>
     * This should be called instead of {@link #visitElemIndices(int[])}, not in conjunction with it.
     *
     * @return An {@link ExprVisitor} to visit with the contents of the {@code init} expr,
     * or {@code null} if this visitor is not interested in the contents of the {@code init} expr.
     */
    public ExprVisitor visitInit() {
        if (dl != null) return dl.visitInit();
        return null;
    }
}
