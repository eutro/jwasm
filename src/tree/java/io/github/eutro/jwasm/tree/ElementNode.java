package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ElementVisitor;
import io.github.eutro.jwasm.ExprVisitor;

import java.util.ArrayList;
import java.util.List;

public class ElementNode extends ElementVisitor {
    public boolean passive;
    public int table;
    public ExprNode offset;
    public byte type;
    public int[] indeces;
    public List<ExprNode> init;

    public void accept(ElementVisitor eev) {
        if (offset == null) {
            eev.visitNonActiveMode(passive);
        } else {
            ExprVisitor ev = eev.visitActiveMode(table);
            if (ev != null) offset.accept(ev);
        }
        eev.visitType(type);
        if (indeces != null) {
            eev.visitElemIneces(indeces);
        } else if (init != null) {
            for (ExprNode en : init) {
                ExprVisitor ev = eev.visitInit();
                if (ev != null) en.accept(ev);
            }
        }
        eev.visitEnd();
    }

    @Override
    public void visitNonActiveMode(boolean passive) {
        super.visitNonActiveMode(passive);
    }

    @Override
    public ExprVisitor visitActiveMode(int table) {
        return super.visitActiveMode(table);
    }

    @Override
    public void visitType(byte type) {
        super.visitType(type);
    }

    @Override
    public void visitElemIneces(int[] indeces) {
        this.indeces = indeces;
    }

    @Override
    public ExprVisitor visitInit() {
        if (init == null) init = new ArrayList<>();
        ExprNode en = new ExprNode();
        init.add(en);
        return en;
    }
}
