package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ImportsVisitor;
import io.github.eutro.jwasm.Opcodes;

public class GlobalImportNode extends AbstractImportNode {
    public GlobalTypeNode type;

    public GlobalImportNode(String module, String name, GlobalTypeNode type) {
        super(module, name);
        this.type = type;
    }

    @Override
    public byte importType() {
        return Opcodes.IMPORTS_GLOBAL;
    }

    @Override
    void accept(ImportsVisitor iv) {
        iv.visitGlobalImport(module, name, type.mut, type.type);
    }
}
