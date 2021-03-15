package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

import java.util.LinkedList;

public class ExprNode extends ExprVisitor {
    public LinkedList<AbstractInsnNode> instructions;

    public void accept(ExprVisitor ev) {
        if (instructions != null) {
            for (AbstractInsnNode next : instructions) {
                if (next instanceof EndInsnNode) break;
                next.accept(ev);
            }
        }
        ev.visitEnd();
    }

    private LinkedList<AbstractInsnNode> insns() {
        if (instructions == null) instructions = new LinkedList<>();
        return instructions;
    }

    @Override
    public void visitInsn(byte opcode) {
        insns().addLast(new InsnNode(opcode));
    }

    @Override
    public void visitPrefixInsn(int opcode) {
        insns().addLast(new PrefixInsnNode(opcode));
    }

    @Override
    public void visitConstInsn(Object v) {
        insns().addLast(new ConstInsnNode(v));
    }

    @Override
    public void visitNullInsn(byte type) {
        insns().addLast(new NullInsnNode(type));
    }

    @Override
    public void visitFuncInsn(int index) {
        insns().addLast(new FuncInsnNode(index));
    }

    @Override
    public void visitSelectInsn(byte[] type) {
        insns().addLast(new SelectInsnNode(type));
    }

    @Override
    public void visitVariableInsn(byte opcode, int index) {
        insns().addLast(new VariableInsnNode(opcode, index));
    }

    @Override
    public void visitTableInsn(byte opcode, int index) {
        insns().addLast(new TableInsnNode(opcode, index));
    }

    @Override
    public void visitPrefixTableInsn(int opcode, int index) {
        insns().addLast(new PrefixTableInsnNode(opcode, index));
    }

    @Override
    public void visitPrefixBinaryTableInsn(int opcode, int sourceIndex, int targetIndex) {
        insns().addLast(new PrefixBinaryTableInsnNode(opcode, sourceIndex, targetIndex));
    }

    @Override
    public void visitMemInsn(byte opcode, int align, int offset) {
        insns().addLast(new MemInsnNode(opcode, align, offset));
    }

    @Override
    public void visitIndexedMemInsn(int opcode, int index) {
        insns().addLast(new IndexedMemInsnNode(opcode, index));
    }

    @Override
    public void visitBlockInsn(byte opcode, int blockType) {
        insns().addLast(new BlockInsnNode(opcode, blockType));
    }

    @Override
    public void visitElseInsn() {
        insns().addLast(new ElseInsnNode());
    }

    @Override
    public void visitEndInsn() {
        insns().addLast(new EndInsnNode());
    }

    @Override
    public void visitBreakInsn(byte opcode, int index) {
        insns().addLast(new BreakInsnNode(opcode, index));
    }

    @Override
    public void visitTableBreakInsn(int[] table, int index) {
        insns().addLast(new TableBreakInsnNode(table, index));
    }

    @Override
    public void visitCallInsn(int index) {
        insns().addLast(new CallInsnNode(index));
    }

    @Override
    public void visitCallIndirectInsn(int table, int index) {
        insns().addLast(new CallIndirectInsnNode(table, index));
    }
}
