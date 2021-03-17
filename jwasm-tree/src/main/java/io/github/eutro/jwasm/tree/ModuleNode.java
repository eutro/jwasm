package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static io.github.eutro.jwasm.Opcodes.*;

public class ModuleNode extends ModuleVisitor {
    public int version = Opcodes.VERSION;
    @SuppressWarnings("unchecked")
    public final List<CustomNode>[] customs = (List<CustomNode>[]) new List<?>[SECTION_DATA_COUNT + 1];
    public TypesNode types;
    public ImportsNode imports;
    public FunctionsNode funcs;
    public TablesNode tables;
    public MemoriesNode mems;
    public GlobalsNode globals;
    public ExportsNode exports;
    public StartNode start;
    public ElementSegmentsNode elems;
    public Integer dataCount;
    public CodesNode codes;
    public DataSegmentsNode datas;

    private byte section = SECTION_CUSTOM;

    public void accept(ModuleVisitor mv) {
        mv.visitHeader(version);

        acceptCustoms(mv, SECTION_CUSTOM);

        if (types != null) {
            TypesVisitor tv = mv.visitTypes();
            if (tv != null) types.accept(tv);
        }

        acceptCustoms(mv, SECTION_TYPE);

        if (imports != null) {
            ImportsVisitor iv = mv.visitImports();
            if (iv != null) imports.accept(iv);
        }

        acceptCustoms(mv, SECTION_IMPORT);

        if (funcs != null) {
            FunctionsVisitor fv = mv.visitFuncs();
            if (fv != null) funcs.accept(fv);
        }

        acceptCustoms(mv, SECTION_FUNCTION);

        if (tables != null) {
            TablesVisitor tv = mv.visitTables();
            if (tv != null) tables.accept(tv);
        }

        acceptCustoms(mv, SECTION_TABLE);

        if (mems != null) {
            MemoriesVisitor mmv = mv.visitMems();
            if (mmv != null) mems.accept(mmv);
        }

        acceptCustoms(mv, SECTION_MEMORY);

        if (globals != null) {
            GlobalsVisitor gv = mv.visitGlobals();
            if (gv != null) globals.accept(gv);
        }

        acceptCustoms(mv, SECTION_GLOBAL);

        if (exports != null) {
            ExportsVisitor ev = mv.visitExports();
            if (ev != null) exports.accept(ev);
        }

        acceptCustoms(mv, SECTION_EXPORT);

        if (start != null) {
            mv.visitStart(start.func);
        }

        acceptCustoms(mv, SECTION_START);

        if (elems != null) {
            ElementSegmentsVisitor ev = mv.visitElems();
            if (ev != null) elems.accept(ev);
        }

        acceptCustoms(mv, SECTION_ELEMENT);

        if (dataCount != null) {
            mv.visitDataCount(dataCount);
        }

        acceptCustoms(mv, SECTION_DATA_COUNT);

        if (codes != null) {
            CodesVisitor cv = mv.visitCode();
            if (cv != null) codes.accept(cv);
        }

        acceptCustoms(mv, SECTION_CODE);

        if (datas != null) {
            DataSegmentsVisitor dv = mv.visitDatas();
            if (dv != null) datas.accept(dv);
        }

        acceptCustoms(mv, SECTION_DATA);

        mv.visitEnd();
    }

    private void acceptCustoms(ModuleVisitor mv, byte section) {
        if (customs[section] != null) {
            for (CustomNode node : customs[section]) {
                node.accept(mv);
            }
        }
    }

    public List<CustomNode> getCustoms(byte section) {
        return customs[section] == null ? customs[section] = new ArrayList<>(section) : customs[section];
    }

    @Override
    public void visitHeader(int version) {
        this.version = version;
    }

    @Override
    public void visitCustom(@NotNull String name, byte @NotNull [] payload) {
        getCustoms(section).add(new CustomNode(name, payload));
    }

    @Override
    public @Nullable TypesVisitor visitTypes() {
        section = SECTION_TYPE;
        return types = new TypesNode();
    }

    @Override
    public @Nullable ImportsVisitor visitImports() {
        section = SECTION_IMPORT;
        return imports = new ImportsNode();
    }

    @Override
    public @Nullable FunctionsVisitor visitFuncs() {
        section = SECTION_FUNCTION;
        return funcs = new FunctionsNode();
    }

    @Override
    public @Nullable TablesVisitor visitTables() {
        section = SECTION_TABLE;
        return tables = new TablesNode();
    }

    @Override
    public @Nullable MemoriesVisitor visitMems() {
        section = SECTION_MEMORY;
        return mems = new MemoriesNode();
    }

    @Override
    public @Nullable GlobalsVisitor visitGlobals() {
        section = SECTION_GLOBAL;
        return globals = new GlobalsNode();
    }

    @Override
    public @Nullable ExportsVisitor visitExports() {
        section = SECTION_EXPORT;
        return exports = new ExportsNode();
    }

    @Override
    public void visitStart(int func) {
        section = SECTION_START;
        start = new StartNode(func);
    }

    @Override
    public @Nullable ElementSegmentsVisitor visitElems() {
        section = SECTION_ELEMENT;
        return elems = new ElementSegmentsNode();
    }

    @Override
    public void visitDataCount(int count) {
        section = SECTION_DATA_COUNT;
        this.dataCount = count;
    }

    @Override
    public @Nullable CodesVisitor visitCode() {
        section = SECTION_CODE;
        return codes = new CodesNode();
    }

    @Override
    public @Nullable DataSegmentsVisitor visitDatas() {
        section = SECTION_DATA;
        return datas = new DataSegmentsNode();
    }
}
