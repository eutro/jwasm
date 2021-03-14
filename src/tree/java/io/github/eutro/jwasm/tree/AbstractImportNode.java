package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ImportsVisitor;

public abstract class AbstractImportNode {
    public String module;
    public String name;

    public AbstractImportNode(String module, String name) {
        this.module = module;
        this.name = name;
    }

    abstract void accept(ImportsVisitor iv);
}
