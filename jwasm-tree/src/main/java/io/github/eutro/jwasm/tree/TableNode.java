package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.Limits;

public class TableNode {
    public Limits limits;
    public byte type;

    public TableNode(Limits limits, byte type) {
        this.limits = limits;
        this.type = type;
    }
}
