package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.DataSegmentsVisitor;
import io.github.eutro.jwasm.DataVisitor;

import java.util.ArrayList;
import java.util.List;

public class DataSegmentsNode extends DataSegmentsVisitor {
    public List<DataNode> datas;

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
}
