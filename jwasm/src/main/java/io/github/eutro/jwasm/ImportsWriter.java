package io.github.eutro.jwasm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A {@link ImportsVisitor} that generates the corresponding WebAssembly bytecode as it is visited.
 * This can be retrieved using {@link #toByteArray()} after {@link #visitEnd()}.
 */
public class ImportsWriter extends ImportsVisitor implements VectorWriter {
    /**
     * The {@link ByteOutputStream} that this visitor will write raw bytes to.
     */
    private final ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();

    /**
     * Records number of elements in the vector.
     */
    private int count;

    /**
     * A callback that is called from {@link #visitEnd()}, or {@code null}.
     */
    public @Nullable Consumer<ImportsWriter> onEnd;

    /**
     * Constructs a writer with no {@link #onEnd end callback}.
     */
    public ImportsWriter() {
    }

    /**
     * Constructs a writer with an optional {@link #onEnd end callback}.
     *
     * @param onEnd A callback that is called from {@link #visitEnd()}, or {@code null}.
     */
    public ImportsWriter(@Nullable Consumer<ImportsWriter> onEnd) {
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
    public void visitFuncImport(@NotNull String module, @NotNull String name, int type) {
        ++count;
        out.putName(module);
        out.putName(name);
        out.put(Opcodes.IMPORTS_FUNC);
        out.putVarUInt(type);
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