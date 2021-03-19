package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExportsVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ExportsNode extends ExportsVisitor implements Iterable<ExportNode> {
    public @Nullable List<ExportNode> exports;

    public void accept(ExportsVisitor ev) {
        if (exports != null) {
            for (ExportNode export : exports) {
                ev.visitExport(export.name, export.type, export.index);
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
