package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ImportsVisitor;
import io.github.eutro.jwasm.Limits;
import io.github.eutro.jwasm.Opcodes;

public class TableImportNode extends AbstractImportNode {
    public Limits limits;
    public byte type;

    public TableImportNode(String module, String name, Limits limits, byte type) {
        super(module, name);
        this.limits = limits;
        this.type = type;
    }

    @Override
    public byte importType() {
        return Opcodes.IMPORTS_TABLE;
    }

    @Override
    void accept(ImportsVisitor iv) {
        iv.visitTableImport(module, name, limits.min, limits.max, type);
    }
}
