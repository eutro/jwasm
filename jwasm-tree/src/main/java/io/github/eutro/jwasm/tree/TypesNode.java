package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.TypesVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TypesNode extends TypesVisitor implements Iterable<TypeNode> {
    public @Nullable List<TypeNode> types;

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

    @NotNull
    @Override
    public Iterator<TypeNode> iterator() {
        return types == null ? Collections.emptyIterator() : types.iterator();
    }
}
