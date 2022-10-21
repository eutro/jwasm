package io.github.eutro.jwasm.sexp.wast;

import io.github.eutro.jwasm.BaseVisitor;
import org.jetbrains.annotations.Nullable;

public class WastModuleVisitor extends BaseVisitor<WastModuleVisitor> {
    public WastModuleVisitor() {
    }

    public WastModuleVisitor(@Nullable WastModuleVisitor dl) {
        super(dl);
    }

    public void visitWatModule(Object module) {
        if (dl != null) dl.visitWatModule(module);
    }

    public void visitBinaryModule(Object module) {
        if (dl != null) dl.visitBinaryModule(module);
    }

    public void visitQuoteModule(Object module) {
        if (dl != null) dl.visitQuoteModule(module);
    }
}
