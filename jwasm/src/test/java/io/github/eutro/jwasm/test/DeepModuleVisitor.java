package io.github.eutro.jwasm.test;

import io.github.eutro.jwasm.*;

public class DeepModuleVisitor extends ModuleVisitor {
    public DeepModuleVisitor() {
    }

    @Override
    public TypesVisitor visitTypes() {
        return new TypesVisitor();
    }

    @Override
    public FunctionsVisitor visitFuncs() {
        return new FunctionsVisitor();
    }

    @Override
    public CodesVisitor visitCode() {
        return new CodesVisitor() {
            @Override
            public ExprVisitor visitCode(byte[] locals) {
                return new ExprVisitor();
            }
        };
    }

    @Override
    public TablesVisitor visitTables() {
        return new TablesVisitor();
    }

    @Override
    public MemoriesVisitor visitMems() {
        return new MemoriesVisitor();
    }

    @Override
    public GlobalsVisitor visitGlobals() {
        return new GlobalsVisitor() {
            @Override
            public ExprVisitor visitGlobal(byte mut, byte type) {
                return new ExprVisitor();
            }
        };
    }

    @Override
    public ElementSegmentsVisitor visitElems() {
        return new ElementSegmentsVisitor() {
            @Override
            public ElementVisitor visitElem() {
                return new ElementVisitor() {
                    @Override
                    public ExprVisitor visitActiveMode(int table) {
                        return new ExprVisitor();
                    }

                    @Override
                    public ExprVisitor visitInit() {
                        return new ExprVisitor();
                    }
                };
            }
        };
    }

    @Override
    public DataSegmentsVisitor visitDatas() {
        return new DataSegmentsVisitor() {
            @Override
            public DataVisitor visitData() {
                return new DataVisitor() {
                    @Override
                    public ExprVisitor visitActive(int memory) {
                        return new ExprVisitor();
                    }
                };
            }
        };
    }

    @Override
    public ExportsVisitor visitExports() {
        return new ExportsVisitor();
    }

    @Override
    public ImportsVisitor visitImports() {
        return new ImportsVisitor();
    }
}