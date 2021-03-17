package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.DataVisitor;
import io.github.eutro.jwasm.ExprVisitor;
import org.jetbrains.annotations.Nullable;

public class DataNode extends DataVisitor {
    public byte[] init;
    public int memory;
    public ExprNode offset;

    public DataNode(byte[] init) {
        this.init = init;
    }

    public DataNode(byte[] init, ExprNode offset) {
        this.init = init;
        this.offset = offset;
    }

    public DataNode(byte[] init, int memory, ExprNode offset) {
        this.init = init;
        this.memory = memory;
        this.offset = offset;
    }

    public void accept(DataVisitor dv) {
        if (offset != null) {
            ExprVisitor ev = dv.visitActive(memory);
            if (ev != null) offset.accept(ev);
        }
        dv.visitEnd();
    }

    @Override
    public @Nullable ExprVisitor visitActive(int memory) {
        this.memory = memory;
        return offset = new ExprNode();
    }

    @Override
    public void visitInit(byte[] init) {
        this.init = init;
    }
}
