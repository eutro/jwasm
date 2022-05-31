package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.BlockType;
import io.github.eutro.jwasm.ExprVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A node that represents an
 * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-expr">expression</a>.
 */
public class ExprNode extends ExprVisitor implements Iterable<AbstractInsnNode> {
    /**
     * The list of {@link AbstractInsnNode instructions} in this expression, or {@code null} if there aren't any.
     */
    public @Nullable LinkedList<AbstractInsnNode> instructions;

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

    private LinkedList<AbstractInsnNode> insns() {
        if (instructions == null) instructions = new LinkedList<>();
        return instructions;
    }

    @Override
    public void visitInsn(byte opcode) {
        super.visitInsn(opcode);
        insns().addLast(new InsnNode(opcode));
    }

    @Override
    public void visitPrefixInsn(int opcode) {
        super.visitPrefixInsn(opcode);
        insns().addLast(new PrefixInsnNode(opcode));
    }

    @Override
    public void visitConstInsn(Object v) {
        super.visitConstInsn(v);
        insns().addLast(new ConstInsnNode(v));
    }

    @Override
    public void visitNullInsn(byte type) {
        super.visitNullInsn(type);
        insns().addLast(new NullInsnNode(type));
    }

    @Override
    public void visitFuncRefInsn(int function) {
        super.visitFuncRefInsn(function);
        insns().addLast(new FuncRefInsnNode(function));
    }

    @Override
    public void visitSelectInsn(byte[] type) {
        super.visitSelectInsn(type);
        insns().addLast(new SelectInsnNode(type));
    }

    @Override
    public void visitVariableInsn(byte opcode, int variable) {
        super.visitVariableInsn(opcode, variable);
        insns().addLast(new VariableInsnNode(opcode, variable));
    }

    @Override
    public void visitTableInsn(byte opcode, int table) {
        super.visitTableInsn(opcode, table);
        insns().addLast(new TableInsnNode(opcode, table));
    }

    @Override
    public void visitPrefixTableInsn(int opcode, int table) {
        super.visitPrefixTableInsn(opcode, table);
        insns().addLast(new PrefixTableInsnNode(opcode, table));
    }

    @Override
    public void visitPrefixBinaryTableInsn(int opcode, int firstIndex, int secondIndex) {
        super.visitPrefixBinaryTableInsn(opcode, firstIndex, secondIndex);
        insns().addLast(new PrefixBinaryTableInsnNode(opcode, firstIndex, secondIndex));
    }

    @Override
    public void visitMemInsn(byte opcode, int align, int offset) {
        super.visitMemInsn(opcode, align, offset);
        insns().addLast(new MemInsnNode(opcode, align, offset));
    }

    @Override
    public void visitIndexedMemInsn(int opcode, int index) {
        super.visitIndexedMemInsn(opcode, index);
        insns().addLast(new IndexedMemInsnNode(opcode, index));
    }

    @Override
    public void visitBlockInsn(byte opcode, BlockType blockType) {
        super.visitBlockInsn(opcode, blockType);
        insns().addLast(new BlockInsnNode(opcode, blockType));
    }

    @Override
    public void visitElseInsn() {
        super.visitElseInsn();
        insns().addLast(new ElseInsnNode());
    }

    @Override
    public void visitEndInsn() {
        super.visitEndInsn();
        insns().addLast(new EndInsnNode());
    }

    @Override
    public void visitBreakInsn(byte opcode, int label) {
        super.visitBreakInsn(opcode, label);
        insns().addLast(new BreakInsnNode(opcode, label));
    }

    @Override
    public void visitTableBreakInsn(int[] labels, int defaultLabel) {
        super.visitTableBreakInsn(labels, defaultLabel);
        insns().addLast(new TableBreakInsnNode(labels, defaultLabel));
    }

    @Override
    public void visitCallInsn(int function) {
        super.visitCallInsn(function);
        insns().addLast(new CallInsnNode(function));
    }

    @Override
    public void visitCallIndirectInsn(int table, int type) {
        super.visitCallIndirectInsn(table, type);
        insns().addLast(new CallIndirectInsnNode(table, type));
    }

    @NotNull
    @Override
    public Iterator<AbstractInsnNode> iterator() {
        return instructions == null ? Collections.emptyIterator() : instructions.iterator();
    }
}
