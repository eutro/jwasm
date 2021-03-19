package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ImportsVisitor;
import io.github.eutro.jwasm.Opcodes;

public class FuncImportNode extends AbstractImportNode {
    public int type;

    public FuncImportNode(String module, String name, int type) {
        super(module, name);
        this.type = type;
    }

    @Override
    public byte importType() {
        return Opcodes.IMPORTS_FUNC;
    }

    @Override
    void accept(ImportsVisitor iv) {
        iv.visitFuncImport(module, name, type);
    }
}
