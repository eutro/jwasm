package io.github.eutro.jwasm;

public class DataSegmentsVisitor extends BaseVisitor<DataSegmentsVisitor> {
    public DataSegmentsVisitor() {
    }

    public DataSegmentsVisitor(DataSegmentsVisitor dl) {
        super(dl);
    }

    public void visitData(byte[] init) {
        if (dl != null) dl.visitData(init);
    }

    public ExprVisitor visitActive(int memory) {
        if (dl != null) return dl.visitActive(memory);
        return null;
    }
}
