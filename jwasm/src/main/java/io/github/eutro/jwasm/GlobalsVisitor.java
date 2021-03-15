package io.github.eutro.jwasm;

public class GlobalsVisitor extends BaseVisitor<GlobalsVisitor> {
    public GlobalsVisitor() {
    }

    public GlobalsVisitor(GlobalsVisitor dl) {
        super(dl);
    }

    public ExprVisitor visitGlobal(byte mut, byte type) {
        if (dl != null) return dl.visitGlobal(mut, type);
        return null;
    }
}
