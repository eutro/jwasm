package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.CodesVisitor;
import io.github.eutro.jwasm.FunctionsVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A node that represents the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#function-section">function section</a>
 * of a module.
 *
 * @see ModuleNode#visitFuncs()
 */
public class FunctionsNode extends FunctionsVisitor implements Iterable<FuncNode> {
    /**
     * The vector of {@link FuncNode}s in this module.
     */
    public @Nullable List<FuncNode> funcs;

    /**
     * Construct a visitor with no delegate.
     */
    public FunctionsNode() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    public FunctionsNode(@Nullable FunctionsVisitor dl) {
        super(dl);
    }

    /**
     * Make the given {@link FunctionsVisitor} visit all the function types of this node.
     *
     * @param fv The visitor to visit.
     */
    public void accept(FunctionsVisitor fv) {
        if (funcs != null) {
            for (FuncNode func : funcs) {
                func.accept(fv);
            }
        }
        fv.visitEnd();
    }

    @Override
    public void visitFunc(int type) {
        super.visitFunc(type);
        if (funcs == null) funcs = new ArrayList<>();
        funcs.add(new FuncNode(type));
    }

    @NotNull
    @Override
    public Iterator<FuncNode> iterator() {
        return funcs == null ? Collections.emptyIterator() : funcs.iterator();
    }
}
