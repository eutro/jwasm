package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

/**
 * A node that represents a block instruction.
 *
 * @see ExprVisitor#visitBlockInsn(byte, int)
 */
public class BlockInsnNode extends AbstractInsnNode {
    /**
     * The
     * <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-blocktype">type</a>
     * of the block.
     */
    public int blockType;

    /**
     * Construct a {@link BlockInsnNode} with the given opcode and block type.
     *
     * @param opcode    The opcode of this instruction.
     * @param blockType The
     *                  <a href="https://webassembly.github.io/spec/core/binary/instructions.html#binary-blocktype">type</a>
     *                  of the block.
     */
    public BlockInsnNode(byte opcode, int blockType) {
        super(opcode);
        this.blockType = blockType;
    }

    /**
     * {@inheritDoc}
     *
     * @param ev The visitor to visit.
     * @see ExprVisitor#visitBlockInsn(byte, int)
     */
    @Override
    void accept(ExprVisitor ev) {
        ev.visitBlockInsn(opcode, blockType);
    }
}
