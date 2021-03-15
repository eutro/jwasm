package io.github.eutro.jwasm.tree;

public class ExportNode {
    public String name;
    public byte type;
    public int index;

    public ExportNode(String name, byte type, int index) {
        this.name = name;
        this.type = type;
        this.index = index;
    }
}
