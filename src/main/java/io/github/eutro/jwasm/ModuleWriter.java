package io.github.eutro.jwasm;

import io.github.eutro.jwasm.ByteOutputStream.BaosByteOutputStream;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static io.github.eutro.jwasm.Opcodes.*;

public class ModuleWriter extends ModuleVisitor implements ByteArrayConvertible {
    private final BaosByteOutputStream out = new BaosByteOutputStream();

    @Override
    public byte[] toByteArray() {
        return out.toByteArray();
    }

    @Override
    public void visitHeader(int version) {
        out.putUInt32(MAGIC);
        out.putUInt32(version);
    }

    @Override
    public void visitCustom(String name, byte[] payload) {
        out.put(SECTION_CUSTOM);
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
    public TypesVisitor visitTypes() {
        return new TypesWriter(putVectorSection(SECTION_TYPE));
    }

    @Override
    public ImportsVisitor visitImports() {
        return new ImportsWriter(putVectorSection(SECTION_IMPORT));
    }

    @Override
    public FunctionsVisitor visitFuncs() {
        return new FunctionsWriter(putVectorSection(SECTION_FUNCTION));
    }

    @Override
    public TablesVisitor visitTables() {
        return new TablesWriter(putVectorSection(SECTION_TABLE));
    }

    @Override
    public MemoriesVisitor visitMems() {
        return new MemoriesWriter(putVectorSection(SECTION_MEMORY));
    }

    @Override
    public GlobalsVisitor visitGlobals() {
        return new GlobalsWriter(putVectorSection(SECTION_GLOBAL));
    }

    @Override
    public ExportsVisitor visitExports() {
        return new ExportsWriter(putVectorSection(SECTION_EXPORT));
    }

    @Override
    public void visitStart(int func) {
        out.put(SECTION_START);
        out.putVarUInt(func);
    }

    @Override
    public ElementSegmentsVisitor visitElems() {
        return new ElementSegmentsWriter(putVectorSection(SECTION_ELEMENT));
    }

    @Override
    public void visitDataCount(int count) {
        out.put(SECTION_DATA_COUNT);
        out.putVarUInt(count);
    }

    @Override
    public CodesVisitor visitCode() {
        return new CodesWriter(putVectorSection(SECTION_CODE));
    }

    @Override
    public DataSegmentsVisitor visitDatas() {
        return new DataSegmentsWriter(putVectorSection(SECTION_DATA));
    }
}
