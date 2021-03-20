package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.CodesVisitor;
import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.ModuleVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
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
     * The vector of {@link CodeNode}s, or {@code null} if there aren't any.
     */
    public @Nullable List<CodeNode> codes;

    /**
     * Make the given {@link CodesVisitor} visit the codes of this node.
     *
     * @param cv The visitor to visit.
     */
    public void accept(CodesVisitor cv) {
        if (codes != null) {
            for (CodeNode code : codes) {
                ExprVisitor ev = cv.visitCode(code.locals);
                if (ev != null) code.expr.accept(ev);
            }
        }
        cv.visitEnd();
    }

    @Override
    public @Nullable ExprVisitor visitCode(byte @NotNull [] locals) {
        if (codes == null) codes = new ArrayList<>();
        ExprNode en = new ExprNode();
        codes.add(new CodeNode(locals, en));
        return en;
    }

    @NotNull
    @Override
    public Iterator<CodeNode> iterator() {
        return codes == null ? Collections.emptyIterator() : codes.iterator();
    }
}
