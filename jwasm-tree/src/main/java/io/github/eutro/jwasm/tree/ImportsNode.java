package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ImportsVisitor;

import java.util.ArrayList;
import java.util.List;

public class ImportsNode extends ImportsVisitor {
    public List<AbstractImportNode> imports;

    public void accept(ImportsVisitor iv) {
        if (imports != null) {
            for (AbstractImportNode anImport : imports) {
                anImport.accept(iv);
            }
        }
        iv.visitEnd();
    }

    @Override
    public void visitFuncImport(String module, String name, int index) {
        if (imports == null) imports = new ArrayList<>();
        imports.add(new FuncImportNode(module, name, index));
    }

    @Override
    public void visitTableImport(String module, String name, int min, Integer max, byte type) {
        if (imports == null) imports = new ArrayList<>();
        imports.add(new TableImportNode(module, name, new LimitsNode(min, max), type));
    }

    @Override
    public void visitMemImport(String module, String name, int min, Integer max) {
        if (imports == null) imports = new ArrayList<>();
        imports.add(new MemImportNode(module, name, new LimitsNode(min, max)));
    }

    @Override
    public void visitGlobalImport(String module, String name, byte mut, byte type) {
        if (imports == null) imports = new ArrayList<>();
        imports.add(new GlobalImportNode(module, name, new GlobalTypeNode(mut, type)));
    }
}
