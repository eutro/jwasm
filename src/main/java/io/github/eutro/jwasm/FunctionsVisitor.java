package io.github.eutro.jwasm;

public class FunctionsVisitor extends BaseVisitor<FunctionsVisitor> {
    public FunctionsVisitor() {
    }

    public FunctionsVisitor(FunctionsVisitor dl) {
        super(dl);
    }

    public ExprVisitor visitFunc(int type, byte[] locals) {
        if (dl != null) return dl.visitFunc(type, locals);
        return null;
    }
}
