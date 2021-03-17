package io.github.eutro.jwasm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

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
    public void visitFuncImport(@NotNull String module, @NotNull String name, int index) {
        ++count;
        out.putName(module);
        out.putName(name);
        out.put(Opcodes.IMPORTS_FUNC);
        out.putVarUInt(index);
    }

    @Override
    public void visitTableImport(@NotNull String module, @NotNull String name, int min, @Nullable Integer max, byte type) {
        ++count;
        out.putName(module);
        out.putName(name);
        out.put(Opcodes.IMPORTS_TABLE);
        out.put(type);
        out.putLimit(min, max);
    }

    @Override
    public void visitMemImport(@NotNull String module, @NotNull String name, int min, @Nullable Integer max) {
        ++count;
        out.putName(module);
        out.putName(name);
        out.put(Opcodes.IMPORTS_MEM);
        out.putLimit(min, max);
    }

    @Override
    public void visitGlobalImport(@NotNull String module, @NotNull String name, byte mut, byte type) {
        ++count;
        out.putName(module);
        out.putName(name);
        out.put(Opcodes.IMPORTS_GLOBAL);
        out.put(type);
        out.put(mut);
    }

    @Override
    public void visitEnd() {
        if (onEnd != null) onEnd.accept(this);
    }
}