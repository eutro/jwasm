package io.github.eutro.jwasm.sexp.wast;

import io.github.eutro.jwasm.BaseVisitor;
import org.jetbrains.annotations.Nullable;

/**
 * A visitor which visits a module in a wast script.
 *
 * @see WastReader
 */
public class WastModuleVisitor extends BaseVisitor<WastModuleVisitor> {
    /**
     * Construct a {@link WastModuleVisitor} with no delegate.
     */
    public WastModuleVisitor() {
    }

    /**
     * Construct a {@link WastModuleVisitor} with the given delegate.
     *
     * @param dl The delegate.
     */
    public WastModuleVisitor(@Nullable WastModuleVisitor dl) {
        super(dl);
    }

    /**
     * Visit an s-expression module.
     *
     * @param module The s-expression of the module.
     */
    public void visitWatModule(Object module) {
        if (dl != null) dl.visitWatModule(module);
    }

    /**
     * Visit a module in binary text. May be malformed.
     *
     * @param module The s-expression of the module.
     */
    public void visitBinaryModule(Object module) {
        if (dl != null) dl.visitBinaryModule(module);
    }

    /**
     * Visit a module in quoted text. May be malformed.
     *
     * @param module The s-expression of the module.
     */
    public void visitQuoteModule(Object module) {
        if (dl != null) dl.visitQuoteModule(module);
    }
}
