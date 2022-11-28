package io.github.eutro.jwasm.sexp;

import io.github.eutro.jwasm.BlockType;
import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Limits;
import io.github.eutro.jwasm.Opcodes;
import io.github.eutro.jwasm.attrs.InsnAttributes;
import io.github.eutro.jwasm.tree.*;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A class for converting a parsed {@link ModuleNode} into a Wat parsed s-expression.
 *
 * @see Parser
 * @see Writer
 */
public class Unparser {
    public static Object unparse(ModuleNode node) {
        List<Object> moduleList = new ArrayList<>();
        moduleList.add("module");

        if (node.types != null) {
            for (TypeNode type : node.types) {
                List<Object> typeList = new ArrayList<>();
                typeList.add("type");

                List<Object> funcList = new ArrayList<>();
                funcList.add("func");

                if (type.params.length != 0) {
                    List<Object> params = new ArrayList<>();
                    params.add("param");
                    funcList.add(params);
                    unparseTypes(params, type.params);
                }

                if (type.returns.length != 0) {
                    List<Object> results = new ArrayList<>();
                    results.add("result");
                    funcList.add(results);
                    unparseTypes(results, type.returns);
                }

                typeList.add(funcList);
                moduleList.add(typeList);
            }
        }

        if (node.imports != null) {
            for (AbstractImportNode theImport : node.imports) {
                List<Object> importList = new ArrayList<>();
                importList.add("import");
                importList.add(theImport.module.getBytes(StandardCharsets.UTF_8));
                importList.add(theImport.name.getBytes(StandardCharsets.UTF_8));

                List<Object> descList = new ArrayList<>();
                switch (theImport.importType()) {
                    case Opcodes.IMPORTS_FUNC: {
                        descList.add("func");
                        FuncImportNode fNode = (FuncImportNode) theImport;
                        descList.add(Arrays.asList("type", BigInteger.valueOf(fNode.type)));
                        break;
                    }
                    case Opcodes.IMPORTS_TABLE: {
                        descList.add("table");
                        TableImportNode tNode = (TableImportNode) theImport;
                        descList.addAll(unparseLimits(tNode.limits));
                        descList.add(unparseType(tNode.type));
                        break;
                    }
                    case Opcodes.IMPORTS_MEM: {
                        descList.add("memory");
                        MemImportNode mNode = (MemImportNode) theImport;
                        descList.addAll(unparseLimits(mNode.limits));
                        break;
                    }
                    case Opcodes.IMPORTS_GLOBAL: {
                        descList.add("global");
                        GlobalImportNode gNode = (GlobalImportNode) theImport;
                        descList.add(unparseGlobalType(gNode.type));
                        break;
                    }
                    default:
                        throw new IllegalStateException();
                }

                importList.add(descList);
                moduleList.add(importList);
            }
        }

        if (node.tables != null) {
            for (TableNode table : node.tables) {
                List<Object> tableList = new ArrayList<>();
                tableList.add("table");
                tableList.addAll(unparseLimits(table.limits));
                tableList.add(unparseType(table.type));
                moduleList.add(tableList);
            }
        }

        if (node.mems != null) {
            for (MemoryNode mem : node.mems) {
                List<Object> memList = new ArrayList<>();
                memList.add("memory");
                memList.addAll(unparseLimits(mem.limits));
                moduleList.add(memList);
            }
        }

        if (node.globals != null) {
            for (GlobalNode global : node.globals) {
                List<Object> globalList = new ArrayList<>();
                globalList.add("global");
                globalList.add(unparseGlobalType(global.type));
                unparseExpr(globalList, global.init);

                moduleList.add(globalList);
            }
        }

        if (node.exports != null) {
            for (ExportNode export : node.exports) {
                List<Object> exportList = new ArrayList<>();
                exportList.add("export");
                exportList.add(export.name.getBytes(StandardCharsets.UTF_8));

                List<Object> descList = new ArrayList<>();
                switch (export.type) {
                    // @formatter:off
                    case Opcodes.EXPORTS_FUNC: descList.add("func"); break;
                    case Opcodes.EXPORTS_TABLE: descList.add("table"); break;
                    case Opcodes.EXPORTS_MEM: descList.add("memory"); break;
                    case Opcodes.EXPORTS_GLOBAL: descList.add("global"); break;
                    // @formatter:on
                    default:
                        throw new IllegalStateException();
                }
                descList.add(export.index);

                exportList.add(descList);
                moduleList.add(exportList);
            }
        }

        if (node.start != null) {
            moduleList.add(Arrays.asList("start", BigInteger.valueOf(node.start)));
        }

        if (node.elems != null) {
            for (ElementNode elem : node.elems) {
                List<Object> elemList = new ArrayList<>();
                elemList.add("elem");

                if (elem.offset == null) {
                    if (!elem.passive) {
                        // declarative
                        elemList.add("declare");
                    } // passive otherwise
                } else {
                    // active
                    elemList.add(Arrays.asList("table", BigInteger.valueOf(elem.table)));
                    List<Object> offsetList = new ArrayList<>();
                    offsetList.add("offset");
                    unparseExpr(offsetList, elem.offset);
                    elemList.add(offsetList);
                }

                if (elem.init == null) {
                    elemList.add("func");
                    for (int index : elem.indices) {
                        elemList.add(BigInteger.valueOf(index));
                    }
                } else {
                    elemList.add(unparseType(elem.type));
                    for (ExprNode init : elem.init) {
                        List<Object> itemList = new ArrayList<>();
                        unparseExpr(itemList, init);

                        if (itemList.size() > 1) {
                            itemList.add(0, "item"); // single insn abbrev otherwise
                        }

                        elemList.add(itemList);
                    }
                }

                moduleList.add(elemList);
            }
        }

        if (node.codes != null && node.codes.codes != null && !node.codes.codes.isEmpty()) {
            assert node.funcs != null;

            Iterator<FuncNode> fi = node.funcs.iterator();
            Iterator<CodeNode> ci = node.codes.iterator();
            while (fi.hasNext()) {
                assert ci.hasNext();
                FuncNode fn = fi.next();
                CodeNode cn = ci.next();
                List<Object> funcList = new ArrayList<>();
                funcList.add("func");
                funcList.add(BigInteger.valueOf(fn.type));

                if (cn.locals.length != 0) {
                    List<Object> localsList = new ArrayList<>();
                    localsList.add("local");
                    for (byte localTy : cn.locals) {
                        localsList.add(unparseType(localTy));
                    }
                    funcList.add(localsList);
                }

                unparseExpr(funcList, cn.expr);

                moduleList.add(funcList);
            }
            assert !ci.hasNext();
        }

        if (node.datas != null) {
            for (DataNode data : node.datas) {
                List<Object> dataList = new ArrayList<>();
                dataList.add("data");
                if (data.offset != null) {
                    dataList.add(Arrays.asList("memory", BigInteger.valueOf(data.memory)));

                    List<Object> offsetList = new ArrayList<>();
                    offsetList.add("offset");
                    unparseExpr(offsetList, data.offset);
                    dataList.add(offsetList);
                }
                dataList.add(data.init);

                moduleList.add(dataList);
            }
        }

        return moduleList;
    }

    @NotNull
    private static Object unparseGlobalType(GlobalTypeNode type) {
        String refTy = unparseType(type.type);
        return type.mut == Opcodes.MUT_CONST ? refTy : Arrays.asList("mut", refTy);
    }

    private static void unparseExpr(List<Object> t, ExprNode expr) {
        if (expr.instructions == null) return;
        ExprVisitor ev = new ExprVisitor() {
            @Override
            public void visitInsn(byte opcode) {
                t.add(InsnAttributes.lookup(opcode).getMnemonic());
            }

            @Override
            public void visitPrefixInsn(int opcode) {
                t.add(InsnAttributes.lookupPrefix(opcode).getMnemonic());
            }

            @Override
            public void visitConstInsn(Object v) {
                if (v instanceof Integer) t.add(Arrays.asList("i32.const", BigInteger.valueOf((int) v)));
                else if (v instanceof Long) t.add(Arrays.asList("i64.const", BigInteger.valueOf((long) v)));
                else if (v instanceof Float) t.add(Arrays.asList("f32.const", (double) (float) v));
                else if (v instanceof Double) t.add(Arrays.asList("f64.const", (double) v));
                else throw new IllegalArgumentException();
            }

            @Override
            public void visitNullInsn(byte type) {
                t.add(Arrays.asList("ref.null", type == Opcodes.EXTERNREF ? "extern" : "func"));
            }

            @Override
            public void visitFuncRefInsn(int function) {
                t.add(Arrays.asList("ref.func", BigInteger.valueOf(function)));
            }

            @Override
            public void visitSelectInsn(byte[] type) {
                List<Object> selectList = new ArrayList<>();
                selectList.add("select");
                unparseTypes(selectList, type);
                t.add(selectList);
            }

            @Override
            public void visitVariableInsn(byte opcode, int variable) {
                t.add(Arrays.asList(InsnAttributes.lookup(opcode).getMnemonic(), BigInteger.valueOf(variable)));
            }

            @Override
            public void visitTableInsn(byte opcode, int table) {
                t.add(Arrays.asList(InsnAttributes.lookup(opcode).getMnemonic(), BigInteger.valueOf(table)));
            }

            @Override
            public void visitPrefixTableInsn(int opcode, int table) {
                t.add(Arrays.asList(InsnAttributes.lookupPrefix(opcode).getMnemonic(),
                        BigInteger.valueOf(table)));
            }

            @Override
            public void visitPrefixBinaryTableInsn(int opcode, int firstIndex, int secondIndex) {
                t.add(Arrays.asList(InsnAttributes.lookupPrefix(opcode).getMnemonic(),
                        BigInteger.valueOf(firstIndex),
                        BigInteger.valueOf(secondIndex)));
            }

            @Override
            public void visitMemInsn(byte opcode, int align, int offset) {
                List<Object> insnList = new ArrayList<>();
                insnList.add(InsnAttributes.lookup(opcode).getMnemonic());
                if (offset != 0) {
                    insnList.add(new Reader.MemArgPart(Reader.MemArgPart.Type.OFFSET, BigInteger.valueOf(offset)));
                }
                insnList.add(new Reader.MemArgPart(Reader.MemArgPart.Type.ALIGN, BigInteger.valueOf(align)));
                t.add(insnList);
            }

            @Override
            public void visitIndexedMemInsn(int opcode, int index) {
                t.add(Arrays.asList(InsnAttributes.lookupPrefix(opcode).getMnemonic(),
                        BigInteger.valueOf(index)));
            }

            @Override
            public void visitBlockInsn(byte opcode, BlockType blockType) {
                switch (opcode) {
                    // @formatter:off
                    case Opcodes.IF: t.add("if"); break;
                    case Opcodes.LOOP: t.add("loop"); break;
                    case Opcodes.BLOCK: t.add("block"); break;
                    // @formatter:on
                    default:
                        throw new IllegalArgumentException();
                }
                if (blockType.kind == BlockType.Kind.VALTYPE) {
                    if (blockType.type != Opcodes.EMPTY_TYPE) {
                        t.add(Arrays.asList("result", unparseType((byte) blockType.type)));
                    }
                } else {
                    t.add(Arrays.asList("type", BigInteger.valueOf(blockType.type)));
                }
            }

            @Override
            public void visitElseInsn() {
                t.add("else");
            }

            @Override
            public void visitEndInsn() {
                t.add("end");
            }

            @Override
            public void visitBreakInsn(byte opcode, int label) {
                t.add(Arrays.asList("br", BigInteger.valueOf(label)));
            }

            @Override
            public void visitTableBreakInsn(int[] labels, int defaultLabel) {
                List<Object> l = new ArrayList<>();
                l.add("br_table");
                for (int label : labels) {
                    l.add(BigInteger.valueOf(label));
                }
                l.add(BigInteger.valueOf(defaultLabel));
                t.add(l);
            }

            @Override
            public void visitCallInsn(int function) {
                t.add(Arrays.asList("call", BigInteger.valueOf(function)));
            }

            @Override
            public void visitCallIndirectInsn(int table, int type) {
                t.add(Arrays.asList("call_indirect", BigInteger.valueOf(table), BigInteger.valueOf(type)));
            }
        };

        for (Iterator<AbstractInsnNode> iter = expr.instructions.iterator(); iter.hasNext(); ) {
            AbstractInsnNode insn = iter.next();
            if (insn instanceof EndInsnNode && !iter.hasNext()) {
                return; // omit last 'end'
            }

            int prevSize = t.size();
            insn.accept(ev);
            if (prevSize == t.size()) {
                throw new IllegalStateException();
            }
        }
    }

    private static List<?> unparseLimits(Limits limits) {
        return limits.max == null
                ? Collections.singletonList(limits.min)
                : Arrays.asList(limits.min, limits.max);
    }

    public static String unparseType(byte type) {
        switch (type) {
            case Opcodes.I32:
                return "i32";
            case Opcodes.I64:
                return "i64";
            case Opcodes.F32:
                return "f32";
            case Opcodes.F64:
                return "f64";
            case Opcodes.V128:
                return "v128";
            case Opcodes.FUNCREF:
                return "funcref";
            case Opcodes.EXTERNREF:
                return "externref";
            default:
                throw new IllegalArgumentException(String.format("type 0x%02x", type));
        }
    }

    private static void unparseTypes(List<Object> target, byte[] types) {
        for (byte type : types) {
            target.add(unparseType(type));
        }
    }
}
