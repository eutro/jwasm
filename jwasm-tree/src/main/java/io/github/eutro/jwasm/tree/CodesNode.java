package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.CodesVisitor;
import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.ModuleVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A node that represents the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#code-section">code section</a>
 * of a module.
 *
 * @see ModuleVisitor#visitCode()
 * @see CodeNode
 */
public class CodesNode extends CodesVisitor implements Iterable<CodeNode> {
    /**
     * The vector of {@link CodeNode}s.
     */
    public @NotNull List<CodeNode> codes = new ArrayList<>();

    /**
     * Construct a visitor with no delegate.
     */
    public CodesNode() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    public CodesNode(@Nullable CodesVisitor dl) {
        super(dl);
    }

    /**
     * Make the given {@link CodesVisitor} visit the codes of this node.
     *
     * @param cv The visitor to visit.
     */
    public void accept(CodesVisitor cv) {
        for (CodeNode code : codes) {
            ExprVisitor ev = cv.visitCode(code.locals);
            if (ev != null) code.expr.accept(ev);
        }
        cv.visitEnd();
    }

    @Override
    public @Nullable ExprVisitor visitCode(byte @NotNull [] locals) {
        super.visitCode(locals);
        ExprNode en = new ExprNode();
        codes.add(new CodeNode(locals, en));
        return en;
    }

    @NotNull
    @Override
    public Iterator<CodeNode> iterator() {
        return codes.iterator();
    }
}
