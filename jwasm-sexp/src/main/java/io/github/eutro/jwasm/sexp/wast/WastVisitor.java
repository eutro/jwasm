package io.github.eutro.jwasm.sexp.wast;

import io.github.eutro.jwasm.BaseVisitor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A visitor which visits a
 * <a href="https://github.com/WebAssembly/spec/blob/main/interpreter/README.md#scripts">wast script</a>.
 *
 * @see WastReader
 */
public class WastVisitor extends BaseVisitor<WastVisitor> {
    /**
     * Construct a {@link WastVisitor} with no delegate.
     */
    public WastVisitor() {
    }

    /**
     * Construct a {@link WastVisitor} with the given delegate.
     *
     * @param dl The delegate.
     */
    public WastVisitor(@Nullable WastVisitor dl) {
        super(dl);
    }

    /**
     * Visit a top-level module command.
     *
     * @param name The name of the module, if present.
     * @return A visitor for the module, or null if it should be ignored.
     */
    public @Nullable WastModuleVisitor visitModule(@Nullable String name) {
        if (dl != null) return dl.visitModule(name);
        return null;
    }

    /**
     * Visit a register command.
     *
     * @param string The name the module should be registered with for imports.
     * @param name The name of the module that should be registered, or null if it should be the last one.
     */
    public void visitRegister(String string, @Nullable String name) {
        if (dl != null) dl.visitRegister(string, name);
    }

    /**
     * Visit a top-level action command.
     *
     * @return A visitor for the action, or null if it should be ignored.
     */
    public @Nullable ActionVisitor visitTopAction() {
        if (dl != null) return dl.visitTopAction();
        return null;
    }

    /**
     * Visit an {@code assert_return} command.
     *
     * @param results The expected results.
     * @return A visitor for the action being {@code assert_return}-ed, or null if it should be ignored.
     */
    public @Nullable ActionVisitor visitAssertReturn(Object... results) {
        if (dl != null) return dl.visitAssertReturn(results);
        return null;
    }

    /**
     * Visit an {@code assert_trap} command.
     *
     * @param failure The expected failure message.
     * @return A visitor for the action being {@code assert_trap}-ed, or null if it should be ignored.
     */
    public @Nullable ActionVisitor visitAssertTrap(String failure) {
        if (dl != null) return dl.visitAssertTrap(failure);
        return null;
    }

    /**
     * Visit an {@code assert_exhaustion} command.
     *
     * @param failure The expected failure message.
     * @return A visitor for the action being {@code assert_exhaustion}-ed, or null if it should be ignored.
     */
    public @Nullable ActionVisitor visitAssertExhaustion(String failure) {
        if (dl != null) return dl.visitAssertExhaustion(failure);
        return null;
    }

    /**
     * Visit an {@code assert_malformed} command.
     *
     * @param failure The expected failure message.
     * @return A visitor for the module being {@code assert_malformed}-ed, or null if it should be ignored.
     */
    public @Nullable WastModuleVisitor visitAssertMalformed(String failure) {
        if (dl != null) return dl.visitAssertMalformed(failure);
        return null;
    }

    /**
     * Visit an {@code assert_invalid} command.
     *
     * @param failure The expected failure message.
     * @return A visitor for the module being {@code assert_invalid}-ed, or null if it should be ignored.
     */
    public @Nullable WastModuleVisitor visitAssertInvalid(String failure) {
        if (dl != null) return dl.visitAssertInvalid(failure);
        return null;
    }

    /**
     * Visit an {@code assert_unlinkable} command.
     *
     * @param failure The expected failure message.
     * @return A visitor for the module being {@code assert_unlinkable}-ed, or null if it should be ignored.
     */
    public @Nullable WastModuleVisitor visitAssertUnlinkable(String failure) {
        if (dl != null) return dl.visitAssertUnlinkable(failure);
        return null;
    }

    /**
     * Visit an {@code assert_trap} command.
     *
     * @param failure The expected failure message.
     * @return A visitor for the module being {@code assert_trap}-ed, or null if it should be ignored.
     */
    public @Nullable WastModuleVisitor visitAssertModuleTrap(String failure) {
        if (dl != null) return dl.visitAssertModuleTrap(failure);
        return null;
    }

    /**
     * Visit an unrecognised top-level command.
     *
     * @param macro The first symbol of the s-expression.
     * @param fullSexp The full s-expression.
     */
    public void visitOther(String macro, List<?> fullSexp) {
        if (dl != null) dl.visitOther(macro, fullSexp);
    }
}
