package io.github.eutro.jwasm;

import org.jetbrains.annotations.Nullable;

public class DataVisitor extends BaseVisitor<DataVisitor> {
    public DataVisitor() {
    }

    public DataVisitor(@Nullable DataVisitor dl) {
        super(dl);
    }

    public ExprVisitor visitActive(int memory) {
        if (dl != null) dl.visitActive(memory);
        return null;
    }

    public void visitInit(byte[] init) {
        if (dl != null) dl.visitInit(init);
    }
}
