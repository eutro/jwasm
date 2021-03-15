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

    public void visitConstInsn(Object v) {
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

    public void visitPrefixBinaryTableInsn(int opcode, int sourceIndex, int targetIndex) {
        if (dl != null) dl.visitPrefixBinaryTableInsn(opcode, sourceIndex, targetIndex);
    }

    public void visitMemInsn(byte opcode, int align, int offset) {
        if (dl != null) dl.visitMemInsn(opcode, align, offset);
    }

    public void visitIndexedMemInsn(int opcode, int index) {
        if (dl != null) dl.visitIndexedMemInsn(opcode, index);
    }

    public void visitBlockInsn(byte opcode, int blockType) {
        if (dl != null) dl.visitBlockInsn(opcode, blockType);
    }

    public void visitElseInsn() {
        if (dl != null) dl.visitElseInsn();
    }

    public void visitEndInsn() {
        if (dl != null) dl.visitEndInsn();
    }

    public void visitBreakInsn(byte opcode, int index) {
        if (dl != null) dl.visitBreakInsn(opcode, index);
    }

    public void visitTableBreakInsn(int[] table, int index) {
        if (dl != null) dl.visitTableBreakInsn(table, index);
    }

    public void visitCallInsn(int index) {
        if (dl != null) dl.visitCallInsn(index);
    }

    public void visitCallIndirectInsn(int table, int index) {
        if (dl != null) dl.visitCallIndirectInsn(table, index);
    }
}
