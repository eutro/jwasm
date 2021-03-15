package io.github.eutro.jwasm;

import java.util.function.Consumer;

import static io.github.eutro.jwasm.Opcodes.*;

public class ElementWriter extends ElementVisitor implements ByteArrayConvertible {
    public Consumer<byte[]> onEnd;

    private byte elemType = 0x00;
    private int table;
    private byte[] offsetBytes;
    private byte type;
    private int exprCount;
    private ByteOutputStream.BaosByteOutputStream exprs;
    private int[] elemIndeces;

    public ElementWriter() {
    }

    public ElementWriter(Consumer<byte[]> onEnd) {
        this.onEnd = onEnd;
    }

    @Override
    public byte[] toByteArray() {
        ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();
        out.put(elemType);
        if ((elemType & ELEM_PASSIVE_OR_DECLARATIVE) == 0) {
            if ((elemType & ELEM_TABLE_INDEX) != 0) {
                out.putVarUInt(table);
            }
            out.put(offsetBytes);
        }
        if ((elemType & ELEM_EXPRESSIONS) != 0) {
            if ((elemType & 0b011) != 0) out.put(type);
            out.putVarUInt(exprCount);
            out.put(exprs.toByteArray());
        } else {
            if ((elemType & 0b011) != 0) out.put(ELEMKIND);
            out.putVarUInt(elemIndeces.length);
            for (int index : elemIndeces) {
                out.putVarUInt(index);
            }
        }
        return out.toByteArray();
    }

    @Override
    public void visitNonActiveMode(boolean passive) {
        elemType |= ELEM_PASSIVE_OR_DECLARATIVE;
        if (passive) elemType |= ELEM_TABLE_INDEX;
    }

    @Override
    public ExprVisitor visitActiveMode(int table) {
        if (table != 0) {
            elemType |= ELEM_TABLE_INDEX;
            this.table = table;
        }
        return new ExprWriter(b -> this.offsetBytes = b);
    }

    @Override
    public void visitType(byte type) {
        this.type = type;
    }

    @Override
    public ExprVisitor visitInit() {
        elemType |= ELEM_EXPRESSIONS;
        ++exprCount;
        return new ExprWriter((exprs = new ByteOutputStream.BaosByteOutputStream())::put);
    }

    @Override
    public void visitElemIneces(int[] indeces) {
        this.elemIndeces = indeces;
    }

    @Override
    public void visitEnd() {
        if (onEnd != null) onEnd.accept(toByteArray());
    }
}
