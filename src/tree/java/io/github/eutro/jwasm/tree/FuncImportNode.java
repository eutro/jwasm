package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ImportsVisitor;

public class FuncImportNode extends AbstractImportNode {
    public int index;

    public FuncImportNode(String module, String name, int index) {
        super(module, name);
        this.index = index;
    }

    @Override
    void accept(ImportsVisitor iv) {
        iv.visitFuncImport(module, name, index);
    }
}
