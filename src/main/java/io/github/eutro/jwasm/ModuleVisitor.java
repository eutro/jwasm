package io.github.eutro.jwasm;

public class ModuleVisitor extends BaseVisitor<ModuleVisitor> {
    public ModuleVisitor() {
    }

    public ModuleVisitor(ModuleVisitor dl) {
        super(dl);
    }

    public void visit(int version) {
        if (dl != null) dl.visit(version);
    }

    public void visitCustom(String name, byte[] payload) {
        if (dl != null) dl.visitCustom(name, payload);
    }

    public TypesVisitor visitTypes() {
        if (dl != null) return dl.visitTypes();
        return null;
    }

    public FunctionsVisitor visitFuncs() {
        if (dl != null) return dl.visitFuncs();
        return null;
    }

    public TablesVisitor visitTables() {
        if (dl != null) return dl.visitTables();
        return null;
    }

    public MemoriesVisitor visitMems() {
        if (dl != null) return dl.visitMems();
        return null;
    }

    public GlobalsVisitor visitGlobals() {
        if (dl != null) return dl.visitGlobals();
        return null;
    }

    public ElementSegmentsVisitor visitElems() {
        if (dl != null) return dl.visitElems();
        return null;
    }

    public DataSegmentsVisitor visitDatas() {
        if (dl != null) return dl.visitDatas();
        return null;
    }

    public void visitStart(int func) {
        if (dl != null) dl.visitStart(func);
    }

    public ExportsVisitor visitExports() {
        if (dl != null) return dl.visitExports();
        return null;
    }

    public ImportsVisitor visitImports() {
        if (dl != null) return dl.visitImports();
        return null;
    }
}
