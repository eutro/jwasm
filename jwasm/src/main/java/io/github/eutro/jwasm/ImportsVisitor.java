package io.github.eutro.jwasm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A visitor that visits the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#import-section">import section</a>
 * of a module.
 * <p>
 * Methods are expected to be called in the order:
 * <p>
 * ( {@code visitType} )*
 * {@code visitEnd}
 */
public class ImportsVisitor extends BaseVisitor<ImportsVisitor> {
    /**
     * Construct a visitor with no delegate.
     */
    public ImportsVisitor() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    public ImportsVisitor(@Nullable ImportsVisitor dl) {
        super(dl);
    }

    /**
     * Visit a {@code func} import.
     *
     * @param module The module to import from.
     * @param name   The name to import.
     * @param index  The
     *               <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-typeidx">index</a>
     *               of the type of the imported function.
     */
    public void visitFuncImport(@NotNull String module, @NotNull String name, int index) {
        if (dl != null) dl.visitFuncImport(module, name, index);
    }

    /**
     * Visit a {@code table} import.
     *
     * @param module The module to import from.
     * @param name   The name to import.
     * @param min    The minimum of the
     *               <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-limits">limit</a>
     *               of the imported table's
     *               <a href="https://webassembly.github.io/spec/core/binary/types.html#table-types">type</a>.
     * @param max    The maximum of the
     *               <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-limits">limit</a>
     *               of the imported table's
     *               <a href="https://webassembly.github.io/spec/core/binary/types.html#table-types">type</a>,
     *               or {@code null} if unspecified.
     * @param type   The <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-reftype">reftype</a>
     *               of values in the imported table.
     */
    public void visitTableImport(@NotNull String module, @NotNull String name, int min, @Nullable Integer max, byte type) {
        if (dl != null) dl.visitTableImport(module, name, min, max, type);
    }

    /**
     * Visit a {@code mem} import.
     *
     * @param module The module to import from.
     * @param name   The name to import.
     * @param min    The minimum of the
     *               <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-limits">limit</a>
     *               of the
     *               <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-memtype">memtype</a>
     *               of the imported memory.
     * @param max    The maximum of the
     *               <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-limits">limit</a>
     *               of the
     *               <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-memtype">memtype</a>
     *               of the imported memory,
     *               or {@code null} if unspecified.
     */
    public void visitMemImport(@NotNull String module, @NotNull String name, int min, @Nullable Integer max) {
        if (dl != null) dl.visitMemImport(module, name, min, max);
    }

    /**
     * Visit a {@code global} import.
     *
     * @param module The module to import from.
     * @param name   The name to import.
     * @param mut    The
     *               <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-mut">mutability</a>
     *               of the
     *               <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-globaltype">globaltype</a>
     *               of the global to import.
     * @param type   The
     *               <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-valtype">valtype</a>
     *               of the
     *               <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-globaltype">globaltype</a>
     *               of the global to import.
     */
    public void visitGlobalImport(@NotNull String module, @NotNull String name, byte mut, byte type) {
        if (dl != null) dl.visitGlobalImport(module, name, mut, type);
    }
}
