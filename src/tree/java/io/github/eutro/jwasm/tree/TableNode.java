package io.github.eutro.jwasm.tree;

public class TableNode {
    public LimitsNode limits;
    public byte type;

    public TableNode(LimitsNode limits, byte type) {
        this.limits = limits;
        this.type = type;
    }
}
