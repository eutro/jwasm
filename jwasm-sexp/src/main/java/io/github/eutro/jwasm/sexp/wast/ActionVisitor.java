package io.github.eutro.jwasm.sexp.wast;

import io.github.eutro.jwasm.BaseVisitor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ActionVisitor extends BaseVisitor<ActionVisitor> {
    public ActionVisitor() {
    }

    public ActionVisitor(@Nullable ActionVisitor dl) {
        super(dl);
    }

    public void visitInvoke(@Nullable String name, String string, Object... args) {
        if (dl != null) dl.visitInvoke(name, string, args);
    }

    public void visitGet(@Nullable String name, String string) {
        if (dl != null) dl.visitGet(name, string);
    }

    public void visitOther(String macro, List<?> fullSexp) {
        if (dl != null) dl.visitOther(macro, fullSexp);
    }
}
