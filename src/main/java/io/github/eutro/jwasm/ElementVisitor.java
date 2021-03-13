package io.github.eutro.jwasm;

public class ElementVisitor extends BaseVisitor<ElementVisitor> {
    public ElementVisitor() {
    }

    public ElementVisitor(ElementVisitor dl) {
        super(dl);
    }

    public void visitNonActiveMode(boolean passive) {
        if (dl != null) dl.visitNonActiveMode(passive);
    }

    public ExprVisitor visitActiveMode(int table) {
        if (dl != null) return dl.visitActiveMode(table);
        return null;
    }

    public void visitType(byte type) {
        if (dl != null) dl.visitType(type);
    }

    public ExprVisitor visitInit() {
        if (dl != null) return dl.visitInit();
        return null;
    }
}
