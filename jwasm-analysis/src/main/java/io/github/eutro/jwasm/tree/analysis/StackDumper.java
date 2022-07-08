package io.github.eutro.jwasm.tree.analysis;

import io.github.eutro.jwasm.BlockType;
import io.github.eutro.jwasm.ExprVisitor;
import org.intellij.lang.annotations.PrintFormat;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import static io.github.eutro.jwasm.Opcodes.*;

/**
 * A {@link ModuleValidator} which also prints the stack shape of every expression in the module.
 */
public class StackDumper extends ModuleValidator {
    private final PrintStream ps;

    /**
     * Construct a {@link StackDumper} which prints to the given {@link PrintStream}.
     *
     * @param ps The {@link PrintStream}.
     */
    public StackDumper(PrintStream ps) {
        this.ps = ps;
    }

    @Override
    protected ExprVisitor wrapExprVisitor(ExprValidator ev) {
        return new ExprVisitor(ev) {
            {
                ExprValidator.CtrlFrame frame = ev.ctrlsRef(0);
                ps.printf("Expr -> %s {\n", typeArray(frame.endTypes));
                ps.printf("Locals: %s\n", typeArray(new ByteList(ev.locals)));
            }

            private void logStack() {
                ps.printf("          |                                     * %s\n", typeArray(ev.vals));
            }

            private int indent = 0;

            private void insn(byte opcode, @Nullable Integer intOpcode, @PrintFormat String fmt, Object... args) {
                ps.printf("0x%02x ", opcode);
                if (intOpcode == null) {
                    ps.print("    ");
                } else {
                    ps.printf("% 4d", opcode);
                }
                ps.print(" | ");
                for (int size = indent; size > 0; size--) {
                    ps.print("  ");
                }
                ps.printf(fmt, args);
                ps.println();
                logStack();
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
                ps.println("}");
            }

            @Override
            public void visitInsn(byte opcode) {
                super.visitInsn(opcode);
                switch (opcode) {
                    case UNREACHABLE:
                        insn(opcode, null, "unreachable");
                        break;
                    case NOP:
                        insn(opcode, null, "nop");
                        break;
                    case RETURN:
                        insn(opcode, null, "return");
                        break;
                    case REF_IS_NULL:
                        insn(opcode, null, "ref.is_null");
                        break;
                    case DROP:
                        insn(opcode, null, "drop");
                        break;
                    case SELECT:
                        insn(opcode, null, "select");
                        break;
                    case I32_EQZ:
                        insn(opcode, null, "i32.eqz");
                        break;
                    case I32_EQ:
                        insn(opcode, null, "i32.eq");
                        break;
                    case I32_NE:
                        insn(opcode, null, "i32.ne");
                        break;
                    case I32_LT_S:
                        insn(opcode, null, "i32.lt_s");
                        break;
                    case I32_LT_U:
                        insn(opcode, null, "i32.lt_u");
                        break;
                    case I32_GT_S:
                        insn(opcode, null, "i32.gt_s");
                        break;
                    case I32_GT_U:
                        insn(opcode, null, "i32.gt_u");
                        break;
                    case I32_LE_S:
                        insn(opcode, null, "i32.le_s");
                        break;
                    case I32_LE_U:
                        insn(opcode, null, "i32.le_u");
                        break;
                    case I32_GE_S:
                        insn(opcode, null, "i32.ge_s");
                        break;
                    case I32_GE_U:
                        insn(opcode, null, "i32.ge_u");
                        break;
                    case I32_ADD:
                        insn(opcode, null, "i32.add");
                        break;
                    case I32_SUB:
                        insn(opcode, null, "i32.sub");
                        break;
                    case I32_MUL:
                        insn(opcode, null, "i32.mul");
                        break;
                    case I32_DIV_S:
                        insn(opcode, null, "i32.div_s");
                        break;
                    case I32_DIV_U:
                        insn(opcode, null, "i32.div_u");
                        break;
                    case I32_REM_S:
                        insn(opcode, null, "i32.rem_s");
                        break;
                    case I32_REM_U:
                        insn(opcode, null, "i32.rem_u");
                        break;
                    case I32_AND:
                        insn(opcode, null, "i32.and");
                        break;
                    case I32_OR:
                        insn(opcode, null, "i32.or");
                        break;
                    case I32_XOR:
                        insn(opcode, null, "i32.xor");
                        break;
                    case I32_SHL:
                        insn(opcode, null, "i32.shl");
                        break;
                    case I32_SHR_S:
                        insn(opcode, null, "i32.shr_s");
                        break;
                    case I32_SHR_U:
                        insn(opcode, null, "i32.shr_u");
                        break;
                    case I32_ROTL:
                        insn(opcode, null, "i32.rotl");
                        break;
                    case I32_ROTR:
                        insn(opcode, null, "i32.rotr");
                        break;
                    case I64_EQZ:
                        insn(opcode, null, "i64.eqz");
                        break;
                    case I64_EQ:
                        insn(opcode, null, "i64.eq");
                        break;
                    case I64_NE:
                        insn(opcode, null, "i64.ne");
                        break;
                    case I64_LT_S:
                        insn(opcode, null, "i64.lt_s");
                        break;
                    case I64_LT_U:
                        insn(opcode, null, "i64.lt_u");
                        break;
                    case I64_GT_S:
                        insn(opcode, null, "i64.gt_s");
                        break;
                    case I64_GT_U:
                        insn(opcode, null, "i64.gt_u");
                        break;
                    case I64_LE_S:
                        insn(opcode, null, "i64.le_s");
                        break;
                    case I64_LE_U:
                        insn(opcode, null, "i64.le_u");
                        break;
                    case I64_GE_S:
                        insn(opcode, null, "i64.ge_s");
                        break;
                    case I64_GE_U:
                        insn(opcode, null, "i64.ge_u");
                        break;
                    case F32_EQ:
                        insn(opcode, null, "f32.eq");
                        break;
                    case F32_NE:
                        insn(opcode, null, "f32.ne");
                        break;
                    case F32_LT:
                        insn(opcode, null, "f32.lt");
                        break;
                    case F32_GT:
                        insn(opcode, null, "f32.gt");
                        break;
                    case F32_LE:
                        insn(opcode, null, "f32.le");
                        break;
                    case F32_GE:
                        insn(opcode, null, "f32.ge");
                        break;
                    case F64_EQ:
                        insn(opcode, null, "f64.eq");
                        break;
                    case F64_NE:
                        insn(opcode, null, "f64.ne");
                        break;
                    case F64_LT:
                        insn(opcode, null, "f64.lt");
                        break;
                    case F64_GT:
                        insn(opcode, null, "f64.gt");
                        break;
                    case F64_LE:
                        insn(opcode, null, "f64.le");
                        break;
                    case F64_GE:
                        insn(opcode, null, "f64.ge");
                        break;
                    case I32_CLZ:
                        insn(opcode, null, "i32.clz");
                        break;
                    case I32_CTZ:
                        insn(opcode, null, "i32.ctz");
                        break;
                    case I32_POPCNT:
                        insn(opcode, null, "i32.popcnt");
                        break;
                    case I64_CLZ:
                        insn(opcode, null, "i64.clz");
                        break;
                    case I64_CTZ:
                        insn(opcode, null, "i64.ctz");
                        break;
                    case I64_POPCNT:
                        insn(opcode, null, "i64.popcnt");
                        break;
                    case I64_ADD:
                        insn(opcode, null, "i64.add");
                        break;
                    case I64_SUB:
                        insn(opcode, null, "i64.sub");
                        break;
                    case I64_MUL:
                        insn(opcode, null, "i64.mul");
                        break;
                    case I64_DIV_S:
                        insn(opcode, null, "i64.div_s");
                        break;
                    case I64_DIV_U:
                        insn(opcode, null, "i64.div_u");
                        break;
                    case I64_REM_S:
                        insn(opcode, null, "i64.rem_s");
                        break;
                    case I64_REM_U:
                        insn(opcode, null, "i64.rem_u");
                        break;
                    case I64_AND:
                        insn(opcode, null, "i64.and");
                        break;
                    case I64_OR:
                        insn(opcode, null, "i64.or");
                        break;
                    case I64_XOR:
                        insn(opcode, null, "i64.xor");
                        break;
                    case I64_SHL:
                        insn(opcode, null, "i64.shl");
                        break;
                    case I64_SHR_S:
                        insn(opcode, null, "i64.shr_s");
                        break;
                    case I64_SHR_U:
                        insn(opcode, null, "i64.shr_u");
                        break;
                    case I64_ROTL:
                        insn(opcode, null, "i64.rotl");
                        break;
                    case I64_ROTR:
                        insn(opcode, null, "i64.rotr");
                        break;
                    case F32_ABS:
                        insn(opcode, null, "f32.abs");
                        break;
                    case F32_NEG:
                        insn(opcode, null, "f32.neg");
                        break;
                    case F32_CEIL:
                        insn(opcode, null, "f32.ceil");
                        break;
                    case F32_FLOOR:
                        insn(opcode, null, "f32.floor");
                        break;
                    case F32_TRUNC:
                        insn(opcode, null, "f32.trunc");
                        break;
                    case F32_NEAREST:
                        insn(opcode, null, "f32.nearest");
                        break;
                    case F32_SQRT:
                        insn(opcode, null, "f32.sqrt");
                        break;
                    case F32_ADD:
                        insn(opcode, null, "f32.add");
                        break;
                    case F32_SUB:
                        insn(opcode, null, "f32.sub");
                        break;
                    case F32_MUL:
                        insn(opcode, null, "f32.mul");
                        break;
                    case F32_DIV:
                        insn(opcode, null, "f32.div");
                        break;
                    case F32_MIN:
                        insn(opcode, null, "f32.min");
                        break;
                    case F32_MAX:
                        insn(opcode, null, "f32.max");
                        break;
                    case F32_COPYSIGN:
                        insn(opcode, null, "f32.copysign");
                        break;
                    case F64_ABS:
                        insn(opcode, null, "f64.abs");
                        break;
                    case F64_NEG:
                        insn(opcode, null, "f64.neg");
                        break;
                    case F64_CEIL:
                        insn(opcode, null, "f64.ceil");
                        break;
                    case F64_FLOOR:
                        insn(opcode, null, "f64.floor");
                        break;
                    case F64_TRUNC:
                        insn(opcode, null, "f64.trunc");
                        break;
                    case F64_NEAREST:
                        insn(opcode, null, "f64.nearest");
                        break;
                    case F64_SQRT:
                        insn(opcode, null, "f64.sqrt");
                        break;
                    case F64_ADD:
                        insn(opcode, null, "f64.add");
                        break;
                    case F64_SUB:
                        insn(opcode, null, "f64.sub");
                        break;
                    case F64_MUL:
                        insn(opcode, null, "f64.mul");
                        break;
                    case F64_DIV:
                        insn(opcode, null, "f64.div");
                        break;
                    case F64_MIN:
                        insn(opcode, null, "f64.min");
                        break;
                    case F64_MAX:
                        insn(opcode, null, "f64.max");
                        break;
                    case F64_COPYSIGN:
                        insn(opcode, null, "f64.copysign");
                        break;
                    case I32_WRAP_I64:
                        insn(opcode, null, "i32.wrap_i64");
                        break;
                    case I32_TRUNC_F32_S:
                        insn(opcode, null, "i32.trunc_f32_s");
                        break;
                    case I32_TRUNC_F32_U:
                        insn(opcode, null, "i32.trunc_f32_u");
                        break;
                    case I32_REINTERPRET_F32:
                        insn(opcode, null, "i32.reinterpret_f32");
                        break;
                    case I32_TRUNC_F64_S:
                        insn(opcode, null, "i32.trunc_f64_s");
                        break;
                    case I32_TRUNC_F64_U:
                        insn(opcode, null, "i32.trunc_f64_u");
                        break;
                    case I64_EXTEND_I32_S:
                        insn(opcode, null, "i64.extend_i32_s");
                        break;
                    case I64_EXTEND_I32_U:
                        insn(opcode, null, "i64.extend_i32_u");
                        break;
                    case I64_TRUNC_F32_S:
                        insn(opcode, null, "i64.trunc_f32_s");
                        break;
                    case I64_TRUNC_F32_U:
                        insn(opcode, null, "i64.trunc_f32_u");
                        break;
                    case I64_TRUNC_F64_S:
                        insn(opcode, null, "i64.trunc_f64_s");
                        break;
                    case I64_TRUNC_F64_U:
                        insn(opcode, null, "i64.trunc_f64_u");
                        break;
                    case I64_REINTERPRET_F64:
                        insn(opcode, null, "i64.reinterpret_f64");
                        break;
                    case F32_CONVERT_I32_S:
                        insn(opcode, null, "f32.convert_i32_s");
                        break;
                    case F32_CONVERT_I32_U:
                        insn(opcode, null, "f32.convert_i32_u");
                        break;
                    case F32_REINTERPRET_I32:
                        insn(opcode, null, "f32.reinterpret_i32");
                        break;
                    case F32_CONVERT_I64_S:
                        insn(opcode, null, "f32.convert_i64_s");
                        break;
                    case F32_CONVERT_I64_U:
                        insn(opcode, null, "f32.convert_i64_u");
                        break;
                    case F32_DEMOTE_F64:
                        insn(opcode, null, "f32.demote_f64");
                        break;
                    case F64_CONVERT_I32_S:
                        insn(opcode, null, "f64.convert_i32_s");
                        break;
                    case F64_CONVERT_I32_U:
                        insn(opcode, null, "f64.convert_i32_u");
                        break;
                    case F64_CONVERT_I64_S:
                        insn(opcode, null, "f64.convert_i64_s");
                        break;
                    case F64_CONVERT_I64_U:
                        insn(opcode, null, "f64.convert_i64_u");
                        break;
                    case F64_REINTERPRET_I64:
                        insn(opcode, null, "f64.reinterpret_i64");
                        break;
                    case F64_PROMOTE_F32:
                        insn(opcode, null, "f64.promote_f32");
                        break;
                    case I32_EXTEND8_S:
                        insn(opcode, null, "i32.extend8_s");
                        break;
                    case I32_EXTEND16_S:
                        insn(opcode, null, "i32.extend16_s");
                        break;
                    case I64_EXTEND8_S:
                        insn(opcode, null, "i64.extend8_s");
                        break;
                    case I64_EXTEND16_S:
                        insn(opcode, null, "i64.extend16_s");
                        break;
                    case I64_EXTEND32_S:
                        insn(opcode, null, "i64.extend32_s");
                        break;
                    case MEMORY_SIZE:
                        insn(opcode, null, "memory.size");
                        break;
                    case MEMORY_GROW:
                        insn(opcode, null, "memory.grow");
                        break;
                    default:
                        insn(opcode, null, "???");
                        break;
                }
            }

            @Override
            public void visitPrefixInsn(int opcode) {
                super.visitPrefixInsn(opcode);
                switch (opcode) {
                    case I32_TRUNC_SAT_F32_S:
                        insn(INSN_PREFIX, opcode, "i32.trunc_sat_f32_s");
                        break;
                    case I32_TRUNC_SAT_F32_U:
                        insn(INSN_PREFIX, opcode, "i32.trunc_sat_f32_u");
                        break;
                    case I32_TRUNC_SAT_F64_S:
                        insn(INSN_PREFIX, opcode, "i32.trunc_sat_f64_s");
                        break;
                    case I32_TRUNC_SAT_F64_U:
                        insn(INSN_PREFIX, opcode, "i32.trunc_sat_f64_u");
                        break;
                    case I64_TRUNC_SAT_F32_S:
                        insn(INSN_PREFIX, opcode, "i64.trunc_sat_f32_s");
                        break;
                    case I64_TRUNC_SAT_F32_U:
                        insn(INSN_PREFIX, opcode, "i64.trunc_sat_f32_u");
                        break;
                    case I64_TRUNC_SAT_F64_S:
                        insn(INSN_PREFIX, opcode, "i64.trunc_sat_f64_s");
                        break;
                    case I64_TRUNC_SAT_F64_U:
                        insn(INSN_PREFIX, opcode, "i64.trunc_sat_f64_u");
                        break;
                    default:
                        insn(INSN_PREFIX, opcode, "???");
                        break;
                }
            }

            @Override
            public void visitConstInsn(Object v) {
                super.visitConstInsn(v);
                if (v instanceof Integer) {
                    insn(I32_CONST, null, "i32.const %d", v);
                } else if (v instanceof Long) {
                    insn(I64_CONST, null, "i64.const %d", v);
                } else if (v instanceof Float) {
                    insn(F32_CONST, null, "f32.const %f", v);
                } else if (v instanceof Double) {
                    insn(F64_CONST, null, "f64.const %f", v);
                } else {
                    throw new IllegalArgumentException();
                }
            }

            @Override
            public void visitNullInsn(byte type) {
                super.visitNullInsn(type);
                insn(REF_NULL, null, "ref.null 0x%02x", type);
            }

            @Override
            public void visitFuncRefInsn(int function) {
                super.visitFuncRefInsn(function);
                insn(REF_FUNC, null, "ref.func %d", function);
            }

            @Override
            public void visitSelectInsn(byte[] type) {
                super.visitSelectInsn(type);
                insn(SELECTT, null, "select %s", Arrays.toString(type));
            }

            @Override
            public void visitVariableInsn(byte opcode, int variable) {
                super.visitVariableInsn(opcode, variable);
                switch (opcode) {
                    case LOCAL_GET:
                        insn(opcode, null, "local.get %d", variable);
                        break;
                    case LOCAL_SET:
                        insn(opcode, null, "local.set %d", variable);
                        break;
                    case LOCAL_TEE:
                        insn(opcode, null, "local.tee %d", variable);
                        break;
                    case GLOBAL_GET:
                        insn(opcode, null, "global.get %d", variable);
                        break;
                    case GLOBAL_SET:
                        insn(opcode, null, "global.set %d", variable);
                        break;
                    default:
                        insn(opcode, null, "??? %d", variable);
                        break;
                }
            }

            @Override
            public void visitTableInsn(byte opcode, int table) {
                super.visitTableInsn(opcode, table);
                switch (opcode) {
                    case TABLE_GET:
                        insn(opcode, null, "table.get %d", table);
                        break;
                    case TABLE_SET:
                        insn(opcode, null, "table.set %d", table);
                        break;
                    default:
                        insn(opcode, null, "??? %d", table);
                        break;
                }
            }

            @Override
            public void visitPrefixTableInsn(int opcode, int table) {
                super.visitPrefixTableInsn(opcode, table);
                switch (opcode) {
                    case ELEM_DROP:
                        insn(INSN_PREFIX, opcode, "elem.drop %d", table);
                        break;
                    case TABLE_SIZE:
                        insn(INSN_PREFIX, opcode, "table.size %d", table);
                        break;
                    case TABLE_GROW:
                        insn(INSN_PREFIX, opcode, "table.grow %d", table);
                        break;
                    case TABLE_FILL:
                        insn(INSN_PREFIX, opcode, "table.fill %d", table);
                        break;
                    default:
                        insn(INSN_PREFIX, opcode, "??? %d", table);
                        break;
                }
            }

            @Override
            public void visitPrefixBinaryTableInsn(int opcode, int firstIndex, int secondIndex) {
                super.visitPrefixBinaryTableInsn(opcode, firstIndex, secondIndex);
                switch (opcode) {
                    case TABLE_INIT:
                        insn(INSN_PREFIX, opcode, "table.init %d %d", firstIndex, secondIndex);
                        break;
                    case TABLE_COPY:
                        insn(INSN_PREFIX, opcode, "table.copy %d %d", firstIndex, secondIndex);
                        break;
                    default:
                        insn(INSN_PREFIX, opcode, "??? %d %d", firstIndex, secondIndex);
                        break;
                }
            }

            @Override
            public void visitMemInsn(byte opcode, int align, int offset) {
                super.visitMemInsn(opcode, align, offset);
                switch (opcode) {
                    case I32_LOAD:
                        insn(opcode, null, "i32.load %d %d", align, offset);
                        break;
                    case I32_LOAD8_S:
                        insn(opcode, null, "i32.load8_s %d %d", align, offset);
                        break;
                    case I32_LOAD8_U:
                        insn(opcode, null, "i32.load8_u %d %d", align, offset);
                        break;
                    case I32_LOAD16_S:
                        insn(opcode, null, "i32.load16_s %d %d", align, offset);
                        break;
                    case I32_LOAD16_U:
                        insn(opcode, null, "i32.load16_u %d %d", align, offset);
                        break;
                    case I64_LOAD:
                        insn(opcode, null, "i64.load %d %d", align, offset);
                        break;
                    case I64_LOAD8_S:
                        insn(opcode, null, "i64.load8_s %d %d", align, offset);
                        break;
                    case I64_LOAD8_U:
                        insn(opcode, null, "i64.load8_u %d %d", align, offset);
                        break;
                    case I64_LOAD16_S:
                        insn(opcode, null, "i64.load16_s %d %d", align, offset);
                        break;
                    case I64_LOAD16_U:
                        insn(opcode, null, "i64.load16_u %d %d", align, offset);
                        break;
                    case I64_LOAD32_S:
                        insn(opcode, null, "i64.load32_s %d %d", align, offset);
                        break;
                    case I64_LOAD32_U:
                        insn(opcode, null, "i64.load32_u %d %d", align, offset);
                        break;
                    case F32_LOAD:
                        insn(opcode, null, "f32.load %d %d", align, offset);
                        break;
                    case F64_LOAD:
                        insn(opcode, null, "f64.load %d %d", align, offset);
                        break;
                    case I32_STORE:
                        insn(opcode, null, "i32.store %d %d", align, offset);
                        break;
                    case I32_STORE8:
                        insn(opcode, null, "i32.store8 %d %d", align, offset);
                        break;
                    case I32_STORE16:
                        insn(opcode, null, "i32.store16 %d %d", align, offset);
                        break;
                    case I64_STORE:
                        insn(opcode, null, "i64.store %d %d", align, offset);
                        break;
                    case I64_STORE8:
                        insn(opcode, null, "i64.store8 %d %d", align, offset);
                        break;
                    case I64_STORE16:
                        insn(opcode, null, "i64.store16 %d %d", align, offset);
                        break;
                    case I64_STORE32:
                        insn(opcode, null, "i64.store32 %d %d", align, offset);
                        break;
                    case F32_STORE:
                        insn(opcode, null, "f32.store %d %d", align, offset);
                        break;
                    case F64_STORE:
                        insn(opcode, null, "f64.store %d %d", align, offset);
                        break;
                    default:
                        insn(opcode, null, "??? %d %d", align, offset);
                        break;
                }
            }

            @Override
            public void visitIndexedMemInsn(int opcode, int index) {
                super.visitIndexedMemInsn(opcode, index);
                switch (opcode) {
                    case DATA_DROP:
                        insn(INSN_PREFIX, opcode, "data.drop %d", index);
                        break;
                    case MEMORY_INIT:
                        insn(INSN_PREFIX, opcode, "memory.init %d", index);
                        break;
                    case MEMORY_COPY:
                        insn(INSN_PREFIX, opcode, "memory.copy %d", index);
                        break;
                    case MEMORY_FILL:
                        insn(INSN_PREFIX, opcode, "memory.fill %d", index);
                        break;
                    default:
                        insn(INSN_PREFIX, opcode, "??? %d", index);
                        break;
                }
            }

            @Override
            public void visitBlockInsn(byte opcode, BlockType blockType) {
                super.visitBlockInsn(opcode, blockType);
                ExprValidator.CtrlFrame frame = ev.ctrlsRef(0);
                switch (opcode) {
                    case BLOCK:
                        insn(opcode, null, "block %s -> %s", typeArray(frame.startTypes), typeArray(frame.endTypes));
                        break;
                    case LOOP:
                        insn(opcode, null, "loop %s -> %s", typeArray(frame.startTypes), typeArray(frame.endTypes));
                        break;
                    case IF:
                        insn(opcode, null, "if %s -> %s", typeArray(frame.startTypes), typeArray(frame.endTypes));
                        break;
                    default:
                        insn(opcode, null, "???");
                        break;
                }
                indent++;
            }

            @Override
            public void visitElseInsn() {
                super.visitElseInsn();
                indent--;
                insn(ELSE, null, "else");
                indent++;
            }

            @Override
            public void visitEndInsn() {
                super.visitEndInsn();
                indent--;
                insn(ELSE, null, "end");
            }

            @Override
            public void visitBreakInsn(byte opcode, int label) {
                super.visitBreakInsn(opcode, label);
                switch (opcode) {
                    case BR:
                        insn(opcode, null, "br %d", label);
                        break;
                    case BR_IF:
                        insn(opcode, null, "br_if %d", label);
                        break;
                    default:
                        insn(opcode, null, "??? %d", label);
                        break;
                }
            }

            @Override
            public void visitTableBreakInsn(int[] labels, int defaultLabel) {
                super.visitTableBreakInsn(labels, defaultLabel);
                insn(BR_TABLE, null, "br_table %s %d", Arrays.toString(labels), defaultLabel);
            }

            @Override
            public void visitCallInsn(int function) {
                super.visitCallInsn(function);
                insn(CALL, null, "call %d", function);
            }

            @Override
            public void visitCallIndirectInsn(int table, int type) {
                super.visitCallIndirectInsn(table, type);
                insn(CALL_INDIRECT, null, "call_indirect %d %d", table, type);
            }
        };
    }

    private String typeArray(List<Byte> stack) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (Byte type : stack) {
            sb.append(' ').append(typeName(type));
        }
        sb.append(" ]");
        return sb.toString();
    }

    private String typeName(Byte type) {
        if (type == null) return "nil";
        switch (type) {
            case I32:
                return "i32";
            case I64:
                return "i64";
            case F32:
                return "f32";
            case F64:
                return "f64";
            case FUNCREF:
                return "fnc";
            case EXTERNREF:
                return "ext";
            default:
                return "???";
        }
    }
}
