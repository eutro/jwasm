package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.Limits;

public class MemoryNode {
    public Limits limits;

    public MemoryNode(Limits limits) {
        this.limits = limits;
    }
}
