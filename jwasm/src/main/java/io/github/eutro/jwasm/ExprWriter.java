package io.github.eutro.jwasm;

import java.util.function.Consumer;


/**
 * A {@link DataVisitor} that generates the corresponding WebAssembly bytecode as it is visited.
 * This can be retrieved using {@link #toByteArray()} after {@link #visitEnd()}.
 */
public class ExprWriter extends ExprVisitor implements ByteArrayConvertible {
    /**
     * The {@link ByteOutputStream} that this visitor will write raw bytes to.
     */
    private final ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();

    /**
     * A callback that is called from {@link #visitEnd()}, or {@code null}.
     */
    public Consumer<byte[]> onEnd;

    /**
     * Constructs a writer with no {@link #onEnd end callback}.
     */
    public ExprWriter() {
    }

    /**
     * Constructs a writer with an optional {@link #onEnd end callback}.
     *
     * @param onEnd A callback that is called from {@link #visitEnd()}, or {@code null}.
     */
    public ExprWriter(Consumer<byte[]> onEnd) {
        this.onEnd = onEnd;
    }

    @Override
    public byte[] toByteArray() {
        return out.toByteArray();
    }

    @Override
    public void visitInsn(byte opcode) {
        out.put(opcode);
        switch (opcode) {
            case Opcodes.MEMORY_GROW:
            case Opcodes.MEMORY_SIZE:
                out.put((byte) 0x00);
                break;
        }
    }

    @Override
    public void visitPrefixInsn(int opcode) {
        out.put(Opcodes.INSN_PREFIX);
        out.putVarUInt(opcode);
        switch (opcode) {
            case Opcodes.MEMORY_COPY:
                out.put((byte) 0x00);
                // fall through
            case Opcodes.MEMORY_FILL:
                out.put((byte) 0x00);
                break;
        }
    }

    @Override
    public void visitConstInsn(Object v) {
        if (v instanceof Integer) {
            out.put(Opcodes.I32_CONST);
            out.putVarSInt((Integer) v);
        } else if (v instanceof Long) {
            out.put(Opcodes.I64_CONST);
            out.putVarSInt((Long) v);
        } else if (v instanceof Float) {
            out.put(Opcodes.F32_CONST);
            out.putFloat32((Float) v);
        } else if (v instanceof Double) {
            out.put(Opcodes.F64_CONST);
            out.putFloat64((Double) v);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void visitNullInsn(byte type) {
        out.put(Opcodes.REF_NULL);
        out.put(type);
    }

    @Override
    public void visitFuncRefInsn(int function) {
        out.put(Opcodes.REF_FUNC);
        out.putVarUInt(function);
    }

    @Override
    public void visitSelectInsn(byte[] type) {
        out.put(Opcodes.SELECTT);
        out.putByteArray(type);
    }

    @Override
    public void visitVariableInsn(byte opcode, int variable) {
        out.put(opcode);
        out.putVarUInt(variable);
    }

    @Override
    public void visitTableInsn(byte opcode, int table) {
        out.put(opcode);
        out.putVarUInt(table);
    }

    @Override
    public void visitPrefixTableInsn(int opcode, int table) {
        out.put(Opcodes.INSN_PREFIX);
        out.putVarUInt(opcode);
        out.putVarUInt(table);
    }

    @Override
    public void visitPrefixBinaryTableInsn(int opcode, int firstIndex, int secondIndex) {
        out.put(Opcodes.INSN_PREFIX);
        out.putVarUInt(opcode);
        switch (opcode) {
            case Opcodes.TABLE_COPY:
                out.putVarUInt(secondIndex);
                out.putVarUInt(firstIndex);
                break;
            case Opcodes.TABLE_INIT:
                out.putVarUInt(firstIndex);
                out.putVarUInt(secondIndex);
                break;
        }
    }

    @Override
    public void visitMemInsn(byte opcode, int align, int offset) {
        out.put(opcode);
        out.putVarUInt(align);
        out.putVarUInt(offset);
    }

    @Override
    public void visitIndexedMemInsn(int opcode, int index) {
        out.put(Opcodes.INSN_PREFIX);
        out.putVarUInt(opcode);
        out.putVarUInt(index);
        if (opcode == Opcodes.MEMORY_INIT) out.put((byte) 0x00);
    }

    @Override
    public void visitBlockInsn(byte opcode, BlockType blockType) {
        out.put(opcode);
        if (blockType.isValtype()) {
            out.put((byte) blockType.get());
        } else {
            out.putVarSInt(Integer.toUnsignedLong(blockType.get()));
        }
    }

    @Override
    public void visitElseInsn() {
        out.put(Opcodes.ELSE);
    }

    @Override
    public void visitEndInsn() {
        out.put(Opcodes.END);
    }

    @Override
    public void visitBreakInsn(byte opcode, int label) {
        out.put(opcode);
        out.putVarUInt(label);
    }

    @Override
    public void visitTableBreakInsn(int[] labels, int defaultLabel) {
        out.put(Opcodes.BR_TABLE);
        out.putVarUInt(labels.length);
        for (int i : labels) {
            out.putVarUInt(i);
        }
        out.putVarUInt(defaultLabel);
    }

    @Override
    public void visitCallInsn(int function) {
        out.put(Opcodes.CALL);
        out.putVarUInt(function);
    }

    @Override
    public void visitCallIndirectInsn(int table, int type) {
        out.put(Opcodes.CALL_INDIRECT);
        out.putVarUInt(type);
        out.putVarUInt(table);
    }

    @Override
    public void visitVectorInsn(int opcode) {
        out.put(Opcodes.VECTOR_PREFIX);
        out.putVarUInt(opcode);
    }

    @Override
    public void visitVectorMemInsn(int opcode, int align, int offset) {
        out.put(Opcodes.VECTOR_PREFIX);
        out.putVarUInt(opcode);
        out.putVarUInt(align);
        out.putVarUInt(offset);
    }

    @Override
    public void visitVectorMemLaneInsn(int opcode, int align, int offset, byte lane) {
        out.put(Opcodes.VECTOR_PREFIX);
        out.putVarUInt(opcode);
        out.putVarUInt(align);
        out.putVarUInt(offset);
        out.put(lane);
    }

    @Override
    public void visitVectorConstOrShuffleInsn(int opcode, byte[] bytes) {
        out.put(Opcodes.VECTOR_PREFIX);
        out.putVarUInt(opcode);
        out.put(bytes);
    }

    @Override
    public void visitVectorLaneInsn(int opcode, byte lane) {
        out.put(Opcodes.VECTOR_PREFIX);
        out.putVarUInt(opcode);
        out.put(lane);
    }

    @Override
    public void visitEnd() {
        if (onEnd != null) onEnd.accept(toByteArray());
    }
}
