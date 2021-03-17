package io.github.eutro.jwasm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A visitor that visits the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#code-section">code section</a>
 * of a module.
 * <p>
 * Methods are expected to be called in the order:
 * <p>
 * ( {@code visitCode} )*
 * {@code visitEnd}
 */
public class CodesVisitor extends BaseVisitor<CodesVisitor> {
    /**
     * Construct a visitor with no delegate.
     */
    public CodesVisitor() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    public CodesVisitor(@Nullable CodesVisitor dl) {
        super(dl);
    }

    /**
     * Visit the code of a function.
     * <p>
     * This completes the locals and body fields of a function
     * whose type was visited in {@link FunctionsVisitor#visitFunc(int)}
     * <p>
     * Each call to this function should match a {@link FunctionsVisitor#visitFunc(int)}
     * call to the corresponding {@link FunctionsVisitor} obtained from {@link ModuleVisitor#visitFuncs()},
     * if it was not {@code null}.
     *
     * @param locals The <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-valtype">valtypes</a>
     *               of the local variables of the function.
     * @return An {@link ExprVisitor} to visit with the body of the function,
     * or {@code null} if this visitor is not interested in the body of the function.
     */
    public @Nullable ExprVisitor visitCode(byte @NotNull [] locals) {
        if (dl != null) return dl.visitCode(locals);
        return null;
    }
}
