package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ModuleVisitor;
import io.github.eutro.jwasm.TypesVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
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
     * The vector of {@link TypeNode}s, or {@code null} if there aren't any.
     */
    public @Nullable List<TypeNode> types;

    /**
     * Make the given {@link TypesVisitor} visit all the types of this node.
     *
     * @param tv The visitor to visit.
     */
    public void accept(TypesVisitor tv) {
        if (types != null) {
            for (TypeNode type : types) {
                type.accept(tv);
            }
        }
        tv.visitEnd();
    }

    @Override
    public void visitFuncType(byte @NotNull [] params, byte @NotNull [] returns) {
        if (types == null) types = new ArrayList<>();
        types.add(new TypeNode(params, returns));
    }

    @NotNull
    @Override
    public Iterator<TypeNode> iterator() {
        return types == null ? Collections.emptyIterator() : types.iterator();
    }
}
