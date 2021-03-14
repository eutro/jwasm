package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.FunctionsVisitor;

import java.util.ArrayList;
import java.util.List;

public class FunctionsNode extends FunctionsVisitor {
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
}
