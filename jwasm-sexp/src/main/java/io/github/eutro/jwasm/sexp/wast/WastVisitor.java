package io.github.eutro.jwasm.sexp.wast;

import io.github.eutro.jwasm.BaseVisitor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WastVisitor extends BaseVisitor<WastVisitor> {
    public WastVisitor() {
    }

    public WastVisitor(@Nullable WastVisitor dl) {
        super(dl);
    }

    public @Nullable WastModuleVisitor visitModule() {
        if (dl != null) return dl.visitModule();
        return null;
    }

    public void visitRegister(String string, @Nullable String name) {
        if (dl != null) dl.visitRegister(string, name);
    }

    public @Nullable ActionVisitor visitTopAction() {
        if (dl != null) return dl.visitTopAction();
        return null;
    }

    public @Nullable ActionVisitor visitAssertReturn(Object... results) {
        if (dl != null) return dl.visitAssertReturn(results);
        return null;
    }

    public @Nullable ActionVisitor visitAssertTrap(String failure) {
        if (dl != null) return dl.visitAssertTrap(failure);
        return null;
    }

    public @Nullable ActionVisitor visitAssertExhaustion(String failure) {
        if (dl != null) return dl.visitAssertExhaustion(failure);
        return null;
    }

    public @Nullable WastModuleVisitor visitAssertMalformed(String failure) {
        if (dl != null) return dl.visitAssertMalformed(failure);
        return null;
    }

    public @Nullable WastModuleVisitor visitAssertInvalid(String failure) {
        if (dl != null) return dl.visitAssertInvalid(failure);
        return null;
    }

    public @Nullable WastModuleVisitor visitAssertUnlinkable(String failure) {
        if (dl != null) return dl.visitAssertUnlinkable(failure);
        return null;
    }

    public @Nullable WastModuleVisitor visitAssertModuleTrap(String failure) {
        if (dl != null) return dl.visitAssertModuleTrap(failure);
        return null;
    }

    public @Nullable WastVisitor visitMetaScript(@Nullable String name) {
        if (dl != null) return dl.visitMetaScript(name);
        return null;
    }

    public void visitMetaInput(@Nullable String name, String string) {
        if (dl != null) dl.visitMetaInput(name, string);
    }

    public void visitMetaOutput(@Nullable String name, String string) {
        if (dl != null) dl.visitMetaOutput(name, string);
    }

    public void visitOther(String macro, List<?> fullSexp) {
        if (dl != null) dl.visitOther(macro, fullSexp);
    }
}
