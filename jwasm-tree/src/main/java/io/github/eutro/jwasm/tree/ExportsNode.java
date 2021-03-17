package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExportsVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ExportsNode extends ExportsVisitor {
    public List<ExportNode> exports;

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
}
