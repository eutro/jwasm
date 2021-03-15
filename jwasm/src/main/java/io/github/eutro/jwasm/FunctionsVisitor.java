package io.github.eutro.jwasm;

public class FunctionsVisitor extends BaseVisitor<FunctionsVisitor> {
    public FunctionsVisitor() {
    }

    public FunctionsVisitor(FunctionsVisitor dl) {
        super(dl);
    }

    public void visitFunc(int type) {
        if (dl != null) dl.visitFunc(type);
    }
}
