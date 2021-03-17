package io.github.eutro.jwasm;

import org.jetbrains.annotations.Nullable;

/**
 * A base for delegating {@code *Visitor} classes.
 * <p>
 * Classes that extend this are expected to delegate to {@link #dl}, if it is present,
 * by default, which may be passed in through the constructor.
 *
 * @param <T> The type of the delegate.
 */
public class BaseVisitor<T extends BaseVisitor<T>> {
    /**
     * The visitor to delegate all method calls to, or {@code null}.
     */
    protected @Nullable T dl;

    /**
     * Construct a visitor with no delegate.
     */
    protected BaseVisitor() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    protected BaseVisitor(@Nullable T dl) {
        this.dl = dl;
    }

    /**
     * Finish visiting this visitor.
     * <p>
     * This should be the last {@code visit*} method called.
     */
    public void visitEnd() {
        if (dl != null) dl.visitEnd();
    }
}
