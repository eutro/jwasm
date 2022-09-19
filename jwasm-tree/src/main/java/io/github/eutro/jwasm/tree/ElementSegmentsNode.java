package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ElementSegmentsVisitor;
import io.github.eutro.jwasm.ElementVisitor;
import io.github.eutro.jwasm.ModuleVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A node that represents the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#element-section">element section</a>
 * of a module.
 *
 * @see ModuleVisitor#visitElems()
 * @see ElementNode
 */
public class ElementSegmentsNode extends ElementSegmentsVisitor implements Iterable<ElementNode> {
    /**
     * The vector of {@link ElementNode}s, or {@code null} if there aren't any.
     */
    public @Nullable List<ElementNode> elems;

    /**
     * Construct a visitor with no delegate.
     */
    public ElementSegmentsNode() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    public ElementSegmentsNode(@Nullable ElementSegmentsVisitor dl) {
        super(dl);
    }

    /**
     * Make the given {@link ElementSegmentsVisitor} visit all the element segments of this node.
     *
     * @param ev The visitor to visit.
     */
    public void accept(ElementSegmentsVisitor ev) {
        if (elems != null) {
            for (ElementNode elem : elems) {
                ElementVisitor eev = ev.visitElem();
                if (eev != null) elem.accept(eev);
            }
        }
        ev.visitEnd();
    }

    @Override
    public ElementVisitor visitElem() {
        if (elems == null) elems = new ArrayList<>();
        ElementNode en = new ElementNode(super.visitElem());
        elems.add(en);
        return en;
    }

    @NotNull
    @Override
    public Iterator<ElementNode> iterator() {
        return elems == null ? Collections.emptyIterator() : elems.iterator();
    }
}
