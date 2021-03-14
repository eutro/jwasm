package io.github.eutro.jwasm;

public class TablesVisitor extends BaseVisitor<TablesVisitor> {
    public TablesVisitor() {
    }

    public TablesVisitor(TablesVisitor dl) {
        super(dl);
    }

    public void visitTable(int min, Integer max, byte type) {
        if (dl != null) dl.visitTable(min, max, type);
    }
}
