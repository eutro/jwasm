package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ImportsVisitor;

public class TableImportNode extends AbstractImportNode {
    public LimitsNode limits;
    public byte type;

    public TableImportNode(String module, String name, LimitsNode limits, byte type) {
        super(module, name);
        this.limits = limits;
        this.type = type;
    }

    @Override
    void accept(ImportsVisitor iv) {
        iv.visitTableImport(module, name, limits.min, limits.max, type);
    }
}
