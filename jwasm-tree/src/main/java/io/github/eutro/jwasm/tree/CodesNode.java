package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.CodesVisitor;
import io.github.eutro.jwasm.ExprVisitor;

import java.util.ArrayList;
import java.util.List;

public class CodesNode extends CodesVisitor {
    public List<CodeNode> codes;

    public void accept(CodesVisitor cv) {
        if (codes != null) {
            for (CodeNode code : codes) {
                ExprVisitor ev = cv.visitCode(code.locals);
                if (ev != null) code.expr.accept(ev);
            }
        }
        cv.visitEnd();
    }

    @Override
    public ExprVisitor visitCode(byte[] locals) {
        if (codes == null) codes = new ArrayList<>();
        ExprNode en = new ExprNode();
        codes.add(new CodeNode(locals, en));
        return en;
    }
}
