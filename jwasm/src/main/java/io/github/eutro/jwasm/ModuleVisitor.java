package io.github.eutro.jwasm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A visitor that visits a WebAssembly
 * <a href="https://webassembly.github.io/spec/core/syntax/modules.html">module</a>.
 * <p>
 * The structure and order of visits fits the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-module">binary format</a>
 * rather than the common syntax format.
 * <p>
 * Methods are expected to be called in the order:
 * <p>
 * {@code visitHeader}
 * ( {@code visitCustom} )*
 * [ {@code visitTypes} ]
 * ( {@code visitCustom} )*
 * [ {@code visitImports} ]
 * ( {@code visitCustom} )*
 * [ {@code visitFuncs} ]
 * ( {@code visitCustom} )*
 * [ {@code visitTables} ]
 * ( {@code visitCustom} )*
 * [ {@code visitMems} ]
 * ( {@code visitCustom} )*
 * [ {@code visitGlobals} ]
 * ( {@code visitCustom} )*
 * [ {@code visitExports} ]
 * ( {@code visitCustom} )*
 * [ {@code visitStart} ]
 * ( {@code visitCustom} )*
 * [ {@code visitElems} ]
 * ( {@code visitCustom} )*
 * [ {@code visitDataCount} ]
 * ( {@code visitCustom} )*
 * [ {@code visitCode} ]
 * ( {@code visitCustom} )*
 * [ {@code visitDatas} ]
 * ( {@code visitCustom} )*
 * {@code visitEnd}
 */
public class ModuleVisitor extends BaseVisitor<ModuleVisitor> {
    /**
     * Construct a visitor with no delegate.
     */
    public ModuleVisitor() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    public ModuleVisitor(@Nullable ModuleVisitor dl) {
        super(dl);
    }

    /**
     * Visit the version field of the module.
     *
     * @param version The version field of the module. Expected to be {@link Opcodes#VERSION}.
     */
    public void visitHeader(int version) {
        if (dl != null) dl.visitHeader(version);
    }

    /**
     * Visit a
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#custom-section">custom section</a>
     * of the module.
     *
     * @param name    The name of the custom section, for further identification.
     * @param payload The raw contents of the custom section.
     */
    public void visitCustom(@NotNull String name, byte @NotNull [] payload) {
        if (dl != null) dl.visitCustom(name, payload);
    }

    /**
     * Visit the
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#type-section">type section</a>
     * of a module.
     *
     * @return The visitor to visit the section with, or
     * {@code null} if this visitor is not interested in visiting this section.
     */
    public @Nullable TypesVisitor visitTypes() {
        if (dl != null) return dl.visitTypes();
        return null;
    }

    /**
     * Visit the
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#import-section">import section</a>
     * of a module.
     *
     * @return The visitor to visit the section with, or
     * {@code null} if this visitor is not interested in visiting this section.
     */
    public @Nullable ImportsVisitor visitImports() {
        if (dl != null) return dl.visitImports();
        return null;
    }

    /**
     * Visit the
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#function-section">function section</a>
     * of a module.
     *
     * @return The visitor to visit the section with, or
     * {@code null} if this visitor is not interested in visiting this section.
     */
    public @Nullable FunctionsVisitor visitFuncs() {
        if (dl != null) return dl.visitFuncs();
        return null;
    }

    /**
     * Visit the
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#table-section">table section</a>
     * of a module.
     *
     * @return The visitor to visit the section with, or
     * {@code null} if this visitor is not interested in visiting this section.
     */
    public @Nullable TablesVisitor visitTables() {
        if (dl != null) return dl.visitTables();
        return null;
    }

    /**
     * Visit the
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#memory-section">memory section</a>
     * of a module.
     *
     * @return The visitor to visit the section with, or
     * {@code null} if this visitor is not interested in visiting this section.
     */
    public @Nullable MemoriesVisitor visitMems() {
        if (dl != null) return dl.visitMems();
        return null;
    }

    /**
     * Visit the
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#global-section">global section</a>
     * of a module.
     *
     * @return The visitor to visit the section with, or
     * {@code null} if this visitor is not interested in visiting this section.
     */
    public @Nullable GlobalsVisitor visitGlobals() {
        if (dl != null) return dl.visitGlobals();
        return null;
    }

    /**
     * Visit the
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#export-section">export section</a>
     * of a module.
     *
     * @return The visitor to visit the section with, or
     * {@code null} if this visitor is not interested in visiting this section.
     */
    public @Nullable ExportsVisitor visitExports() {
        if (dl != null) return dl.visitExports();
        return null;
    }

    /**
     * Visit the
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#start-section">start section</a>
     * of a module.
     *
     * @param func The start function of the module.
     */
    public void visitStart(int func) {
        if (dl != null) dl.visitStart(func);
    }

    /**
     * Visit the
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#element-section">element section</a>
     * of a module.
     *
     * @return The visitor to visit the section with, or
     * {@code null} if this visitor is not interested in visiting this section.
     */
    public @Nullable ElementSegmentsVisitor visitElems() {
        if (dl != null) return dl.visitElems();
        return null;
    }

    /**
     * Visit the
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#data-count-section">data count section</a>
     * of a module.
     *
     * @param count The number of data segments in the data section.
     * @see #visitDatas()
     */
    public void visitDataCount(int count) {
        if (dl != null) dl.visitDataCount(count);
    }

    /**
     * Visit the
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#code-section">code section</a>
     * of a module.
     *
     * @return The visitor to visit the section with, or
     * {@code null} if this visitor is not interested in visiting this section.
     */
    public @Nullable CodesVisitor visitCode() {
        if (dl != null) return dl.visitCode();
        return null;
    }

    /**
     * Visit the
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#data-section">data section</a>
     * of a module.
     *
     * @return The visitor to visit the section with, or
     * {@code null} if this visitor is not interested in visiting this section.
     */
    public @Nullable DataSegmentsVisitor visitDatas() {
        if (dl != null) return dl.visitDatas();
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
