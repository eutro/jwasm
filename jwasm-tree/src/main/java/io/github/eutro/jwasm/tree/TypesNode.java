package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ModuleVisitor;
import io.github.eutro.jwasm.TypesVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A node that represents the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#type-section">type section</a>
 * of a module.
 *
 * @see ModuleVisitor#visitTypes()
 */
public class TypesNode extends TypesVisitor implements Iterable<TypeNode> {
    /**
     * The vector of {@link TypeNode}s.
     */
    public @NotNull List<TypeNode> types = new ArrayList<>();

    /**
     * Construct a visitor with no delegate.
     */
    public TypesNode() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    public TypesNode(@Nullable TypesVisitor dl) {
        super(dl);
    }

    /**
     * Make the given {@link TypesVisitor} visit all the types of this node.
     *
     * @param tv The visitor to visit.
     */
    public void accept(TypesVisitor tv) {
        for (TypeNode type : types) {
            type.accept(tv);
        }
        tv.visitEnd();
    }

    @Override
    public void visitFuncType(byte @NotNull [] params, byte @NotNull [] returns) {
        super.visitFuncType(params, returns);
        types.add(new TypeNode(params, returns));
    }

    @NotNull
    @Override
    public Iterator<TypeNode> iterator() {
        return types.iterator();
    }
}
