package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.Limits;
import io.github.eutro.jwasm.TablesVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TablesNode extends TablesVisitor implements Iterable<TableNode> {
    public @Nullable List<TableNode> tables;

    public void accept(TablesVisitor tv) {
        if (tables != null) {
            for (TableNode table : tables) {
                tv.visitTable(table.limits.min, table.limits.max, table.type);
            }
        }
        tv.visitEnd();
    }

    @Override
    public void visitTable(int min, Integer max, byte type) {
        if (tables == null) tables = new ArrayList<>();
        tables.add(new TableNode(new Limits(min, max), type));
    }

    @NotNull
    @Override
    public Iterator<TableNode> iterator() {
        return tables == null ? Collections.emptyIterator() : tables.iterator();
    }
}
