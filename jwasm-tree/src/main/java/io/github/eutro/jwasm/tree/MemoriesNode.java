package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.Limits;
import io.github.eutro.jwasm.MemoriesVisitor;

import java.util.ArrayList;
import java.util.List;

public class MemoriesNode extends MemoriesVisitor {
    public List<MemoryNode> memories;

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
}
