package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ImportsVisitor;
import io.github.eutro.jwasm.Limits;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public void visitFuncImport(@NotNull String module, @NotNull String name, int index) {
        if (imports == null) imports = new ArrayList<>();
        imports.add(new FuncImportNode(module, name, index));
    }

    @Override
    public void visitTableImport(@NotNull String module, @NotNull String name, int min, @Nullable Integer max, byte type) {
        if (imports == null) imports = new ArrayList<>();
        imports.add(new TableImportNode(module, name, new Limits(min, max), type));
    }

    @Override
    public void visitMemImport(@NotNull String module, @NotNull String name, int min, @Nullable Integer max) {
        if (imports == null) imports = new ArrayList<>();
        imports.add(new MemImportNode(module, name, new Limits(min, max)));
    }

    @Override
    public void visitGlobalImport(@NotNull String module, @NotNull String name, byte mut, byte type) {
        if (imports == null) imports = new ArrayList<>();
        imports.add(new GlobalImportNode(module, name, new GlobalTypeNode(mut, type)));
    }
}
