package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.DataSegmentsVisitor;
import io.github.eutro.jwasm.DataVisitor;
import io.github.eutro.jwasm.ModuleVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A node that represents the
 * <a href="https://webassembly.github.io/spec/core/binary/modules.html#data-section">data section</a>
 * of a module.
 *
 * @see ModuleVisitor#visitDatas()
 * @see DataNode
 */
public class DataSegmentsNode extends DataSegmentsVisitor implements Iterable<DataNode> {
    /**
     * The vector of {@link DataNode}s, or {@code null} if there aren't any.
     */
    public @Nullable List<DataNode> datas;

    /**
     * Make the given {@link DataSegmentsVisitor} visit all the data segments of this node.
     *
     * @param dv The visitor to visit.
     */
    public void accept(DataSegmentsVisitor dv) {
        if (datas != null) {
            for (DataNode data : datas) {
                DataVisitor ddv = dv.visitData();
                if (ddv != null) data.accept(ddv);
            }
        }
        dv.visitEnd();
    }

    @Override
    public DataVisitor visitData() {
        if (datas == null) datas = new ArrayList<>();
        DataNode dn = new DataNode(null);
        datas.add(dn);
        return dn;
    }

    @NotNull
    @Override
    public Iterator<DataNode> iterator() {
        return datas == null ? Collections.emptyIterator() : datas.iterator();
    }
}
