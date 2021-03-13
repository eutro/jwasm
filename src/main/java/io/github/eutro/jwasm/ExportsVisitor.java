package io.github.eutro.jwasm;

public class ExportsVisitor extends BaseVisitor<ExportsVisitor> {
    public ExportsVisitor() {
    }

    public ExportsVisitor(ExportsVisitor dl) {
        super(dl);
    }

    public void visitExport(String name, byte type, int index) {
        if (dl != null) dl.visitExport(name, type, index);
    }
}
