package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ElementVisitor;
import io.github.eutro.jwasm.ExprVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ElementNode extends ElementVisitor implements Iterable<ExprNode> {
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
            eev.visitElemIndeces(indeces);
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
        this.passive = passive;
    }

    @Override
    public ExprVisitor visitActiveMode(int table) {
        this.table = table;
        return offset = new ExprNode();
    }

    @Override
    public void visitType(byte type) {
        this.type = type;
    }

    @Override
    public void visitElemIndeces(int[] indeces) {
        this.indeces = indeces;
    }

    @Override
    public ExprVisitor visitInit() {
        if (init == null) init = new ArrayList<>();
        ExprNode en = new ExprNode();
        init.add(en);
        return en;
    }

    @NotNull
    @Override
    public Iterator<ExprNode> iterator() {
        return init == null ? Arrays.stream(indeces).mapToObj(f -> {
            ExprNode en = new ExprNode();
            en.visitFuncInsn(f);
            en.visitEndInsn();
            return en;
        }).iterator() : init.iterator();
    }

    public int size() {
        return init == null ? indeces.length : init.size();
    }
}
