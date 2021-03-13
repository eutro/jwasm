package io.github.eutro.jwasm;

public class ImportsVisitor extends BaseVisitor<ImportsVisitor> {
    public ImportsVisitor() {
    }

    public ImportsVisitor(ImportsVisitor dl) {
        super(dl);
    }

    public void visitFuncImport(String module, String name, int index) {
        if (dl != null) dl.visitFuncImport(module, name, index);
    }

    public void visitTableImport(String module, String name, int min, int max, byte type) {
        if (dl != null) dl.visitTableImport(module, name, min, max, type);
    }

    public void visitMemImport(String module, String name, int min, int max) {
        if (dl != null) dl.visitMemImport(module, name, min, max);
    }

    public void visitGlobalImport(String module, String name, byte mut, byte type) {
        if (dl != null) dl.visitGlobalImport(module, name, mut, type);
    }
}
