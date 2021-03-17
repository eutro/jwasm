package io.github.eutro.jwasm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * A {@link ModuleVisitor} that generates the corresponding WebAssembly bytecode as it is visited.
 * This can be retrieved using {@link #toByteArray()} after {@link #visitEnd()}.
 */

public class ModuleWriter extends ModuleVisitor implements ByteArrayConvertible {
    /**
     * The {@link ByteOutputStream} that this visitor will write raw bytes to.
     */
    private final ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();

    @Override
    public byte[] toByteArray() {
        return out.toByteArray();
    }

    @Override
    public void visitHeader(int version) {
        out.putUInt32(Opcodes.MAGIC);
        out.putUInt32(version);
    }

    @Override
    public void visitCustom(@NotNull String name, byte @NotNull [] payload) {
        out.put(Opcodes.SECTION_CUSTOM);
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        out.putVarUInt(ByteOutputStream.DUMMY.putVarUInt(nameBytes.length) + nameBytes.length + payload.length);
        out.putVarUInt(nameBytes.length);
        out.put(nameBytes);
        out.put(payload);
    }

    private <T extends VectorWriter> Consumer<T> putVectorSection(byte section) {
        return v -> {
            if (v.count() != 0) {
                out.put(section);
                byte[] bytes = v.toByteArray();
                out.putVarUInt(bytes.length);
                out.put(bytes);
            }
        };
    }

    @Override
    public @Nullable TypesVisitor visitTypes() {
        return new TypesWriter(putVectorSection(Opcodes.SECTION_TYPE));
    }

    @Override
    public @Nullable ImportsVisitor visitImports() {
        return new ImportsWriter(putVectorSection(Opcodes.SECTION_IMPORT));
    }

    @Override
    public @Nullable FunctionsVisitor visitFuncs() {
        return new FunctionsWriter(putVectorSection(Opcodes.SECTION_FUNCTION));
    }

    @Override
    public @Nullable TablesVisitor visitTables() {
        return new TablesWriter(putVectorSection(Opcodes.SECTION_TABLE));
    }

    @Override
    public @Nullable MemoriesVisitor visitMems() {
        return new MemoriesWriter(putVectorSection(Opcodes.SECTION_MEMORY));
    }

    @Override
    public @Nullable GlobalsVisitor visitGlobals() {
        return new GlobalsWriter(putVectorSection(Opcodes.SECTION_GLOBAL));
    }

    @Override
    public @Nullable ExportsVisitor visitExports() {
        return new ExportsWriter(putVectorSection(Opcodes.SECTION_EXPORT));
    }

    @Override
    public void visitStart(int func) {
        out.put(Opcodes.SECTION_START);
        out.putVarUInt(func);
    }

    @Override
    public @Nullable ElementSegmentsVisitor visitElems() {
        return new ElementSegmentsWriter(putVectorSection(Opcodes.SECTION_ELEMENT));
    }

    @Override
    public void visitDataCount(int count) {
        out.put(Opcodes.SECTION_DATA_COUNT);
        out.putVarUInt(count);
    }

    @Override
    public @Nullable CodesVisitor visitCode() {
        return new CodesWriter(putVectorSection(Opcodes.SECTION_CODE));
    }

    @Override
    public @Nullable DataSegmentsVisitor visitDatas() {
        return new DataSegmentsWriter(putVectorSection(Opcodes.SECTION_DATA));
    }
}
