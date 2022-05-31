package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.CodesVisitor;
import io.github.eutro.jwasm.ImportsVisitor;
import io.github.eutro.jwasm.Limits;
import io.github.eutro.jwasm.ModuleVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A node that represents the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#import-section">import section</a>
 * of a module.
 *
 * @see ModuleVisitor#visitImports()
 */
public class ImportsNode extends ImportsVisitor implements Iterable<AbstractImportNode> {
    /**
     * The vector of {@link AbstractImportNode}s, or {@code null} if there aren't any.
     */
    public @Nullable List<AbstractImportNode> imports;

    /**
     * Construct a visitor with no delegate.
     */
    public ImportsNode() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    public ImportsNode(@Nullable ImportsVisitor dl) {
        super(dl);
    }

    /**
     * Makes the given {@link ImportsVisitor} visit all the imports of this node.
     *
     * @param iv The visitor to visit.
     */
    public void accept(ImportsVisitor iv) {
        if (imports != null) {
            for (AbstractImportNode anImport : imports) {
                anImport.accept(iv);
            }
        }
        iv.visitEnd();
    }

    private List<AbstractImportNode> imports() {
        return imports == null ? imports = new ArrayList<>() : imports;
    }

    @Override
    public void visitFuncImport(@NotNull String module, @NotNull String name, int type) {
        super.visitFuncImport(module, name, type);
        imports().add(new FuncImportNode(module, name, type));
    }

    @Override
    public void visitTableImport(@NotNull String module, @NotNull String name, int min, @Nullable Integer max, byte type) {
        super.visitTableImport(module, name, min, max, type);
        imports().add(new TableImportNode(module, name, new Limits(min, max), type));
    }

    @Override
    public void visitMemImport(@NotNull String module, @NotNull String name, int min, @Nullable Integer max) {
        super.visitMemImport(module, name, min, max);
        imports().add(new MemImportNode(module, name, new Limits(min, max)));
    }

    @Override
    public void visitGlobalImport(@NotNull String module, @NotNull String name, byte mut, byte type) {
        super.visitGlobalImport(module, name, mut, type);
        imports().add(new GlobalImportNode(module, name, new GlobalTypeNode(mut, type)));
    }

    @NotNull
    @Override
    public Iterator<AbstractImportNode> iterator() {
        return imports == null ? Collections.emptyIterator() : imports.iterator();
    }
}
