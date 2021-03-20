package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExportsVisitor;
import io.github.eutro.jwasm.ModuleVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A node that represents the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#export-section">export section</a>
 * of a module.
 *
 * @see ModuleVisitor#visitExports()
 */
public class ExportsNode extends ExportsVisitor implements Iterable<ExportNode> {
    /**
     * The vector of {@link ExportNode}s, or {@code null} if there aren't any.
     */
    public @Nullable List<ExportNode> exports;

    /**
     * Make the given {@link ExportsVisitor} visit all the exports of this node.
     *
     * @param ev The visitor to visit.
     */
    public void accept(ExportsVisitor ev) {
        if (exports != null) {
            for (ExportNode export : exports) {
                export.accept(ev);
            }
        }
        ev.visitEnd();
    }

    @Override
    public void visitExport(@NotNull String name, byte type, int index) {
        if (exports == null) exports = new ArrayList<>();
        exports.add(new ExportNode(name, type, index));
    }

    @NotNull
    @Override
    public Iterator<ExportNode> iterator() {
        return exports == null ? Collections.emptyIterator() : exports.iterator();
    }
}
