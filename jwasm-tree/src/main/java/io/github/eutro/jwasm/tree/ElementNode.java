package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ElementSegmentsVisitor;
import io.github.eutro.jwasm.ElementVisitor;
import io.github.eutro.jwasm.ExprVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * A node that represents an
 * <a href="https://webassembly.github.io/spec/core/syntax/modules.html#syntax-elem">element segment</a>
 * in the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#element-section">element section</a>
 * of a module.
 *
 * @see ElementSegmentsVisitor#visitElem()
 * @see ElementSegmentsNode
 */
public class ElementNode extends ElementVisitor implements Iterable<ExprNode> {
    /**
     * Whether the element segment is passive, if it is not active.
     * <p>
     * True if passive, false if declarative, ignored if active.
     *
     * @see #offset
     */
    public boolean passive;

    /**
     * The table index of the element segment, if it is active.
     */
    public int table;

    /**
     * The offset expression of an active element segment, or {@code null} if it is not active.
     */
    public ExprNode offset;

    /**
     * The
     * <a href="https://webassembly.github.io/spec/core/syntax/types.html#syntax-reftype">reftype</a>
     * of the element segment.
     */
    public byte type;

    /**
     * The vector of function
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-funcidx">indices</a>
     * to reference in the init exprs,
     * or {@code null} if the {@link #init init expressions} are explicitly declared.
     *
     * @see #init
     */
    public int[] indices;

    /**
     * The vector of {@code init} exprs, or {@code null} if the {@link #indices function indices} should be used.
     *
     * @see #indices
     */
    public List<ExprNode> init = new ArrayList<>();

    /**
     * Construct a visitor with no delegate.
     */
    public ElementNode() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    public ElementNode(@Nullable ElementVisitor dl) {
        super(dl);
    }

    /**
     * Make the given {@link ElementVisitor} visit this element.
     *
     * @param eev The visitor to visit.
     */
    public void accept(ElementVisitor eev) {
        if (offset == null) {
            eev.visitNonActiveMode(passive);
        } else {
            ExprVisitor ev = eev.visitActiveMode(table);
            if (ev != null) offset.accept(ev);
        }
        eev.visitType(type);
        if (indices != null) {
            eev.visitElemIndices(indices);
        } else if (init != null) {
            for (ExprNode en : init) {
                ExprVisitor ev = eev.visitInit();
                if (ev != null) en.accept(ev);
            }
        }
        eev.visitEnd();
    }

    /**
     * The number of init expressions in this element segment, the size of {@link #iterator()}.
     *
     * @return The number of expressions in this element segment.
     */
    public int size() {
        return init == null ? indices.length : init.size();
    }

    @Override
    public void visitNonActiveMode(boolean passive) {
        super.visitNonActiveMode(passive);
        this.passive = passive;
    }

    @Override
    public ExprVisitor visitActiveMode(int table) {
        this.table = table;
        return offset = new ExprNode(super.visitActiveMode(table));
    }

    @Override
    public void visitType(byte type) {
        super.visitType(type);
        this.type = type;
    }

    @Override
    public void visitElemIndices(int[] indices) {
        super.visitElemIndices(indices);
        this.indices = indices;
        this.init = null;
    }

    @Override
    public ExprVisitor visitInit() {
        if (init == null) init = new ArrayList<>();
        ExprNode en = new ExprNode(super.visitInit());
        init.add(en);
        return en;
    }

    /**
     * Returns an iterator over the init expressions in this element segment,
     * whether they be {@link #indices implicitly derived from function indices}
     * or {@link #init explicitly declared}.
     *
     * @return An iterator over the init expressions in this element segment.
     */
    @NotNull
    @Override
    public Iterator<ExprNode> iterator() {
        return init == null ? Arrays.stream(indices).mapToObj(f -> {
            ExprNode en = new ExprNode();
            en.visitFuncRefInsn(f);
            en.visitEndInsn();
            return en;
        }).iterator() : init.iterator();
    }
}
