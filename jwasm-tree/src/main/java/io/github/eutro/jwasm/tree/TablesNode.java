package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.CodesVisitor;
import io.github.eutro.jwasm.Limits;
import io.github.eutro.jwasm.ModuleVisitor;
import io.github.eutro.jwasm.TablesVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A node that represents the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#table-section">table section</a>
 * of a module.
 *
 * @see ModuleVisitor#visitTables()
 */
public class TablesNode extends TablesVisitor implements Iterable<TableNode> {
    /**
     * The vector of {@link TableNode}s, or {@code null} if there aren't any.
     */
    public @Nullable List<TableNode> tables;

    /**
     * Construct a visitor with no delegate.
     */
    public TablesNode() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    public TablesNode(@Nullable TablesVisitor dl) {
        super(dl);
    }

    /**
     * Make the given {@link TablesVisitor} visit all the tables of this node.
     *
     * @param tv The visitor to visit.
     */
    public void accept(TablesVisitor tv) {
        if (tables != null) {
            for (TableNode table : tables) {
                table.accept(tv);
            }
        }
        tv.visitEnd();
    }

    @Override
    public void visitTable(int min, Integer max, byte type) {
        super.visitTable(min, max, type);
        if (tables == null) tables = new ArrayList<>();
        tables.add(new TableNode(new Limits(min, max), type));
    }

    @NotNull
    @Override
    public Iterator<TableNode> iterator() {
        return tables == null ? Collections.emptyIterator() : tables.iterator();
    }
}
