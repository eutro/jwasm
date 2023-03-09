package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.BlockType;
import io.github.eutro.jwasm.ExprVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A node that represents an
 * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-expr">expression</a>.
 */
public class ExprNode extends ExprVisitor implements Iterable<AbstractInsnNode> {
    /**
     * The list of {@link AbstractInsnNode instructions} in this expression, or {@code null} if there aren't any.
     */
    public @Nullable List<AbstractInsnNode> instructions;

    /**
     * The argument to the last call to {@link #visitPc(long)}.
     */
    private long lastPc = -1;

    /**
     * Construct a visitor with no delegate.
     */
    public ExprNode() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    public ExprNode(@Nullable ExprVisitor dl) {
        super(dl);
    }

    /**
     * Make the given {@link ExprVisitor} visit all the instructions of this node.
     *
     * @param ev The visitor to visit.
     */
    public void accept(ExprVisitor ev) {
        if (instructions != null) {
            for (AbstractInsnNode next : instructions) {
                next.accept(ev);
            }
        }
        ev.visitEnd();
    }

    private void insn(AbstractInsnNode insn) {
        if (instructions == null) instructions = new ArrayList<>();
        insn.pc = lastPc;
        lastPc = -1;
        instructions.add(insn);
    }

    @Override
    public void visitPc(long pc) {
        super.visitPc(pc);
        lastPc = pc;
    }

    @Override
    public void visitInsn(byte opcode) {
        super.visitInsn(opcode);
        insn(new InsnNode(opcode));
    }

    @Override
    public void visitPrefixInsn(int opcode) {
        super.visitPrefixInsn(opcode);
        insn(new PrefixInsnNode(opcode));
    }

    @Override
    public void visitConstInsn(Object v) {
        super.visitConstInsn(v);
        insn(new ConstInsnNode(v));
    }

    @Override
    public void visitNullInsn(byte type) {
        super.visitNullInsn(type);
        insn(new NullInsnNode(type));
    }

    @Override
    public void visitFuncRefInsn(int function) {
        super.visitFuncRefInsn(function);
        insn(new FuncRefInsnNode(function));
    }

    @Override
    public void visitSelectInsn(byte[] type) {
        super.visitSelectInsn(type);
        insn(new SelectInsnNode(type));
    }

    @Override
    public void visitVariableInsn(byte opcode, int variable) {
        super.visitVariableInsn(opcode, variable);
        insn(new VariableInsnNode(opcode, variable));
    }

    @Override
    public void visitTableInsn(byte opcode, int table) {
        super.visitTableInsn(opcode, table);
        insn(new TableInsnNode(opcode, table));
    }

    @Override
    public void visitPrefixTableInsn(int opcode, int table) {
        super.visitPrefixTableInsn(opcode, table);
        insn(new PrefixTableInsnNode(opcode, table));
    }

    @Override
    public void visitPrefixBinaryTableInsn(int opcode, int firstIndex, int secondIndex) {
        super.visitPrefixBinaryTableInsn(opcode, firstIndex, secondIndex);
        insn(new PrefixBinaryTableInsnNode(opcode, firstIndex, secondIndex));
    }

    @Override
    public void visitMemInsn(byte opcode, int align, int offset) {
        super.visitMemInsn(opcode, align, offset);
        insn(new MemInsnNode(opcode, align, offset));
    }

    @Override
    public void visitIndexedMemInsn(int opcode, int index) {
        super.visitIndexedMemInsn(opcode, index);
        insn(new IndexedMemInsnNode(opcode, index));
    }

    @Override
    public void visitBlockInsn(byte opcode, BlockType blockType) {
        super.visitBlockInsn(opcode, blockType);
        insn(new BlockInsnNode(opcode, blockType));
    }

    @Override
    public void visitElseInsn() {
        super.visitElseInsn();
        insn(new ElseInsnNode());
    }

    @Override
    public void visitEndInsn() {
        super.visitEndInsn();
        insn(new EndInsnNode());
    }

    @Override
    public void visitBreakInsn(byte opcode, int label) {
        super.visitBreakInsn(opcode, label);
        insn(new BreakInsnNode(opcode, label));
    }

    @Override
    public void visitTableBreakInsn(int[] labels, int defaultLabel) {
        super.visitTableBreakInsn(labels, defaultLabel);
        insn(new TableBreakInsnNode(labels, defaultLabel));
    }

    @Override
    public void visitCallInsn(int function) {
        super.visitCallInsn(function);
        insn(new CallInsnNode(function));
    }

    @Override
    public void visitCallIndirectInsn(int table, int type) {
        super.visitCallIndirectInsn(table, type);
        insn(new CallIndirectInsnNode(table, type));
    }

    @Override
    public void visitVectorInsn(int opcode) {
        super.visitVectorInsn(opcode);
        insn(new VectorInsnNode(opcode));
    }

    @Override
    public void visitVectorMemInsn(int opcode, int align, int offset) {
        super.visitVectorMemInsn(opcode, align, offset);
        insn(new VectorMemInsnNode(opcode, align, offset));
    }

    @Override
    public void visitVectorMemLaneInsn(int opcode, int align, int offset, byte lane) {
        super.visitVectorMemLaneInsn(opcode, align, offset, lane);
        insn(new VectorMemLaneInsnNode(opcode, align, offset, lane));
    }

    @Override
    public void visitVectorConstOrShuffleInsn(int opcode, byte[] bytes) {
        super.visitVectorConstOrShuffleInsn(opcode, bytes);
        insn(new VectorConstOrShuffleInsnNode(opcode, bytes));
    }

    @Override
    public void visitVectorLaneInsn(int opcode, byte lane) {
        super.visitVectorLaneInsn(opcode, lane);
        insn(new VectorLaneInsnNode(opcode, lane));
    }

    @NotNull
    @Override
    public Iterator<AbstractInsnNode> iterator() {
        return instructions == null ? Collections.emptyIterator() : instructions.iterator();
    }
}
