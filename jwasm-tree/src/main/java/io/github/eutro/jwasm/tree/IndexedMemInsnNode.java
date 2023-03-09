package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

/**
 * A node that represents a prefixed
 * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#memory-instructions">memory</a>
 * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-instr">instr</a>
 * with an <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-dataidx">index</a> argument.
 *
 * @see ExprVisitor#visitIndexedMemInsn(int, int)
 */
public class IndexedMemInsnNode extends PrefixInsnNode {
    /**
     * The <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-dataidx">index</a>
     * argument.
     */
    public int index;

    /**
     * Construct an {@link IndexedMemInsnNode} with the given integer opcode and index.
     *
     * @param intOpcode The opcode of the instruction.
     * @param index     The <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-dataidx">index</a>
     *                  argument.
     */
    public IndexedMemInsnNode(int intOpcode, int index) {
        super(intOpcode);
        this.index = index;
    }

    /**
     * {@inheritDoc}
     *
     * @param ev The visitor to visit.
     * @see ExprVisitor#visitIndexedMemInsn(int, int)
     */
    @Override
    public void accept(ExprVisitor ev) {
        ev.visitPc(pc);
        ev.visitIndexedMemInsn(intOpcode, index);
    }
}
