package io.github.eutro.jwasm.tree;

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
     * Make the given {@link ExprVisitor} visit all the instructions of this node.
     *
     * @param ev The visitor to visit.
     */
    public void accept(ExprVisitor ev) {
        if (instructions != null) {
            int depth = 0;
            for (AbstractInsnNode next : instructions) {
                if (next instanceof BlockInsnNode) depth++;
                else if (next instanceof EndInsnNode && --depth < 0) break;
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
    public void visitFuncRefInsn(int function) {
        insns().addLast(new FuncRefInsnNode(function));
    }

    @Override
    public void visitSelectInsn(byte[] type) {
        insns().addLast(new SelectInsnNode(type));
    }

    @Override
    public void visitVariableInsn(byte opcode, int variable) {
        insns().addLast(new VariableInsnNode(opcode, variable));
    }

    @Override
    public void visitTableInsn(byte opcode, int table) {
        insns().addLast(new TableInsnNode(opcode, table));
    }

    @Override
    public void visitPrefixTableInsn(int opcode, int table) {
        insns().addLast(new PrefixTableInsnNode(opcode, table));
    }

    @Override
    public void visitPrefixBinaryTableInsn(int opcode, int firstIndex, int secondIndex) {
        insns().addLast(new PrefixBinaryTableInsnNode(opcode, firstIndex, secondIndex));
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
    public void visitBreakInsn(byte opcode, int label) {
        insns().addLast(new BreakInsnNode(opcode, label));
    }

    @Override
    public void visitTableBreakInsn(int[] labels, int defaultLabel) {
        insns().addLast(new TableBreakInsnNode(labels, defaultLabel));
    }

    @Override
    public void visitCallInsn(int function) {
        insns().addLast(new CallInsnNode(function));
    }

    @Override
    public void visitCallIndirectInsn(int table, int type) {
        insns().addLast(new CallIndirectInsnNode(table, type));
    }

    @NotNull
    @Override
    public Iterator<AbstractInsnNode> iterator() {
        return instructions == null ? Collections.emptyIterator() : instructions.iterator();
    }
}
