package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ModuleVisitor;

public class CustomNode {
    public String name;
    public byte[] data;

    public CustomNode(String name, byte[] data) {
        this.name = name;
        this.data = data;
    }

    public void accept(ModuleVisitor mv) {
        mv.visitCustom(name, data);
    }
}
