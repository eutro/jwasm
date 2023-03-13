package io.github.eutro.jwasm.sexp.wast;

import io.github.eutro.jwasm.BaseVisitor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A visitor which visits an action in a wast script.
 *
 * @see WastReader
 */
public class ActionVisitor extends BaseVisitor<ActionVisitor> {
    /**
     * Construct an {@link ActionVisitor} with no delegate.
     */
    public ActionVisitor() {
    }

    /**
     * Construct an {@link ActionVisitor} with the given delegate.
     *
     * @param dl The delegate.
     */
    public ActionVisitor(@Nullable ActionVisitor dl) {
        super(dl);
    }

    /**
     * Visit an {@code invoke} action, which invokes the given exported function on a module.
     *
     * @param name   The module to take the export from, or null to take it from the last.
     * @param string The name of the export.
     * @param args   The arguments to the invocation.
     */
    public void visitInvoke(@Nullable String name, String string, Object... args) {
        if (dl != null) dl.visitInvoke(name, string, args);
    }

    /**
     * Visit a {@code get} action, which gets an exported global.
     *
     * @param name   The module to take the export from, or null to take it from the last.
     * @param string The name of the export.
     */
    public void visitGet(@Nullable String name, String string) {
        if (dl != null) dl.visitGet(name, string);
    }

    /**
     * Visit an unrecognised action.
     *
     * @param macro    The first symbol in the s-expression.
     * @param fullSexp The full s-expression.
     */
    public void visitOther(String macro, List<?> fullSexp) {
        if (dl != null) dl.visitOther(macro, fullSexp);
    }
}
