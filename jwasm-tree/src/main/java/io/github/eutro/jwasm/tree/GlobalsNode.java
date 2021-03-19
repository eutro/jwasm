package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.GlobalsVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class GlobalsNode extends GlobalsVisitor implements Iterable<GlobalNode> {
    public @Nullable List<GlobalNode> globals;

    public void accept(GlobalsVisitor gv) {
        if (globals != null) {
            for (GlobalNode global : globals) {
                ExprVisitor ev = gv.visitGlobal(global.type.mut, global.type.type);
                if (ev != null) global.init.accept(ev);
            }
        }
        gv.visitEnd();
    }

    @Override
    public ExprVisitor visitGlobal(byte mut, byte type) {
        if (globals == null) globals = new ArrayList<>();
        ExprNode ev = new ExprNode();
        globals.add(new GlobalNode(new GlobalTypeNode(mut, type), ev));
        return ev;
    }

    @NotNull
    @Override
    public Iterator<GlobalNode> iterator() {
        return globals == null ? Collections.emptyIterator() : globals.iterator();
    }
}
