package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.Limits;
import io.github.eutro.jwasm.MemoriesVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MemoriesNode extends MemoriesVisitor implements Iterable<MemoryNode> {
    public @Nullable List<MemoryNode> memories;

    public void accept(MemoriesVisitor mmv) {
        if (memories != null) {
            for (MemoryNode memory : memories) {
                mmv.visitMemory(memory.limits.min, memory.limits.max);
            }
        }
        mmv.visitEnd();
    }

    @Override
    public void visitMemory(int min, Integer max) {
        if (memories == null) memories = new ArrayList<>();
        memories.add(new MemoryNode(new Limits(min, max)));
    }

    @NotNull
    @Override
    public Iterator<MemoryNode> iterator() {
        return memories == null ? Collections.emptyIterator() : memories.iterator();
    }
}
