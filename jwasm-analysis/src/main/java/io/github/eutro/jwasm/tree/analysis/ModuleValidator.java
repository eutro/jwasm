package io.github.eutro.jwasm.tree.analysis;

import io.github.eutro.jwasm.*;
import io.github.eutro.jwasm.tree.ElementNode;
import io.github.eutro.jwasm.tree.FuncNode;
import io.github.eutro.jwasm.tree.ModuleNode;
import io.github.eutro.jwasm.tree.TypeNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.eutro.jwasm.tree.analysis.ExprValidator.isRef;
import static io.github.eutro.jwasm.tree.analysis.ExprValidator.isValType;

/**
 * An {@link ModuleVisitor} that verifies whether the module is well-formed.
 */
public class ModuleValidator extends ModuleNode implements Validator {
    public List<FuncNode> referencableFuncs = new ArrayList<>();

    public ModuleValidator() {
        super();
    }

    public ModuleValidator(@Nullable ModuleVisitor dl) {
        super(dl);
    }

    protected ExprVisitor wrapExprVisitor(ExprValidator ev) {
        return ev;
    }

    @Contract("false, _, _ -> fail")
    public static void assertMsg(boolean cond, String fmt, Object... args) {
        if (!cond) {
            throw new ValidationException(String.format(fmt, args), null);
        }
    }

    private void checkLimit(int min, @Nullable Integer max, int k) {
        assertMsg(min >= 0, "min must be positive");
        assertMsg(min <= k, "min must not exceed %d", k);
        if (max != null) {
            assertMsg(min <= max, "min must be no greater than max");
            assertMsg(max <= k, "max must not exceed %d", k);
        }
    }

    private void checkFuncType(byte[] params, byte[] returns) {
        checkTypes(params);
        checkTypes(returns);
    }

    private void checkTypes(byte... tys) {
        for (byte ty : tys) {
            assertMsg(isValType(ty), "0x%02x is not a value type", ty);
        }
    }

    private void checkGlobal(byte mut, byte type) {
        assertMsg(mut == Opcodes.MUT_CONST || mut == Opcodes.MUT_VAR,
                "mut is neither const nor var");
        checkTypes(type);
    }

    private void checkMem(int min, @Nullable Integer max) {
        checkLimit(min, max, 1 << 16);
    }

    private void checkTable(int min, @Nullable Integer max, byte type) {
        checkLimit(min, max, Integer.MAX_VALUE);
        assertMsg(isRef(type), "0x%02x is not a reference type", type);
    }

    private FuncNode getFunc(int func, boolean includeImports) {
        if (includeImports) {
            assertMsg(referencableFuncs.size() > func, "func %d does not exist", func);
            return referencableFuncs.get(func);
        } else {
            assertMsg(funcs != null && funcs.funcs != null && funcs.funcs.size() > func,
                    "func %d does not exist", func);
            return funcs.funcs.get(func);
        }
    }

    private TypeNode checkFuncType(int type) {
        assertMsg(types != null && types.types != null && types.types.size() > type,
                "type %d does not exist", type);
        return types.types.get(type);
    }

    @Override
    public @Nullable TypesVisitor visitTypes() {
        return new TypesVisitor(super.visitTypes()) {
            @Override
            public void visitFuncType(byte @NotNull [] params, byte @NotNull [] returns) {
                checkFuncType(params, returns);
                super.visitFuncType(params, returns);
            }
        };
    }

    @Override
    public @Nullable TablesVisitor visitTables() {
        return new TablesVisitor(super.visitTables()) {
            @Override
            public void visitTable(int min, @Nullable Integer max, byte type) {
                checkTable(min, max, type);
                super.visitTable(min, max, type);
            }
        };
    }

    @Override
    public @Nullable MemoriesVisitor visitMems() {
        return new MemoriesVisitor(super.visitMems()) {
            @Override
            public void visitMemory(int min, @Nullable Integer max) {
                checkMem(min, max);
                super.visitMemory(min, max);
            }
        };
    }

    @Override
    public @Nullable GlobalsVisitor visitGlobals() {
        return new GlobalsVisitor(super.visitGlobals()) {
            @Override
            public ExprVisitor visitGlobal(byte mut, byte type) {
                checkGlobal(mut, type);
                return wrapExprVisitor(new ExprValidator(
                        ModuleValidator.this,
                        new byte[0],
                        null,
                        new byte[]{type},
                        new ConstantExprValidator(
                                ModuleValidator.this,
                                super.visitGlobal(mut, type)
                        )
                ));
            }
        };
    }

    @Override
    public @Nullable FunctionsVisitor visitFuncs() {
        return new FunctionsVisitor(super.visitFuncs()) {
            @Override
            public void visitFunc(int type) {
                super.visitFunc(type);
                checkFuncType(type);
                Objects.requireNonNull(funcs);
                Objects.requireNonNull(funcs.funcs);
                referencableFuncs.add(funcs.funcs.get(funcs.funcs.size() - 1));
            }
        };
    }

    @Override
    public @Nullable CodesVisitor visitCode() {
        return new CodesVisitor(super.visitCode()) {
            int func = 0;

            @Override
            public ExprVisitor visitCode(byte @NotNull [] locals) {
                FuncNode fn = getFunc(func++, false);
                TypeNode tn = checkFuncType(fn.type);
                byte[] allLocals = new byte[tn.params.length + locals.length];
                System.arraycopy(tn.params, 0, allLocals, 0, tn.params.length);
                System.arraycopy(locals, 0, allLocals, tn.params.length, locals.length);
                return wrapExprVisitor(new ExprValidator(
                        ModuleValidator.this,
                        allLocals,
                        tn.returns,
                        tn.returns,
                        super.visitCode(locals)
                ));
            }
        };
    }

    @Override
    public @Nullable DataSegmentsVisitor visitDatas() {
        return new DataSegmentsVisitor(super.visitDatas()) {
            int dc = 0;

            @Override
            public DataVisitor visitData() {
                dc++;
                return new DataVisitor(super.visitData()) {
                    @Override
                    public ExprVisitor visitActive(int memory) {
                        assertMsg(mems != null && mems.memories != null && mems.memories.size() > memory,
                                "memory %d does not exist", memory);
                        return wrapExprVisitor(new ExprValidator(
                                ModuleValidator.this,
                                new byte[0],
                                null,
                                new byte[]{Opcodes.I32},
                                new ConstantExprValidator(
                                        ModuleValidator.this,
                                        super.visitActive(memory)
                                )
                        ));
                    }
                };
            }

            @Override
            public void visitEnd() {
                if (dataCount != null) {
                    assertMsg(dc == dataCount, "data count mismatch");
                }
                super.visitEnd();
            }
        };
    }

    @Override
    public @Nullable ElementSegmentsVisitor visitElems() {
        return new ElementSegmentsVisitor(super.visitElems()) {
            @Override
            public ElementVisitor visitElem() {
                return new ElementNode(super.visitElem()) {
                    private void checkRefType(byte type) {
                        assertMsg(isRef(type), "elem type must be reference type");
                    }

                    @Override
                    public void visitType(byte type) {
                        super.visitType(type);
                        checkRefType(type);
                    }

                    @Override
                    public ExprVisitor visitInit() {
                        checkRefType(type);
                        return wrapExprVisitor(new ExprValidator(
                                ModuleValidator.this,
                                new byte[0],
                                null,
                                new byte[]{type},
                                new ConstantExprValidator(
                                        ModuleValidator.this,
                                        super.visitInit()
                                )
                        ));
                    }

                    @Override
                    public ExprVisitor visitActiveMode(int table) {
                        return wrapExprVisitor(new ExprValidator(
                                ModuleValidator.this,
                                new byte[0],
                                null,
                                new byte[]{Opcodes.I32},
                                new ConstantExprValidator(
                                        ModuleValidator.this,
                                        super.visitActiveMode(table)
                                )
                        ));
                    }
                };
            }
        };
    }

    @Override
    public void visitStart(int func) {
        super.visitStart(func);
        FuncNode start = getFunc(func, true);
        TypeNode type = checkFuncType(start.type);
        assertMsg(type.params.length == 0 && type.returns.length == 0,
                "start function must be of type [] -> []");
    }

    @Override
    public @Nullable ImportsVisitor visitImports() {
        return new ImportsVisitor(super.visitImports()) {
            @Override
            public void visitFuncImport(@NotNull String module, @NotNull String name, int type) {
                super.visitFuncImport(module, name, type);
                checkFuncType(type);
                referencableFuncs.add(new FuncNode(type));
            }

            @Override
            public void visitTableImport(@NotNull String module, @NotNull String name, int min, @Nullable Integer max, byte type) {
                super.visitTableImport(module, name, min, max, type);
                checkTable(min, max, type);
            }

            @Override
            public void visitMemImport(@NotNull String module, @NotNull String name, int min, @Nullable Integer max) {
                super.visitMemImport(module, name, min, max);
                checkMem(min, max);
            }

            @Override
            public void visitGlobalImport(@NotNull String module, @NotNull String name, byte mut, byte type) {
                super.visitGlobalImport(module, name, mut, type);
                checkGlobal(mut, type);
            }
        };
    }

    @Override
    public @Nullable ExportsVisitor visitExports() {
        return new ExportsVisitor(super.visitExports()) {
            @Override
            public void visitExport(@NotNull String name, byte type, int index) {
                switch (type) {
                    case Opcodes.EXPORTS_FUNC:
                        getFunc(index, true);
                        break;
                    case Opcodes.EXPORTS_TABLE:
                        assertMsg(tables != null && tables.tables != null && tables.tables.size() > index,
                                "table %d does not exist", index);
                        break;
                    case Opcodes.EXPORTS_MEM:
                        assertMsg(mems != null && mems.memories != null && mems.memories.size() > index,
                                "mem %d does not exist", index);
                        break;
                    case Opcodes.EXPORTS_GLOBAL:
                        assertMsg(globals != null && globals.globals != null && globals.globals.size() > index,
                                "global %d does not exist", index);
                        break;
                    default:
                        throw new ValidationException(String.format("Export type %d does not exist", type), null);
                }
                super.visitExport(name, type, index);
            }
        };
    }
}
