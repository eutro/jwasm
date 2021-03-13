package io.github.eutro.jwasm;

public class ElementSegmentsVisitor extends BaseVisitor<ElementSegmentsVisitor> {
    protected ElementSegmentsVisitor() {
        super();
    }

    protected ElementSegmentsVisitor(ElementSegmentsVisitor dl) {
        super(dl);
    }

    public ElementVisitor visitElem() {
        if (dl != null) return dl.visitElem();
        return null;
    }
}
