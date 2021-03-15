package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ImportsVisitor;

public class MemImportNode extends AbstractImportNode {
    public LimitsNode limits;

    public MemImportNode(String module, String name, LimitsNode limits) {
        super(module, name);
        this.limits = limits;
    }

    @Override
    void accept(ImportsVisitor iv) {
        iv.visitMemImport(module, name, limits.min, limits.max);
    }
}
