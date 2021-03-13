package io.github.eutro.jwasm;

public class ExprVisitor extends BaseVisitor<ExprVisitor> {
    public ExprVisitor() {
    }

    public ExprVisitor(ExprVisitor dl) {
        super(dl);
    }

    public void visitInsn(byte opcode) {
        if (dl != null) dl.visitInsn(opcode);
    }

    public void visitPrefixInsn(int opcode) {
        if (dl != null) dl.visitPrefixInsn(opcode);
    }

    public void visitConstInsn(int v) {
        if (dl != null) dl.visitConstInsn(v);
    }

    public void visitConstInsn(long v) {
        if (dl != null) dl.visitConstInsn(v);
    }

    public void visitConstInsn(float v) {
        if (dl != null) dl.visitConstInsn(v);
    }

    public void visitConstInsn(double v) {
        if (dl != null) dl.visitConstInsn(v);
    }

    public void visitNullInsn(byte type) {
        if (dl != null) dl.visitNullInsn(type);
    }

    public void visitFuncInsn(int index) {
        if (dl != null) dl.visitFuncInsn(index);
    }

    public void visitSelectInsn(byte[] type) {
        if (dl != null) dl.visitSelectInsn(type);
    }

    public void visitVariableInsn(byte opcode, int index) {
        if (dl != null) dl.visitVariableInsn(opcode, index);
    }

    public void visitTableInsn(byte opcode, int index) {
        if (dl != null) dl.visitTableInsn(opcode, index);
    }

    public void visitPrefixTableInsn(int opcode, int index) {
        if (dl != null) dl.visitPrefixTableInsn(opcode, index);
    }

    public void visitPrefixTableInsn(int opcode, int sourceIndex, int targetIndex) {
        if (dl != null) dl.visitPrefixTableInsn(opcode, sourceIndex, targetIndex);
    }

    public void visitMemInsn(byte opcode, int align, int offset) {
        if (dl != null) dl.visitMemInsn(opcode, align, offset);
    }

    public void visitMemInsn(byte opcode, int index) {
        if (dl != null) dl.visitMemInsn(opcode, index);
    }

    public ExprVisitor visitBlock(byte opcode, int blockType) {
        if (dl != null) return dl.visitBlock(opcode, blockType);
        return this;
    }

    public void visitElse() {
        if (dl != null) dl.visitElse();
    }

    public void visitBreak(byte opcode, int index) {
        if (dl != null) dl.visitBreak(opcode, index);
    }

    public void visitTableBreak(int[] table, int index) {
        if (dl != null) dl.visitTableBreak(table, index);
    }

    public void visitCall(int index) {
        if (dl != null) dl.visitCall(index);
    }

    public void visitCallIndirect(int table, int index) {
        if (dl != null) dl.visitCallIndirect(table, index);
    }
}
