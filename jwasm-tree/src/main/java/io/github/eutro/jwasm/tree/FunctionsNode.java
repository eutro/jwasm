package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.FunctionsVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FunctionsNode extends FunctionsVisitor implements Iterable<FuncNode> {
    public List<FuncNode> funcs;

    public void accept(FunctionsVisitor fv) {
        if (funcs != null) {
            for (FuncNode func : funcs) {
                fv.visitFunc(func.type);
            }
        }
        fv.visitEnd();
    }

    @Override
    public void visitFunc(int type) {
        if (funcs == null) funcs = new ArrayList<>();
        funcs.add(new FuncNode(type));
    }

    @NotNull
    @Override
    public Iterator<FuncNode> iterator() {
        return funcs == null ? Collections.emptyIterator() : funcs.iterator();
    }
}
