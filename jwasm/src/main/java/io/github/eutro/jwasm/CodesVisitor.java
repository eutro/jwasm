package io.github.eutro.jwasm;

public class CodesVisitor extends BaseVisitor<CodesVisitor> {
    public CodesVisitor() {
    }

    public CodesVisitor(CodesVisitor dl) {
        super(dl);
    }

    public ExprVisitor visitCode(byte[] locals) {
        if (dl != null) return dl.visitCode(locals);
        return null;
    }
}
