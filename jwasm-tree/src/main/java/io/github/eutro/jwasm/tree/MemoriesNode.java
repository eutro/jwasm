package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.Limits;
import io.github.eutro.jwasm.MemoriesVisitor;
import io.github.eutro.jwasm.ModuleVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A node that represents the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#memory-section">memory section</a>
 * of a module.
 *
 * @see ModuleVisitor#visitMems()
 * @see MemoryNode
 */
public class MemoriesNode extends MemoriesVisitor implements Iterable<MemoryNode> {
    /**
     * The vector of {@link MemoryNode}s, or {@code null} if there aren't any.
     */
    public @Nullable List<MemoryNode> memories;

    /**
     * Make the given {@link MemoriesVisitor} visit all the memories of this node.
     *
     * @param mmv The visitor to visit.
     */
    public void accept(MemoriesVisitor mmv) {
        if (memories != null) {
            for (MemoryNode memory : memories) {
                memory.accept(mmv);
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
