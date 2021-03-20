package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.Limits;
import io.github.eutro.jwasm.MemoriesVisitor;

/**
 * A node that represents a linear memory of a module.
 *
 * @see MemoriesVisitor#visitMemory(int, Integer)
 */
public class MemoryNode {
    /**
     * The limits of the memory.
     */
    public Limits limits;

    /**
     * Construct a {@link MemoryNode} with the given limits.
     *
     * @param limits The limits of the memory.
     */
    public MemoryNode(Limits limits) {
        this.limits = limits;
    }

    /**
     * Make the given {@link MemoriesVisitor} visit this memory.
     *
     * @param mmv The visitor to visit.
     */
    public void accept(MemoriesVisitor mmv) {
        mmv.visitMemory(limits.min, limits.max);
    }
}
