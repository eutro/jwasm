package io.github.eutro.jwasm.test;

import io.github.eutro.jwasm.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeepModuleVisitor extends ModuleVisitor {
    public DeepModuleVisitor() {
    }

    @Override
    public @Nullable TypesVisitor visitTypes() {
        return new TypesVisitor();
    }

    @Override
    public @Nullable FunctionsVisitor visitFuncs() {
        return new FunctionsVisitor();
    }

    @Override
    public @Nullable CodesVisitor visitCode() {
        return new CodesVisitor() {
            @Override
            public ExprVisitor visitCode(byte @NotNull [] locals) {
                return new ExprVisitor();
            }
        };
    }

    @Override
    public @Nullable TablesVisitor visitTables() {
        return new TablesVisitor();
    }

    @Override
    public @Nullable MemoriesVisitor visitMems() {
        return new MemoriesVisitor();
    }

    @Override
    public @Nullable GlobalsVisitor visitGlobals() {
        return new GlobalsVisitor() {
            @Override
            public ExprVisitor visitGlobal(byte mut, byte type) {
                return new ExprVisitor();
            }
        };
    }

    @Override
    public @Nullable ElementSegmentsVisitor visitElems() {
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
    public @Nullable DataSegmentsVisitor visitDatas() {
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
    public @Nullable ExportsVisitor visitExports() {
        return new ExportsVisitor();
    }

    @Override
    public @Nullable ImportsVisitor visitImports() {
        return new ImportsVisitor();
    }
}