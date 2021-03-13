package io.github.eutro.jwasm;

public class BaseVisitor<T extends BaseVisitor<T>> {
    protected T dl;

    protected BaseVisitor() {
    }

    protected BaseVisitor(T dl) {
        this.dl = dl;
    }

    public void visitEnd() {
        if (dl != null) dl.visitEnd();
    }
}
