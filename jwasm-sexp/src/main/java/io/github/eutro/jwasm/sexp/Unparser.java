package io.github.eutro.jwasm.sexp;

import io.github.eutro.jwasm.BlockType;
import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Limits;
import io.github.eutro.jwasm.Opcodes;
import io.github.eutro.jwasm.tree.*;

import java.math.BigInteger;
import java.util.*;

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

                List<Object> params = new ArrayList<>();
                params.add("param");
                funcList.add(params);
                unparseTypes(params, type.params);

                List<Object> results = new ArrayList<>();
                results.add("result");
                funcList.add(results);
                unparseTypes(results, type.returns);

                typeList.add(funcList);
                moduleList.add(typeList);
            }
        }

        if (node.imports != null) {
            throw new UnsupportedOperationException();
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
            throw new UnsupportedOperationException();
        }

        if (node.exports != null) {
            throw new UnsupportedOperationException();
        }

        if (node.start != null) {
            moduleList.add(Arrays.asList("start", BigInteger.valueOf(node.start)));
        }

        if (node.elems != null) {
            throw new UnsupportedOperationException();
        }

        if (node.codes != null) {
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
            throw new UnsupportedOperationException();
        }

        return moduleList;
    }

    private static void unparseExpr(List<Object> t, ExprNode expr) {
        if (expr.instructions == null) return;
        ExprVisitor ev = new ExprVisitor() {
            @Override
            public void visitInsn(byte opcode) {
                super.visitInsn(opcode);
            }

            @Override
            public void visitPrefixInsn(int opcode) {
                super.visitPrefixInsn(opcode);
            }

            @Override
            public void visitConstInsn(Object v) {
                if (v instanceof Integer) t.add(Arrays.asList("i32.const", BigInteger.valueOf((int) v)));
                else if (v instanceof Long) t.add(Arrays.asList("i64.const", BigInteger.valueOf((long) v)));
                else if (v instanceof Float) t.add(Arrays.asList("f32.const", (double) v));
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
                super.visitSelectInsn(type);
            }

            @Override
            public void visitVariableInsn(byte opcode, int variable) {
                super.visitVariableInsn(opcode, variable);
            }

            @Override
            public void visitTableInsn(byte opcode, int table) {
                super.visitTableInsn(opcode, table);
            }

            @Override
            public void visitPrefixTableInsn(int opcode, int table) {
                super.visitPrefixTableInsn(opcode, table);
            }

            @Override
            public void visitPrefixBinaryTableInsn(int opcode, int firstIndex, int secondIndex) {
                super.visitPrefixBinaryTableInsn(opcode, firstIndex, secondIndex);
            }

            @Override
            public void visitMemInsn(byte opcode, int align, int offset) {
                super.visitMemInsn(opcode, align, offset);
            }

            @Override
            public void visitIndexedMemInsn(int opcode, int index) {
                super.visitIndexedMemInsn(opcode, index);
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
                t.add(Arrays.asList("???", insn));
            }
        }
    }

    private static List<?> unparseLimits(Limits limits) {
        return limits.max == null
                ? Collections.singletonList(limits.min)
                : Arrays.asList(limits.min, limits.max);
    }

    private static String unparseType(byte type) {
        switch (type) {
            case Opcodes.I32:
                return "i32";
            case Opcodes.I64:
                return "i64";
            case Opcodes.F32:
                return "f32";
            case Opcodes.F64:
                return "f64";
            case Opcodes.FUNCREF:
                return "funcref";
            case Opcodes.EXTERNREF:
                return "externref";
            default:
                throw new IllegalArgumentException();
        }
    }

    private static void unparseTypes(List<Object> target, byte[] types) {
        for (byte type : types) {
            target.add(unparseType(type));
        }
    }
}
