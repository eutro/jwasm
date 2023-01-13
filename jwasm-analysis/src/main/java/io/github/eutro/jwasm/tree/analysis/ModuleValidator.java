package io.github.eutro.jwasm.tree.analysis;

import io.github.eutro.jwasm.*;
import io.github.eutro.jwasm.tree.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import static io.github.eutro.jwasm.tree.analysis.ExprValidator.*;

/**
 * An {@link ModuleVisitor} that verifies whether the module is
 * <a href="https://webassembly.github.io/spec/core/valid/index.html">well-formed</a>.
 */
public class ModuleValidator extends ModuleVisitor {
    @NotNull
    private final ModuleNode module;

    public ModuleValidator(@Nullable ModuleVisitor dl) {
        super(new ModuleNode(dl));
        assert this.dl instanceof ModuleNode;
        module = (ModuleNode) this.dl;
    }

    public ModuleValidator() {
        this(null);
    }

    @Contract("false, _, _ -> fail")
    public static void assertMsg(boolean cond, String fmt, Object... args) {
        if (!cond) {
            throw new ValidationException(String.format(fmt, args), null);
        }
    }

    public static <T> T assertExists(List<T> list, int index, String name) {
        assertExists(list.size(), index, name);
        return list.get(index);
    }

    public static void assertExists(int list, int index, String name) {
        if (list > index) {
            return;
        }
        throw new ValidationException(String.format("%s at index %d does not exist", name, index),
                new RuntimeException("unknown " + name));
    }

    @Contract("false, _, _, _ -> fail")
    public static void assertMsg1(boolean cond, String msg, String fmt, Object... args) {
        if (!cond) {
            throw new ValidationException(String.format(fmt, args),
                    new RuntimeException(msg));
        }
    }

    private void checkLimit(int min, @Nullable Integer max, int k, String limitMsg) {
        assertMsg(Integer.compareUnsigned(min, 0) >= 0, "min must be positive");
        assertMsg1(Integer.compareUnsigned(min, k) <= 0, limitMsg, "min must not exceed %d", k);
        if (max != null) {
            assertMsg(Integer.compareUnsigned(min, max) <= 0,
                    "size minimum must not be greater than maximum");
            assertMsg1(Integer.compareUnsigned(max, k) <= 0, limitMsg, "max must not exceed %d", k);
        }
    }

    private void checkFuncType(byte[] params, byte[] returns) {
        checkTypes(params);
        checkTypes(returns);
    }

    private void checkFuncType(TypeNode ty) {
        checkFuncType(ty.params, ty.returns);
    }

    private void checkTypes(byte... tys) {
        for (byte ty : tys) {
            assertMsg(isValType(ty), "0x%02x is not a value type", ty);
        }
    }

    private void checkGlobalTy(GlobalTypeNode ty) {
        assertMsg(ty.mut == Opcodes.MUT_CONST || ty.mut == Opcodes.MUT_VAR,
                "mut is neither const nor var");
        checkTypes(ty.type);
    }

    private void checkMemTy(Limits limits) {
        checkLimit(limits.min, limits.max, 1 << 16, "memory size must be at most 65536 pages (4GiB)");
    }

    private void checkTableTy(Limits limits, byte type) {
        checkLimit(limits.min, limits.max, -1, "table size must be at most 2^32 - 1");
        assertMsg(isRef(type), "0x%02x is not a reference type", type);
    }

    @Override
    public void visitEnd() {
        VerifCtx ctx = new VerifCtx();
        VerifCtx ctx2 = new VerifCtx();
        if (module.types != null) {
            ctx.types.addAll(module.types.types);
            for (TypeNode type : ctx.types) {
                checkFuncType(type);
            }
        }

        if (module.imports != null) {
            for (AbstractImportNode theImport : module.imports) {
                switch (theImport.importType()) {
                    case Opcodes.IMPORTS_FUNC: {
                        FuncImportNode fin = (FuncImportNode) theImport;
                        ctx.funcs.add(ctx.resolveType(fin.type));
                        break;
                    }
                    case Opcodes.IMPORTS_TABLE: {
                        TableImportNode tin = (TableImportNode) theImport;
                        checkTableTy(tin.limits, tin.type);
                        ctx.tables.add(new TableNode(tin.limits, tin.type));
                        break;
                    }
                    case Opcodes.IMPORTS_MEM: {
                        MemImportNode min = (MemImportNode) theImport;
                        checkMemTy(min.limits);
                        ctx.mems.add(new MemoryNode(min.limits));
                        break;
                    }
                    case Opcodes.IMPORTS_GLOBAL: {
                        GlobalImportNode gin = (GlobalImportNode) theImport;
                        checkGlobalTy(gin.type);
                        ctx.globals.add(gin.type);
                        ctx2.globals.add(gin.type);
                        break;
                    }
                    default:
                        throw new IllegalStateException();
                }
            }
        }

        if (module.funcs != null) {
            for (FuncNode func : module.funcs) {
                ctx.funcs.add(ctx.resolveType(func.type));
            }
        }
        if (module.tables != null) {
            for (TableNode table : module.tables) {
                ctx.tables.add(table);
            }
        }
        if (module.mems != null) {
            for (MemoryNode mem : module.mems) {
                ctx.mems.add(mem);
            }
        }
        if (module.globals != null) {
            for (GlobalNode global : module.globals) {
                ctx.globals.add(global.type);
            }
        }

        if (module.elems != null) {
            for (ElementNode elem : module.elems) {
                ctx.elems.add(elem.type);
            }
        }

        ctx.datas = module.datas == null ? 0 : module.datas.datas.size();

        collectRefs(ctx);

        ctx2.funcs.addAll(ctx.funcs);
        ctx2.refs.addAll(ctx.refs);

        { // under ctx
            int funcCount = 0, codeCount = 0;
            if (module.funcs != null) funcCount = module.funcs.funcs.size();
            if (module.codes != null) codeCount = module.codes.codes.size();
            assertMsg(funcCount == codeCount, "function count (%d) does not match code count (%d)", funcCount, codeCount);

            if (module.codes != null && module.funcs != null) {
                ListIterator<FuncNode> fi = module.funcs.funcs.listIterator();
                Iterator<CodeNode> ci = module.codes.iterator();
                while (fi.hasNext()) {
                    TypeNode ty = ctx.resolveType(fi.next().type);
                    CodeNode code = ci.next();
                    verifyFunc(ctx, fi.previousIndex(), ty, code);
                }
            }
            if (module.start != null) {
                TypeNode ty = assertExists(ctx.funcs, module.start, "function");
                assertMsg1(new TypeNode(new byte[0], new byte[0]).equals(ty), "start function",
                        "start function %d does not have type [] -> []", module.start);
            }

            // we've already checked our imports
            if (module.exports != null) {
                for (ExportNode export : module.exports) {
                    String name;
                    List<?> list;
                    switch (export.type) {
                        case Opcodes.EXPORTS_FUNC: name = "function"; list = ctx.funcs; break;
                        case Opcodes.EXPORTS_TABLE: name = "table"; list = ctx.tables; break;
                        case Opcodes.EXPORTS_MEM: name = "memory"; list = ctx.mems; break;
                        case Opcodes.EXPORTS_GLOBAL: name = "global"; list = ctx.globals; break;
                        default:
                            throw new ValidationException("Unrecognised export type");
                    }
                    assertExists(list, export.index, name);
                }
            }
        }

        { // under ctx2
            if (module.tables != null) {
                for (TableNode table : module.tables) {
                    checkTableTy(table.limits, table.type);
                }
            }
            if (module.mems != null) {
                for (MemoryNode mem : module.mems) {
                    checkMemTy(mem.limits);
                }
            }
            if (module.globals != null) {
                for (GlobalNode global : module.globals) {
                    checkGlobalTy(global.type);
                    global.init.accept(new ConstantExprValidator(ctx2,
                            new ExprValidator(ctx2,
                                    Collections.singletonList(global.type.type),
                                    null)));
                }
            }
            if (module.elems != null) {
                int i = 0;
                for (ElementNode elem : module.elems) {
                    Supplier<ExprVisitor> evSupplier = () -> new ConstantExprValidator(ctx2,
                            new ExprValidator(ctx2, Collections.singletonList(elem.type), null));
                    if (elem.indices != null) {
                        for (int index : elem.indices) {
                            ExprVisitor ev = evSupplier.get();
                            ev.visitFuncRefInsn(index);
                            ev.visitEndInsn();
                            ev.visitEnd();
                        }
                    } else {
                        for (ExprNode expr : elem.init) {
                            expr.accept(evSupplier.get());
                        }
                    }

                    if (elem.offset != null) {
                        // active
                        // NB: see below about active datas
                        TableNode table = assertExists(ctx.tables, elem.table, "table");
                        if (table.type != elem.type) {
                            throw new ValidationException(
                                    String.format("Table %d and element %d types don't match", elem.table, i),
                                    new RuntimeException(TYPE_MISMATCH)
                            );
                        }
                        elem.offset.accept(new ConstantExprValidator(ctx2,
                                new ExprValidator(ctx2, Collections.singletonList(Opcodes.I32), null)));
                    } // declarative and passive are always ok
                    i++;
                }
            }
            if (module.datas != null) {
                for (DataNode data : module.datas) {
                    if (data.offset != null) {
                        // NB: the spec has us validate this whole thing in C prime,
                        // but mems and tables aren't visible there, so this should fail
                        assertExists(ctx.mems, data.memory, "memory");
                        data.offset.accept(new ConstantExprValidator(ctx2,
                                new ExprValidator(ctx2, Collections.singletonList(Opcodes.I32),
                                        null)));
                    }
                }
            }
        }

        assertMsg1(ctx.mems.size() <= 1, "multiple memories",
                "Too many memories (%d)", ctx.mems.size());

        if (module.exports != null) {
            Set<String> names = new HashSet<>();
            for (ExportNode export : module.exports) {
                if (!names.add(export.name)) {
                    throw new ValidationException(String.format("Duplicate export name: \"%s\"", export.name),
                            new RuntimeException("duplicate export name"));
                }
            }
        }
    }

    private void collectRefs(VerifCtx ctx) {
        ExprVisitor refCollector = new ExprVisitor() {
            @Override
            public void visitFuncRefInsn(int function) {
                assertExists(ctx.funcs, function, "function");
                ctx.refs.add(function);
            }
        };
        if (module.globals != null) {
            for (GlobalNode global : module.globals) {
                global.init.accept(refCollector);
            }
        }
        if (module.exports != null) {
            for (ExportNode export : module.exports) {
                if (export.type == Opcodes.EXPORTS_FUNC) {
                    assertExists(ctx.funcs, export.index, "function");
                    ctx.refs.add(export.index);
                }
            }
        }
        if (module.datas != null) {
            for (DataNode data : module.datas) {
                if (data.offset != null) {
                    data.offset.accept(refCollector);
                }
            }
        }
        if (module.elems != null) {
            for (ElementNode elem : module.elems) {
                if (elem.indices != null) {
                    for (int index : elem.indices) {
                        assertExists(ctx.funcs, index, "function");
                        ctx.refs.add(index);
                    }
                } else {
                    for (ExprNode expr : elem.init) {
                        expr.accept(refCollector);
                    }
                }
                if (elem.offset != null) {
                    elem.offset.accept(refCollector);
                }
            }
        }
    }

    private void verifyFunc(VerifCtx ctx, int index, TypeNode ty, CodeNode code) {
        byte[] locals = Arrays.copyOf(ty.params, ty.params.length + code.locals.length);
        System.arraycopy(code.locals, 0, locals, ty.params.length, code.locals.length);
        VerifCtx ctx1 = ctx.deriveLocals(
                new ByteList(locals),
                new ByteList(ty.returns)
        );
        try {
            code.expr.accept(new ExprValidator(ctx1, ctx1.returns, null));
        } catch (Throwable t) {
            t.addSuppressed(new RuntimeException("in func " + index + " (local index)"));
            throw t;
        }
    }
}
