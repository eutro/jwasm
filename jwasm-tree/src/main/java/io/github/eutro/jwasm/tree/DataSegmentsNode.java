package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.DataSegmentsVisitor;
import io.github.eutro.jwasm.DataVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DataSegmentsNode extends DataSegmentsVisitor implements Iterable<DataNode> {
    public @Nullable List<DataNode> datas;

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
