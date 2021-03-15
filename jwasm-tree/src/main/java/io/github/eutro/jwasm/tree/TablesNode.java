package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.TablesVisitor;

import java.util.ArrayList;
import java.util.List;

public class TablesNode extends TablesVisitor {
    public List<TableNode> tables;

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
        tables.add(new TableNode(new LimitsNode(min, max), type));
    }
}
