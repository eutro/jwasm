package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static io.github.eutro.jwasm.Opcodes.*;

/**
 * A node that represents a WebAssembly
 * <a href="https://webassembly.github.io/spec/core/syntax/modules.html">module</a>.
 */
public class ModuleNode extends ModuleVisitor {
    /**
     * The version field of the module.
     *
     * @see ModuleVisitor#visitHeader(int)
     */
    public int version = Opcodes.VERSION;

    /**
     * An array of lists of {@link CustomNode}s that this module has.
     * <p>
     * Since modules can contain custom sections between any two defined sections, there is a space in this array
     * for a list of custom nodes after each defined section:
     * <p>
     * {@code customs[SECTION_CUSTOM]} is visited before any of the other sections,
     * and {@code customs[SECTION_XXX]} is visited after the section {@code SECTION_XXX},
     * where SECTION_XXX is one of the {@code SECTION_XXX} constants in {@link Opcodes}.
     */
    @SuppressWarnings("unchecked")
    public final List<CustomNode>[] customs = (List<CustomNode>[]) new List<?>[SECTION_DATA_COUNT + 1];

    /**
     * The types of this module.
     *
     * @see ModuleVisitor#visitTypes()
     */
    public @Nullable TypesNode types;

    /**
     * The imports of this module.
     *
     * @see ModuleVisitor#visitImports()
     */
    public @Nullable ImportsNode imports;

    /**
     * The function types of this module.
     *
     * @see ModuleVisitor#visitFuncs()
     */
    public @Nullable FunctionsNode funcs;

    /**
     * The tables of this module.
     *
     * @see ModuleVisitor#visitTables()
     */
    public @Nullable TablesNode tables;

    /**
     * The memories of this module.
     *
     * @see ModuleVisitor#visitMems()
     */
    public @Nullable MemoriesNode mems;

    /**
     * The globals of this module.
     *
     * @see ModuleVisitor#visitGlobals()
     */
    public @Nullable GlobalsNode globals;

    /**
     * The exports of this module.
     *
     * @see ModuleVisitor#visitExports()
     */
    public @Nullable ExportsNode exports;

    /**
     * The start of this module.
     *
     * @see ModuleVisitor#visitStart(int)
     */
    public @Nullable Integer start;

    /**
     * The element segments of this module.
     *
     * @see ModuleVisitor#visitElems()
     */
    public @Nullable ElementSegmentsNode elems;

    /**
     * The data count of this module.
     *
     * @see ModuleVisitor#visitDataCount(int)
     */
    public @Nullable Integer dataCount;

    /**
     * The codes of this module.
     *
     * @see ModuleVisitor#visitCode()
     */
    public @Nullable CodesNode codes;

    /**
     * The datas of this module.
     *
     * @see ModuleVisitor#visitDatas()
     */
    public @Nullable DataSegmentsNode datas;

    private byte section = SECTION_CUSTOM;

    /**
     * Make the given {@link ModuleVisitor} visit the header and all the sections of this node.
     * <p>
     * The order in which custom sections are visited is as described on the {@link #customs customs field}.
     *
     * @param mv The visitor to visit.
     */
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
            mv.visitStart(start);
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

    /**
     * Get the list of custom sections after a given known section, as described in {@link #customs},
     * creating the list if it does not exist yet.
     *
     * @param section The section to put the custom sections after.
     * @return The list of custom nodes following the section.
     */
    public @NotNull List<CustomNode> getCustoms(byte section) {
        return customs[section] == null ? customs[section] = new ArrayList<>(section) : customs[section];
    }

    private void acceptCustoms(ModuleVisitor mv, byte section) {
        if (customs[section] != null) {
            for (CustomNode node : customs[section]) {
                node.accept(mv);
            }
        }
    }

    @Override
    public void visitHeader(int version) {
        this.version = version;
    }

    @Override
    public void visitCustom(@NotNull String name, byte @NotNull [] data) {
        getCustoms(section).add(new CustomNode(name, data));
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
        start = func;
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
