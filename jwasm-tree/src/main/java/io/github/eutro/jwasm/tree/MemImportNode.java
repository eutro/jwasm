package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ImportsVisitor;
import io.github.eutro.jwasm.Limits;
import io.github.eutro.jwasm.Opcodes;

public class MemImportNode extends AbstractImportNode {
    public Limits limits;

    public MemImportNode(String module, String name, Limits limits) {
        super(module, name);
        this.limits = limits;
    }

    @Override
    public byte importType() {
        return Opcodes.IMPORTS_MEM;
    }

    @Override
    void accept(ImportsVisitor iv) {
        iv.visitMemImport(module, name, limits.min, limits.max);
    }
}
