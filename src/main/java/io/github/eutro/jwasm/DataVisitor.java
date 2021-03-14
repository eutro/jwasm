package io.github.eutro.jwasm;

public class DataVisitor extends BaseVisitor<DataVisitor> {
    public DataVisitor() {
    }

    public DataVisitor(DataVisitor dl) {
        super(dl);
    }

    public ExprVisitor visitActive(int memory) {
        if (dl != null) dl.visitActive(memory);
        return null;
    }

    public void visitInit(byte[] init) {
        if (dl != null) dl.visitInit(init);
    }
}
