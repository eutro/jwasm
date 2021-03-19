package io.github.eutro.jwasm.tree;

import org.jetbrains.annotations.NotNull;

public class ExportNode {
    public @NotNull String name;
    public byte type;
    public int index;

    public ExportNode(@NotNull String name, byte type, int index) {
        this.name = name;
        this.type = type;
        this.index = index;
    }
}
