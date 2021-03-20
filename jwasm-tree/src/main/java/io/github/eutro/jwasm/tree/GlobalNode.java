package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.GlobalsVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * A node that represents a global of a module.
 *
 * @see GlobalsVisitor#visitGlobal(byte, byte)
 * @see GlobalsNode
 */
public class GlobalNode {
    /**
     * The the type of the global.
     */
    public @NotNull GlobalTypeNode type;

    /**
     * The init expression of the global.
     */
    public @NotNull ExprNode init;

    /**
     * Construct a {@link GlobalNode} with the given type and init expression.
     *
     * @param type The the type of the global.
     * @param init The init expression of the global.
     */
    public GlobalNode(@NotNull GlobalTypeNode type, @NotNull ExprNode init) {
        this.type = type;
        this.init = init;
    }

    /**
     * Make the given {@link GlobalsVisitor} visit this global.
     *
     * @param gv The visitor to visit.
     */
    public void accept(GlobalsVisitor gv) {
        ExprVisitor ev = gv.visitGlobal(type.mut, type.type);
        if (ev != null) init.accept(ev);
    }
}
