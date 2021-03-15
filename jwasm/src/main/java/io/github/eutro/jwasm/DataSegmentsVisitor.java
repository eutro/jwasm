package io.github.eutro.jwasm;

public class DataSegmentsVisitor extends BaseVisitor<DataSegmentsVisitor> {
    public DataSegmentsVisitor() {
    }

    public DataSegmentsVisitor(DataSegmentsVisitor dl) {
        super(dl);
    }

    public DataVisitor visitData() {
        if (dl != null) return dl.visitData();
        return null;
    }
}
