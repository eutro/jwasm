package io.github.eutro.jwasm;

import org.jetbrains.annotations.Nullable;

/**
 * A visitor that visits an
 * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-expr">expression</a>.
 * <p>
 * Methods are expected to be called in the order:
 * <p>
 * ( {@code visitInsn} | {@code visitPrefixInsn} | {@code visitConstInsn} | {@code visitNullInsn} |
 * {@code visitFuncInsn} | {@code visitSelectInsn} | {@code visitVariableInsn} | {@code visitTableInsn} |
 * {@code visitPrefixTableInsn} | {@code visitPrefixBinaryTableInsn} | {@code visitMemInsn} |
 * {@code visitIndexedMemInsn} | {@code visitBlockInsn} | {@code visitElseInsn} | {@code visitEndInsn} |
 * {@code visitBreakInsn} | {@code visitTableBreakInsn} | {@code visitCallInsn} | {@code visitCallIndirectInsn} )*
 * {@code visitEnd}
 */
public class ExprVisitor extends BaseVisitor<ExprVisitor> {
    /**
     * Construct a visitor with no delegate.
     */
    public ExprVisitor() {
    }

    /**
     * Construct a visitor with a delegate.
     *
     * @param dl The visitor to delegate all method calls to, or {@code null}.
     */
    public ExprVisitor(@Nullable ExprVisitor dl) {
        super(dl);
    }

    /**
     * Visit an <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-instr">instr</a>
     * with no immediate arguments.
     *
     * @param opcode The opcode of the instruction.
     */
    public void visitInsn(byte opcode) {
        if (dl != null) dl.visitInsn(opcode);
    }

    /**
     * Visit a prefix <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-instr">instr</a>,
     * one encoded with {@link Opcodes#INSN_PREFIX} followed by a variable-length {@code u32} representing the actual
     * opcode.
     * <p>
     * It has no immediate arguments.
     *
     * @param opcode The opcode of the instruction.
     */
    public void visitPrefixInsn(int opcode) {
        if (dl != null) dl.visitPrefixInsn(opcode);
    }

    /**
     * Visit a
     * <a href="https://webassembly.github.io/spec/core/syntax/instructions.html#syntax-instr-numeric">const</a>
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-instr">instr</a>.
     *
     * @param v The constant value of the instruction, which may be an
     *          {@link Integer}, {@link Long}, {@link Float} or {@link Double},
     *          representing {@code i32}, {@code i64}, {@code f32} and {@code f64}
     *          {@code const} instructions respectively.
     */
    public void visitConstInsn(Object v) {
        if (dl != null) dl.visitConstInsn(v);
    }

    /**
     * Visit a
     * <a href="https://webassembly.github.io/spec/core/syntax/instructions.html#syntax-instr-ref">ref.null</a>
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-instr">instr</a>.
     *
     * @param type The <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-reftype">reftype</a>
     *             of the null value.
     */
    public void visitNullInsn(byte type) {
        if (dl != null) dl.visitNullInsn(type);
    }

    /**
     * Visit a
     * <a href="https://webassembly.github.io/spec/core/syntax/instructions.html#syntax-instr-ref">ref.func</a>
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-instr">instr</a>.
     *
     * @param function The
     *                 <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-funcidx">index</a>
     *                 of the function to reference.
     */
    public void visitFuncInsn(int function) {
        if (dl != null) dl.visitFuncInsn(function);
    }

    /**
     * Visit a
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#parametric-instructions">select</a>
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-instr">instr</a>
     * with explicit types.
     *
     * @param type The
     *             <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-valtype">valtype</a>s
     *             of the instruction.
     */
    public void visitSelectInsn(byte[] type) {
        if (dl != null) dl.visitSelectInsn(type);
    }

    /**
     * Visit a
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#variable-instructions">variable</a>
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-instr">instr</a>.
     *
     * @param opcode The opcode of the instruction.
     * @param index  The
     *               <a href="https://webassembly.github.io/spec/core/syntax/modules.html#syntax-index">index</a>
     *               of the variable.
     */
    public void visitVariableInsn(byte opcode, int index) {
        if (dl != null) dl.visitVariableInsn(opcode, index);
    }

    /**
     * Visit a prefixless
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#table-instructions">table</a>
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-instr">instr</a>.
     *
     * @param opcode The opcode of the instruction.
     * @param table  The
     *               <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-tableidx">index</a>
     *               of the table.
     */
    public void visitTableInsn(byte opcode, int table) {
        if (dl != null) dl.visitTableInsn(opcode, table);
    }

    /**
     * Visit a prefixed
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#table-instructions">table</a>
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-instr">instr</a>.
     *
     * @param opcode The opcode of the instruction.
     * @param table  The
     *               <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-tableidx">index</a>
     *               of the table.
     */
    public void visitPrefixTableInsn(int opcode, int table) {
        if (dl != null) dl.visitPrefixTableInsn(opcode, table);
    }

    /**
     * Visit a prefixed
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#table-instructions">table</a>
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-instr">instr</a>
     * with two immediate arguments.
     *
     * @param opcode      The opcode of the instruction.
     * @param firstIndex  The
     *                    <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-tableidx">index</a>
     *                    of the target table.
     * @param secondIndex The second
     *                    <a href="https://webassembly.github.io/spec/core/binary/modules.html#indices">index</a>
     *                    argument.
     */
    public void visitPrefixBinaryTableInsn(int opcode, int firstIndex, int secondIndex) {
        if (dl != null) dl.visitPrefixBinaryTableInsn(opcode, firstIndex, secondIndex);
    }

    /**
     * Visit a
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#memory-instructions">memory</a>
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-instr">instr</a>
     * with a single memory argument.
     *
     * @param opcode The opcode of the instruction.
     * @param align  The {@code align} of the argument.
     * @param offset The {@code offset} of the argument.
     */
    public void visitMemInsn(byte opcode, int align, int offset) {
        if (dl != null) dl.visitMemInsn(opcode, align, offset);
    }

    /**
     * Visit a prefixed
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#memory-instructions">memory</a>
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-instr">instr</a>
     * with an <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-dataidx">index</a> argument.
     *
     * @param opcode The opcode of the instruction.
     * @param index  The <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-dataidx">index</a>
     *               argument.
     */
    public void visitIndexedMemInsn(int opcode, int index) {
        if (dl != null) dl.visitIndexedMemInsn(opcode, index);
    }

    /**
     * Visit a
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#control-instructions">block</a>
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-instr">instr</a>.
     *
     * @param opcode    The opcode of the instruction.
     * @param blockType The
     *                  <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-blocktype">type</a>
     *                  of the block.
     */
    public void visitBlockInsn(byte opcode, int blockType) {
        if (dl != null) dl.visitBlockInsn(opcode, blockType);
    }

    /**
     * Visit the optional {@link Opcodes#ELSE else} in an {@link Opcodes#IF if} block.
     */
    public void visitElseInsn() {
        if (dl != null) dl.visitElseInsn();
    }

    /**
     * Visit the {@link Opcodes#END end} of a
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#control-instructions">block</a>
     * or
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#expressions">expression</a>.
     */
    public void visitEndInsn() {
        if (dl != null) dl.visitEndInsn();
    }

    /**
     * Visit a
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#control-instructions">break</a>
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-instr">instr</a>.
     *
     * @param opcode The opcode of the instruction.
     * @param label  The
     *               <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-labelidx">index</a>
     *               of the label.
     */
    public void visitBreakInsn(byte opcode, int label) {
        if (dl != null) dl.visitBreakInsn(opcode, label);
    }

    /**
     * Visit a
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#control-instructions">br_table</a>
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-instr">instr</a>.
     *
     * @param labels       The table of label
     *                     <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-labelidx">indeces</a>.
     * @param defaultLabel The
     *                     <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-labelidx">index</a>
     *                     of the default label.
     */
    public void visitTableBreakInsn(int[] labels, int defaultLabel) {
        if (dl != null) dl.visitTableBreakInsn(labels, defaultLabel);
    }

    /**
     * Visit a
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#control-instructions">call</a>
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-instr">instr</a>.
     *
     * @param function The
     *                 <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-funcidx">index</a>
     *                 of the function to call.
     */
    public void visitCallInsn(int function) {
        if (dl != null) dl.visitCallInsn(function);
    }

    /**
     * Visit a
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#control-instructions">call_indirect</a>
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-instr">instr</a>.
     *
     * @param table The table
     *              <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-tableidx">index</a>
     *              to look up the reference in.
     * @param type  The
     *              <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-typeidx">index</a>
     *              of the function's type.
     */
    public void visitCallIndirectInsn(int table, int type) {
        if (dl != null) dl.visitCallIndirectInsn(table, type);
    }
}
