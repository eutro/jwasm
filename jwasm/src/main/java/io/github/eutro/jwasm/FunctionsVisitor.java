package io.github.eutro.jwasm;

import org.jetbrains.annotations.Nullable;

/**
 * A visitor that visits the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#function-section">function section</a>
 * of a module.
 * <p>
 * Methods are expected to be called in the order:
 * <p>
 * ( {@code visitFunc} )*
 * {@code visitEnd}
 */
public class FunctionsVisitor extends BaseVisitor<FunctionsVisitor> {
    /**
     * Construct a visitor with no delegate.
     */
    public FunctionsVisitor() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    public FunctionsVisitor(@Nullable FunctionsVisitor dl) {
        super(dl);
    }

    /**
     * Visit the type of a function that is defined in the module.
     * <p>
     * The locals and body fields of the function are visited by the {@link CodesVisitor#visitCode(byte[])}.
     * <p>
     * Each call to this function should be matched with a {@link CodesVisitor#visitCode(byte[])}
     * call to the corresponding {@link CodesVisitor} obtained from {@link ModuleVisitor#visitCode()},
     * if it is not {@code null}.
     *
     * @param type The
     *             <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-typeidx">index</a>
     *             of the function's type.
     * @see CodesVisitor
     * @see CodesVisitor#visitCode(byte[])
     */
    public void visitFunc(int type) {
        if (dl != null) dl.visitFunc(type);
    }
}
