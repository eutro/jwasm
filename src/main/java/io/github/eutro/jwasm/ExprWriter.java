package io.github.eutro.jwasm;

import java.util.function.Consumer;

import static io.github.eutro.jwasm.Opcodes.*;

public class ExprWriter extends ExprVisitor implements ByteArrayConvertible {
    private final ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();
    public Consumer<byte[]> onEnd;

    public ExprWriter() {
    }

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
            case MEMORY_GROW:
            case MEMORY_SIZE:
                out.put((byte) 0x00);
                break;
        }
    }

    @Override
    public void visitPrefixInsn(int opcode) {
        out.put(INSN_PREFIX);
        out.putVarUInt(opcode);
        switch (opcode) {
            case MEMORY_COPY:
                out.put((byte) 0x00);
                // fall through
            case MEMORY_FILL:
                out.put((byte) 0x00);
                break;
        }
    }

    @Override
    public void visitConstInsn(Object v) {
        if (v instanceof Integer) {
            out.put(I32_CONST);
            out.putVarSInt((Integer) v);
        } else if (v instanceof Long) {
            out.put(I64_CONST);
            out.putVarSInt((Long) v);
        } else if (v instanceof Float) {
            out.put(F32_CONST);
            out.putFloat32((Float) v);
        } else if (v instanceof Double) {
            out.put(F64_CONST);
            out.putFloat64((Double) v);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void visitNullInsn(byte type) {
        out.put(REF_NULL);
        out.put(type);
    }

    @Override
    public void visitFuncInsn(int index) {
        out.put(REF_FUNC);
        out.putVarUInt(index);
    }

    @Override
    public void visitSelectInsn(byte[] type) {
        out.put(SELECTT);
        out.putByteArray(type);
    }

    @Override
    public void visitVariableInsn(byte opcode, int index) {
        out.put(opcode);
        out.putVarUInt(index);
    }

    @Override
    public void visitTableInsn(byte opcode, int index) {
        out.put(opcode);
        out.putVarUInt(index);
    }

    @Override
    public void visitPrefixTableInsn(int opcode, int index) {
        out.put(INSN_PREFIX);
        out.putVarUInt(opcode);
        out.putVarUInt(index);
    }

    @Override
    public void visitPrefixBinaryTableInsn(int opcode, int sourceIndex, int targetIndex) {
        out.put(INSN_PREFIX);
        out.putVarUInt(opcode);
        switch (opcode) {
            case TABLE_COPY:
                out.putVarUInt(targetIndex);
                out.putVarUInt(sourceIndex);
                break;
            case TABLE_INIT:
                out.putVarUInt(sourceIndex);
                out.putVarUInt(targetIndex);
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
        out.put(INSN_PREFIX);
        out.putVarUInt(opcode);
        out.putVarUInt(index);
        if (opcode == MEMORY_INIT) out.put((byte) 0x00);
    }

    @Override
    public void visitBlockInsn(byte opcode, int blockType) {
        out.put(opcode);
        switch (blockType) {
            case EMPTY_TYPE:
            case I32:
            case I64:
            case F32:
            case F64:
            case FUNCREF:
            case EXTERNREF:
                out.put((byte) blockType);
                break;
            default:
                out.putVarSInt(Integer.toUnsignedLong(blockType));
        }
    }

    @Override
    public void visitElseInsn() {
        out.put(ELSE);
    }

    @Override
    public void visitEndInsn() {
        out.put(END);
    }

    @Override
    public void visitBreakInsn(byte opcode, int index) {
        out.put(opcode);
        out.putVarUInt(index);
    }

    @Override
    public void visitTableBreakInsn(int[] table, int index) {
        out.put(BR_TABLE);
        out.putVarUInt(table.length);
        for (int i : table) {
            out.putVarUInt(i);
        }
        out.putVarUInt(index);
    }

    @Override
    public void visitCallInsn(int index) {
        out.put(CALL);
        out.putVarUInt(index);
    }

    @Override
    public void visitCallIndirectInsn(int table, int index) {
        out.put(CALL_INDIRECT);
        out.putVarUInt(index);
        out.putVarUInt(table);
    }

    @Override
    public void visitEnd() {
        if (onEnd != null) onEnd.accept(toByteArray());
    }
}
