package io.github.eutro.jwasm;

public class MemoriesVisitor extends BaseVisitor<MemoriesVisitor> {
    public MemoriesVisitor() {
    }

    public MemoriesVisitor(MemoriesVisitor dl) {
        super(dl);
    }

    public void visitMemory(int min, int max) {
        if (dl != null) dl.visitMemory(min, max);
    }
}
