package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.TypesVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TypesNode extends TypesVisitor {
    public List<TypeNode> types;

    public void accept(TypesVisitor tv) {
        if (types != null) {
            for (TypeNode type : types) {
                tv.visitFuncType(type.params, type.returns);
            }
        }
        tv.visitEnd();
    }

    @Override
    public void visitFuncType(byte @NotNull [] params, byte @NotNull [] returns) {
        if (types == null) types = new ArrayList<>();
        types.add(new TypeNode(params, returns));
    }
}