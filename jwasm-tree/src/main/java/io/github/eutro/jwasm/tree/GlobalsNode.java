package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.GlobalsVisitor;
import io.github.eutro.jwasm.ModuleVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A node that represents the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#global-section">global section</a>
 * of a module.
 *
 * @see ModuleVisitor#visitGlobals()
 */
public class GlobalsNode extends GlobalsVisitor implements Iterable<GlobalNode> {
    /**
     * The vector of {@link GlobalNode}s, or {@code null} if there aren't any.
     */
    public @Nullable List<GlobalNode> globals;

    /**
     * Make the given {@link GlobalsVisitor} visit all the globals of this node.
     *
     * @param gv The visitor to visit.
     */
    public void accept(GlobalsVisitor gv) {
        if (globals != null) {
            for (GlobalNode global : globals) {
                global.accept(gv);
            }
        }
        gv.visitEnd();
    }

    @Override
    public ExprVisitor visitGlobal(byte mut, byte type) {
        if (globals == null) globals = new ArrayList<>();
        ExprNode ev = new ExprNode();
        globals.add(new GlobalNode(new GlobalTypeNode(mut, type), ev));
        return ev;
    }

    @NotNull
    @Override
    public Iterator<GlobalNode> iterator() {
        return globals == null ? Collections.emptyIterator() : globals.iterator();
    }
}
