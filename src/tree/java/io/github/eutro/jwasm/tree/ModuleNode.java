package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.*;

import java.util.ArrayList;
import java.util.List;

public class ModuleNode extends ModuleVisitor {
    public int version = Opcodes.VERSION;
    public List<CustomNode> customs;
    public TypesNode types;
    public FunctionsNode funcs;
    public TablesNode tables;
    public MemoriesNode mems;
    public GlobalsNode globals;
    public ElementSegmentsNode elems;
    public CodesNode codes;
    public DataSegmentsNode datas;
    public StartNode start;
    public ImportsNode imports;
    public ExportsNode exports;

    public void accept(ModuleVisitor mv) {
        mv.visit(version);

        if (types != null) {
            TypesVisitor tv = mv.visitTypes();
            if (tv != null) types.accept(tv);
        }

        if (imports != null) {
            ImportsVisitor iv = mv.visitImports();
            if (iv != null) imports.accept(iv);
        }

        if (funcs != null) {
            FunctionsVisitor fv = mv.visitFuncs();
            if (fv != null) funcs.accept(fv);
        }

        if (tables != null) {
            TablesVisitor tv = mv.visitTables();
            if (tv != null) tables.accept(tv);
        }

        if (mems != null) {
            MemoriesVisitor mmv = mv.visitMems();
            if (mmv != null) mems.accept(mmv);
        }

        if (globals != null) {
            GlobalsVisitor gv = mv.visitGlobals();
            if (gv != null) globals.accept(gv);
        }

        if (exports != null) {
            ExportsVisitor ev = mv.visitExports();
            if (ev != null) exports.accept(ev);
        }

        if (start != null) {
            mv.visitStart(start.func);
        }

        if (elems != null) {
            ElementSegmentsVisitor ev = mv.visitElems();
            if (ev != null) elems.accept(ev);
        }

        if (codes != null) {
            CodesVisitor cv = mv.visitCode();
            if (cv != null) codes.accept(cv);
        }

        if (datas != null) {
            DataSegmentsVisitor dv = mv.visitDatas();
            if (dv != null) datas.accept(dv);
        }

        if (customs != null) {
            for (CustomNode custom : customs) {
                mv.visitCustom(custom.name, custom.data);
            }
        }

        mv.visitEnd();
    }

    @Override
    public void visit(int version) {
        this.version = version;
    }

    @Override
    public void visitCustom(String name, byte[] payload) {
        if (customs == null) customs = new ArrayList<>();
        customs.add(new CustomNode(name, payload));
    }

    @Override
    public TypesVisitor visitTypes() {
        return types = new TypesNode();
    }

    @Override
    public FunctionsVisitor visitFuncs() {
        return funcs = new FunctionsNode();
    }

    @Override
    public TablesVisitor visitTables() {
        return tables = new TablesNode();
    }

    @Override
    public MemoriesVisitor visitMems() {
        return mems = new MemoriesNode();
    }

    @Override
    public GlobalsVisitor visitGlobals() {
        return globals = new GlobalsNode();
    }

    @Override
    public ElementSegmentsVisitor visitElems() {
        return elems = new ElementSegmentsNode();
    }

    @Override
    public CodesVisitor visitCode() {
        return codes = new CodesNode();
    }

    @Override
    public DataSegmentsVisitor visitDatas() {
        return datas = new DataSegmentsNode();
    }

    @Override
    public void visitStart(int func) {
        start = new StartNode(func);
    }

    @Override
    public ExportsVisitor visitExports() {
        return exports = new ExportsNode();
    }

    @Override
    public ImportsVisitor visitImports() {
        return imports = new ImportsNode();
    }
}
