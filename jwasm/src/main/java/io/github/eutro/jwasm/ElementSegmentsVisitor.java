package io.github.eutro.jwasm;

import org.jetbrains.annotations.Nullable;

/**
 * A visitor that visits the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#element-section">element section</a>
 * of a module.
 * <p>
 * Methods are expected to be called in the order:
 * <p>
 * ( {@code visitElem} )*
 * {@code visitEnd}
 */
public class ElementSegmentsVisitor extends BaseVisitor<ElementSegmentsVisitor> {
    /**
     * Construct a visitor with no delegate.
     */
    protected ElementSegmentsVisitor() {
        super();
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    protected ElementSegmentsVisitor(@Nullable ElementSegmentsVisitor dl) {
        super(dl);
    }

    /**
     * Visit an element segment of the module.
     *
     * @return An {@link ElementVisitor} to visit with the contents of the element segment,
     * or {@code null} if this visitor is not interested in the contents of the element segment.
     */
    public ElementVisitor visitElem() {
        if (dl != null) return dl.visitElem();
        return null;
    }
}
