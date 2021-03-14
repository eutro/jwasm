package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ElementSegmentsVisitor;
import io.github.eutro.jwasm.ElementVisitor;

import java.util.ArrayList;
import java.util.List;

public class ElementSegmentsNode extends ElementSegmentsVisitor {
    public List<ElementNode> elems;

    public void accept(ElementSegmentsVisitor ev) {
        if (elems != null) {
            for (ElementNode elem : elems) {
                ElementVisitor eev = ev.visitElem();
                if (eev != null) elem.accept(eev);
            }
        }
        ev.visitEnd();
    }

    @Override
    public ElementVisitor visitElem() {
        if (elems == null) elems = new ArrayList<>();
        ElementNode en = new ElementNode();
        elems.add(en);
        return en;
    }
}
