package io.github.eutro.jwasm;

/**
 * A base for delegating *Visitor classes.
 *
 * Classes that extend this are expected to delegate to {@link #dl}, if it is present,
 * by default, which may be passed in through the constructor.
 *
 * @param <T> The type of the delegate.
 */
public class BaseVisitor<T extends BaseVisitor<T>> {
    protected T dl;

    /**
     * Construct a visitor with no delegate.
     */
    protected BaseVisitor() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate to, or null.
     */
    protected BaseVisitor(T dl) {
        this.dl = dl;
    }

    /**
     * Finish visiting this visitor.
     *
     * This should be the last visit* method called.
     */
    public void visitEnd() {
        if (dl != null) dl.visitEnd();
    }
}
