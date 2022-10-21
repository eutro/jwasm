package io.github.eutro.jwasm.attrs;

import io.github.eutro.jwasm.Opcodes;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InsnAttributes {
    public static Collection<Opcode> allOpcodes() {
        return Collections.unmodifiableCollection(OPCODE_MAP.keySet());
    }

    public static InsnAttributes lookup(Opcode opcode) {
        return OPCODE_MAP.get(opcode);
    }

    public static InsnAttributes lookup(String mnemonic) {
        return MNEMONIC_MAP.get(mnemonic);
    }

    public static InsnAttributes lookup(byte opcode) {
        return lookup(new Opcode(opcode, 0));
    }

    public static InsnAttributes lookupPrefix(int intOpcode) {
        return lookup(new Opcode(Opcodes.INSN_PREFIX, intOpcode));
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public Opcode getOpcode() {
        return opcode;
    }

    private static final Map<Opcode, InsnAttributes> OPCODE_MAP = new HashMap<>();
    private static final Map<String, InsnAttributes> MNEMONIC_MAP = new HashMap<>();

    private final Opcode opcode;
    private final String mnemonic;

    private InsnAttributes(Opcode opcode, String mnemonic) {
        this.opcode = opcode;
        this.mnemonic = mnemonic;
    }

    private static void reg(Opcode opc, String mnemonic) {
        InsnAttributes attrs = new InsnAttributes(opc, mnemonic);
        OPCODE_MAP.put(opc, attrs);
        MNEMONIC_MAP.put(mnemonic, attrs);
    }

    private static void reg(byte opc, String mnemonic) {
        reg(new Opcode(opc, 0), mnemonic);
    }

    private static void reg(int opc, String mnemonic) {
        reg(new Opcode(Opcodes.INSN_PREFIX, opc), mnemonic);
    }

    static {
        reg(Opcodes.UNREACHABLE, "unreachable");
        reg(Opcodes.NOP, "nop");
        reg(Opcodes.BLOCK, "block");
        reg(Opcodes.LOOP, "loop");
        reg(Opcodes.IF, "if");
        reg(Opcodes.ELSE, "else");
        reg(Opcodes.END, "end");
        reg(Opcodes.BR, "br");
        reg(Opcodes.BR_IF, "br_if");
        reg(Opcodes.BR_TABLE, "br_table");
        reg(Opcodes.RETURN, "return");
        reg(Opcodes.CALL, "call");
        reg(Opcodes.CALL_INDIRECT, "call_indirect");
        reg(Opcodes.REF_NULL, "ref.null");
        reg(Opcodes.REF_IS_NULL, "ref.is_null");
        reg(Opcodes.REF_FUNC, "ref.func");
        reg(Opcodes.DROP, "drop");
        reg(Opcodes.SELECT, "select");
        reg(Opcodes.SELECTT, "selectt");
        reg(Opcodes.LOCAL_GET, "local.get");
        reg(Opcodes.LOCAL_SET, "local.set");
        reg(Opcodes.LOCAL_TEE, "local.tee");
        reg(Opcodes.GLOBAL_GET, "global.get");
        reg(Opcodes.GLOBAL_SET, "global.set");
        reg(Opcodes.TABLE_GET, "table.get");
        reg(Opcodes.TABLE_SET, "table.set");
        reg(Opcodes.TABLE_INIT, "table.init");
        reg(Opcodes.ELEM_DROP, "elem.drop");
        reg(Opcodes.TABLE_COPY, "table.copy");
        reg(Opcodes.TABLE_GROW, "table.grow");
        reg(Opcodes.TABLE_SIZE, "table.size");
        reg(Opcodes.TABLE_FILL, "table.fill");
        reg(Opcodes.I32_LOAD, "i32.load");
        reg(Opcodes.I64_LOAD, "i64.load");
        reg(Opcodes.F32_LOAD, "f32.load");
        reg(Opcodes.F64_LOAD, "f64.load");
        reg(Opcodes.I32_LOAD8_S, "i32.load8_s");
        reg(Opcodes.I32_LOAD8_U, "i32.load8_u");
        reg(Opcodes.I32_LOAD16_S, "i32.load16_s");
        reg(Opcodes.I32_LOAD16_U, "i32.load16_u");
        reg(Opcodes.I64_LOAD8_S, "i64.load8_s");
        reg(Opcodes.I64_LOAD8_U, "i64.load8_u");
        reg(Opcodes.I64_LOAD16_S, "i64.load16_s");
        reg(Opcodes.I64_LOAD16_U, "i64.load16_u");
        reg(Opcodes.I64_LOAD32_S, "i64.load32_s");
        reg(Opcodes.I64_LOAD32_U, "i64.load32_u");
        reg(Opcodes.I32_STORE, "i32.store");
        reg(Opcodes.I64_STORE, "i64.store");
        reg(Opcodes.F32_STORE, "f32.store");
        reg(Opcodes.F64_STORE, "f64.store");
        reg(Opcodes.I32_STORE8, "i32.store8");
        reg(Opcodes.I32_STORE16, "i32.store16");
        reg(Opcodes.I64_STORE8, "i64.store8");
        reg(Opcodes.I64_STORE16, "i64.store16");
        reg(Opcodes.I64_STORE32, "i64.store32");
        reg(Opcodes.MEMORY_SIZE, "memory.size");
        reg(Opcodes.MEMORY_GROW, "memory.grow");
        reg(Opcodes.MEMORY_INIT, "memory.init");
        reg(Opcodes.DATA_DROP, "data.drop");
        reg(Opcodes.MEMORY_COPY, "memory.copy");
        reg(Opcodes.MEMORY_FILL, "memory.fill");
        reg(Opcodes.I32_CONST, "i32.const");
        reg(Opcodes.I64_CONST, "i64.const");
        reg(Opcodes.F32_CONST, "f32.const");
        reg(Opcodes.F64_CONST, "f64.const");
        reg(Opcodes.I32_EQZ, "i32.eqz");
        reg(Opcodes.I32_EQ, "i32.eq");
        reg(Opcodes.I32_NE, "i32.ne");
        reg(Opcodes.I32_LT_S, "i32.lt_s");
        reg(Opcodes.I32_LT_U, "i32.lt_u");
        reg(Opcodes.I32_GT_S, "i32.gt_s");
        reg(Opcodes.I32_GT_U, "i32.gt_u");
        reg(Opcodes.I32_LE_S, "i32.le_s");
        reg(Opcodes.I32_LE_U, "i32.le_u");
        reg(Opcodes.I32_GE_S, "i32.ge_s");
        reg(Opcodes.I32_GE_U, "i32.ge_u");
        reg(Opcodes.I64_EQZ, "i64.eqz");
        reg(Opcodes.I64_EQ, "i64.eq");
        reg(Opcodes.I64_NE, "i64.ne");
        reg(Opcodes.I64_LT_S, "i64.lt_s");
        reg(Opcodes.I64_LT_U, "i64.lt_u");
        reg(Opcodes.I64_GT_S, "i64.gt_s");
        reg(Opcodes.I64_GT_U, "i64.gt_u");
        reg(Opcodes.I64_LE_S, "i64.le_s");
        reg(Opcodes.I64_LE_U, "i64.le_u");
        reg(Opcodes.I64_GE_S, "i64.ge_s");
        reg(Opcodes.I64_GE_U, "i64.ge_u");
        reg(Opcodes.F32_EQ, "f32.eq");
        reg(Opcodes.F32_NE, "f32.ne");
        reg(Opcodes.F32_LT, "f32.lt");
        reg(Opcodes.F32_GT, "f32.gt");
        reg(Opcodes.F32_LE, "f32.le");
        reg(Opcodes.F32_GE, "f32.ge");
        reg(Opcodes.F64_EQ, "f64.eq");
        reg(Opcodes.F64_NE, "f64.ne");
        reg(Opcodes.F64_LT, "f64.lt");
        reg(Opcodes.F64_GT, "f64.gt");
        reg(Opcodes.F64_LE, "f64.le");
        reg(Opcodes.F64_GE, "f64.ge");
        reg(Opcodes.I32_CLZ, "i32.clz");
        reg(Opcodes.I32_CTZ, "i32.ctz");
        reg(Opcodes.I32_POPCNT, "i32.popcnt");
        reg(Opcodes.I32_ADD, "i32.add");
        reg(Opcodes.I32_SUB, "i32.sub");
        reg(Opcodes.I32_MUL, "i32.mul");
        reg(Opcodes.I32_DIV_S, "i32.div_s");
        reg(Opcodes.I32_DIV_U, "i32.div_u");
        reg(Opcodes.I32_REM_S, "i32.rem_s");
        reg(Opcodes.I32_REM_U, "i32.rem_u");
        reg(Opcodes.I32_AND, "i32.and");
        reg(Opcodes.I32_OR, "i32.or");
        reg(Opcodes.I32_XOR, "i32.xor");
        reg(Opcodes.I32_SHL, "i32.shl");
        reg(Opcodes.I32_SHR_S, "i32.shr_s");
        reg(Opcodes.I32_SHR_U, "i32.shr_u");
        reg(Opcodes.I32_ROTL, "i32.rotl");
        reg(Opcodes.I32_ROTR, "i32.rotr");
        reg(Opcodes.I64_CLZ, "i64.clz");
        reg(Opcodes.I64_CTZ, "i64.ctz");
        reg(Opcodes.I64_POPCNT, "i64.popcnt");
        reg(Opcodes.I64_ADD, "i64.add");
        reg(Opcodes.I64_SUB, "i64.sub");
        reg(Opcodes.I64_MUL, "i64.mul");
        reg(Opcodes.I64_DIV_S, "i64.div_s");
        reg(Opcodes.I64_DIV_U, "i64.div_u");
        reg(Opcodes.I64_REM_S, "i64.rem_s");
        reg(Opcodes.I64_REM_U, "i64.rem_u");
        reg(Opcodes.I64_AND, "i64.and");
        reg(Opcodes.I64_OR, "i64.or");
        reg(Opcodes.I64_XOR, "i64.xor");
        reg(Opcodes.I64_SHL, "i64.shl");
        reg(Opcodes.I64_SHR_S, "i64.shr_s");
        reg(Opcodes.I64_SHR_U, "i64.shr_u");
        reg(Opcodes.I64_ROTL, "i64.rotl");
        reg(Opcodes.I64_ROTR, "i64.rotr");
        reg(Opcodes.F32_ABS, "f32.abs");
        reg(Opcodes.F32_NEG, "f32.neg");
        reg(Opcodes.F32_CEIL, "f32.ceil");
        reg(Opcodes.F32_FLOOR, "f32.floor");
        reg(Opcodes.F32_TRUNC, "f32.trunc");
        reg(Opcodes.F32_NEAREST, "f32.nearest");
        reg(Opcodes.F32_SQRT, "f32.sqrt");
        reg(Opcodes.F32_ADD, "f32.add");
        reg(Opcodes.F32_SUB, "f32.sub");
        reg(Opcodes.F32_MUL, "f32.mul");
        reg(Opcodes.F32_DIV, "f32.div");
        reg(Opcodes.F32_MIN, "f32.min");
        reg(Opcodes.F32_MAX, "f32.max");
        reg(Opcodes.F32_COPYSIGN, "f32.copysign");
        reg(Opcodes.F64_ABS, "f64.abs");
        reg(Opcodes.F64_NEG, "f64.neg");
        reg(Opcodes.F64_CEIL, "f64.ceil");
        reg(Opcodes.F64_FLOOR, "f64.floor");
        reg(Opcodes.F64_TRUNC, "f64.trunc");
        reg(Opcodes.F64_NEAREST, "f64.nearest");
        reg(Opcodes.F64_SQRT, "f64.sqrt");
        reg(Opcodes.F64_ADD, "f64.add");
        reg(Opcodes.F64_SUB, "f64.sub");
        reg(Opcodes.F64_MUL, "f64.mul");
        reg(Opcodes.F64_DIV, "f64.div");
        reg(Opcodes.F64_MIN, "f64.min");
        reg(Opcodes.F64_MAX, "f64.max");
        reg(Opcodes.F64_COPYSIGN, "f64.copysign");
        reg(Opcodes.I32_WRAP_I64, "i32.wrap_i64");
        reg(Opcodes.I32_TRUNC_F32_S, "i32.trunc_f32_s");
        reg(Opcodes.I32_TRUNC_F32_U, "i32.trunc_f32_u");
        reg(Opcodes.I32_TRUNC_F64_S, "i32.trunc_f64_s");
        reg(Opcodes.I32_TRUNC_F64_U, "i32.trunc_f64_u");
        reg(Opcodes.I64_EXTEND_I32_S, "i64.extend_i32_s");
        reg(Opcodes.I64_EXTEND_I32_U, "i64.extend_i32_u");
        reg(Opcodes.I64_TRUNC_F32_S, "i64.trunc_f32_s");
        reg(Opcodes.I64_TRUNC_F32_U, "i64.trunc_f32_u");
        reg(Opcodes.I64_TRUNC_F64_S, "i64.trunc_f64_s");
        reg(Opcodes.I64_TRUNC_F64_U, "i64.trunc_f64_u");
        reg(Opcodes.F32_CONVERT_I32_S, "f32.convert_i32_s");
        reg(Opcodes.F32_CONVERT_I32_U, "f32.convert_i32_u");
        reg(Opcodes.F32_CONVERT_I64_S, "f32.convert_i64_s");
        reg(Opcodes.F32_CONVERT_I64_U, "f32.convert_i64_u");
        reg(Opcodes.F32_DEMOTE_F64, "f32.demote_f64");
        reg(Opcodes.F64_CONVERT_I32_S, "f64.convert_i32_s");
        reg(Opcodes.F64_CONVERT_I32_U, "f64.convert_i32_u");
        reg(Opcodes.F64_CONVERT_I64_S, "f64.convert_i64_s");
        reg(Opcodes.F64_CONVERT_I64_U, "f64.convert_i64_u");
        reg(Opcodes.F64_PROMOTE_F32, "f64.promote_f32");
        reg(Opcodes.I32_REINTERPRET_F32, "i32.reinterpret_f32");
        reg(Opcodes.I64_REINTERPRET_F64, "i64.reinterpret_f64");
        reg(Opcodes.F32_REINTERPRET_I32, "f32.reinterpret_i32");
        reg(Opcodes.F64_REINTERPRET_I64, "f64.reinterpret_i64");
        reg(Opcodes.I32_EXTEND8_S, "i32.extend8_s");
        reg(Opcodes.I32_EXTEND16_S, "i32.extend16_s");
        reg(Opcodes.I64_EXTEND8_S, "i64.extend8_s");
        reg(Opcodes.I64_EXTEND16_S, "i64.extend16_s");
        reg(Opcodes.I64_EXTEND32_S, "i64.extend32_s");
        reg(Opcodes.I32_TRUNC_SAT_F32_S, "i32.trunc_sat_f32_s");
        reg(Opcodes.I32_TRUNC_SAT_F32_U, "i32.trunc_sat_f32_u");
        reg(Opcodes.I32_TRUNC_SAT_F64_S, "i32.trunc_sat_f64_s");
        reg(Opcodes.I32_TRUNC_SAT_F64_U, "i32.trunc_sat_f64_u");
        reg(Opcodes.I64_TRUNC_SAT_F32_S, "i64.trunc_sat_f32_s");
        reg(Opcodes.I64_TRUNC_SAT_F32_U, "i64.trunc_sat_f32_u");
        reg(Opcodes.I64_TRUNC_SAT_F64_S, "i64.trunc_sat_f64_s");
        reg(Opcodes.I64_TRUNC_SAT_F64_U, "i64.trunc_sat_f64_u");
    }
}
