package io.github.eutro.jwasm;

import java.util.function.Consumer;

import static io.github.eutro.jwasm.Opcodes.*;

public class ImportsWriter extends ImportsVisitor implements VectorWriter {
    private final ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();
    private int count;
    public Consumer<ImportsWriter> onEnd;

    public ImportsWriter() {
    }

    public ImportsWriter(Consumer<ImportsWriter> onEnd) {
        this.onEnd = onEnd;
    }

    @Override
    public int count() {
        return count;
    }

    @Override
    public byte[] raw() {
        return out.toByteArray();
    }

    @Override
    public void visitFuncImport(String module, String name, int index) {
        ++count;
        out.putName(module);
        out.putName(name);
        out.put(IMPORTS_FUNC);
        out.putVarUInt(index);
    }

    @Override
    public void visitTableImport(String module, String name, int min, Integer max, byte type) {
        ++count;
        out.putName(module);
        out.putName(name);
        out.put(IMPORTS_TABLE);
        out.put(type);
        out.putLimit(min, max);
    }

    @Override
    public void visitMemImport(String module, String name, int min, Integer max) {
        ++count;
        out.putName(module);
        out.putName(name);
        out.put(IMPORTS_MEM);
        out.putLimit(min, max);
    }

    @Override
    public void visitGlobalImport(String module, String name, byte mut, byte type) {
        ++count;
        out.putName(module);
        out.putName(name);
        out.put(IMPORTS_GLOBAL);
        out.put(type);
        out.put(mut);
    }

    @Override
    public void visitEnd() {
        if (onEnd != null) onEnd.accept(this);
    }
}