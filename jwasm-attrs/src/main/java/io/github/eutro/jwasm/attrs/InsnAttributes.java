package io.github.eutro.jwasm.attrs;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static io.github.eutro.jwasm.Opcodes.*;
import static io.github.eutro.jwasm.attrs.StackType.*;
import static io.github.eutro.jwasm.attrs.VectorShape.*;
import static io.github.eutro.jwasm.attrs.VisitTarget.*;

/**
 * A class for querying the attributes of WebAssembly instructions.
 */
public class InsnAttributes {
    /**
     * Get all the opcodes that JWasm currently recognises.
     *
     * @return The collection of opcodes.
     */
    public static Collection<Opcode> allOpcodes() {
        return Collections.unmodifiableCollection(OPCODE_MAP.keySet());
    }

    /**
     * Look up the attributes of an instruction.
     *
     * @param opcode The opcode to look up.
     * @return The attributes.
     */
    public static InsnAttributes lookup(Opcode opcode) {
        return OPCODE_MAP.get(opcode);
    }

    /**
     * Look up the attributes of an instruction by mnemonic.
     *
     * @param mnemonic The mnemonic of the instruction.
     * @return The attributes.
     */
    public static InsnAttributes lookup(String mnemonic) {
        return MNEMONIC_MAP.get(mnemonic);
    }

    /**
     * Look up the attributes of a single-byte-opcode instruction.
     *
     * @param opcode The opcode to look up.
     * @return The attributes.
     */
    public static InsnAttributes lookup(byte opcode) {
        return lookup(new Opcode(opcode, 0));
    }

    /**
     * Look up the attributes of a {@link Opcodes#INSN_PREFIX}-prefixed instruction.
     *
     * @param intOpcode The integer opcode of the instruction to look up.
     * @return The attributes.
     */
    public static InsnAttributes lookupPrefix(int intOpcode) {
        return lookup(new Opcode(INSN_PREFIX, intOpcode));
    }

    /**
     * Look up the attributes of a {@link Opcodes#VECTOR_PREFIX}-prefixed instruction.
     *
     * @param intOpcode The integer opcode of the instruction to look up.
     * @return The attributes.
     */
    public static InsnAttributes lookupVector(int intOpcode) {
        return lookup(new Opcode(VECTOR_PREFIX, intOpcode));
    }

    /**
     * Get the mnemonic of the instruction.
     *
     * @return The mnemonic.
     */
    public String getMnemonic() {
        return mnemonic;
    }

    /**
     * Get the opcode of the instruction.
     *
     * @return The opcode.
     */
    public Opcode getOpcode() {
        return opcode;
    }

    /**
     * Get the method of {@link ExprVisitor} that this instruction visits.
     *
     * @return The visit target.
     */
    public VisitTarget getVisitTarget() {
        return visitTarget;
    }

    /**
     * Get the type of the instruction, if it is simple.
     *
     * @return The stack type.
     */
    public @Nullable StackType getType() {
        return type;
    }

    /**
     * Get the shape of the vector this instruction operates on, if any.
     *
     * @return The vector shape.
     */
    public VectorShape getVectorShape() {
        return vectorShape;
    }

    /**
     * Get the number of bits a memory instruction reads or writes.
     *
     * @return The memory bits.
     */
    public int getMemBits() {
        return memBits;
    }

    private InsnAttributes setMemBits(int memLoadBits) {
        this.memBits = memLoadBits;
        return this;
    }

    private static final Map<Opcode, InsnAttributes> OPCODE_MAP = new HashMap<>();
    private static final Map<String, InsnAttributes> MNEMONIC_MAP = new HashMap<>();

    private final Opcode opcode;
    private final String mnemonic;
    private final VisitTarget visitTarget;
    private final @Nullable StackType type;
    private final VectorShape vectorShape;
    private int memBits = -1;

    private static final HashMap<StackType, StackType> TYPE_INTERNS = new HashMap<>();

    private InsnAttributes(Opcode opcode, String mnemonic, VisitTarget target, @Nullable StackType type, VectorShape vectorShape) {
        this.opcode = opcode;
        this.mnemonic = mnemonic;
        this.visitTarget = target;
        this.type = TYPE_INTERNS.computeIfAbsent(type, $ -> $);
        this.vectorShape = vectorShape;
        OPCODE_MAP.put(opcode, this);
        MNEMONIC_MAP.put(mnemonic, this);
    }

    private static InsnAttributes reg(Opcode opc, String mnemonic, VisitTarget target, @Nullable StackType type) {
        return new InsnAttributes(opc, mnemonic, target, type, null);
    }

    private static InsnAttributes reg(byte opc, String mnemonic, VisitTarget target, @Nullable StackType type) {
        return reg(new Opcode(opc, 0), mnemonic, target, type);
    }

    private static InsnAttributes reg(int opc, String mnemonic, VisitTarget target, @Nullable StackType type) {
        return reg(new Opcode(INSN_PREFIX, opc), mnemonic, target, type);
    }

    private static InsnAttributes regV(int opc, @Nullable VectorShape shape, String mnemonic, VisitTarget target, @Nullable StackType type) {
        return new InsnAttributes(new Opcode(VECTOR_PREFIX, opc), mnemonic, target, type, shape);
    }

    static {
        reg(UNREACHABLE, "unreachable", Insn, null);
        reg(NOP, "nop", Insn, pop().and(push()));
        reg(BLOCK, "block", BlockInsn, null);
        reg(LOOP, "loop", BlockInsn, null);
        reg(IF, "if", BlockInsn, null);
        reg(ELSE, "else", ElseInsn, null);
        reg(END, "end", EndInsn, null);
        reg(BR, "br", BreakInsn, null);
        reg(BR_IF, "br_if", BreakInsn, pop(I32));
        reg(BR_TABLE, "br_table", TableBreakInsn, pop(I32));
        reg(RETURN, "return", Insn, null);
        reg(CALL, "call", CallInsn, null);
        reg(CALL_INDIRECT, "call_indirect", CallIndirectInsn, null);
        reg(REF_NULL, "ref.null", NullInsn, null);
        reg(REF_IS_NULL, "ref.is_null", Insn, null);
        reg(REF_FUNC, "ref.func", FuncRefInsn, push(FUNCREF));
        reg(DROP, "drop", Insn, null);
        reg(SELECTT, "select", Insn, null);
        reg(SELECT, "select", SelectInsn, null);
        reg(LOCAL_GET, "local.get", VariableInsn, null);
        reg(LOCAL_SET, "local.set", VariableInsn, null);
        reg(LOCAL_TEE, "local.tee", VariableInsn, null);
        reg(GLOBAL_GET, "global.get", VariableInsn, null);
        reg(GLOBAL_SET, "global.set", VariableInsn, null);
        reg(TABLE_GET, "table.get", TableInsn, null);
        reg(TABLE_SET, "table.set", TableInsn, null);
        reg(TABLE_INIT, "table.init", PrefixBinaryTableInsn, null);
        reg(ELEM_DROP, "elem.drop", PrefixTableInsn, null);
        reg(TABLE_COPY, "table.copy", PrefixBinaryTableInsn, null);
        reg(TABLE_GROW, "table.grow", PrefixTableInsn, null);
        reg(TABLE_SIZE, "table.size", PrefixTableInsn, null);
        reg(TABLE_FILL, "table.fill", PrefixTableInsn, null);
        reg(I32_LOAD, "i32.load", MemInsn, pop(I32).and(push(I32))).setMemBits(32);
        reg(I64_LOAD, "i64.load", MemInsn, pop(I32).and(push(I64))).setMemBits(64);
        reg(F32_LOAD, "f32.load", MemInsn, pop(I32).and(push(F32))).setMemBits(32);
        reg(F64_LOAD, "f64.load", MemInsn, pop(I32).and(push(F64))).setMemBits(64);
        reg(I32_LOAD8_S, "i32.load8_s", MemInsn, pop(I32).and(push(I32))).setMemBits(8);
        reg(I32_LOAD8_U, "i32.load8_u", MemInsn, pop(I32).and(push(I32))).setMemBits(8);
        reg(I32_LOAD16_S, "i32.load16_s", MemInsn, pop(I32).and(push(I32))).setMemBits(16);
        reg(I32_LOAD16_U, "i32.load16_u", MemInsn, pop(I32).and(push(I32))).setMemBits(16);
        reg(I64_LOAD8_S, "i64.load8_s", MemInsn, pop(I32).and(push(I64))).setMemBits(8);
        reg(I64_LOAD8_U, "i64.load8_u", MemInsn, pop(I32).and(push(I64))).setMemBits(8);
        reg(I64_LOAD16_S, "i64.load16_s", MemInsn, pop(I32).and(push(I64))).setMemBits(16);
        reg(I64_LOAD16_U, "i64.load16_u", MemInsn, pop(I32).and(push(I64))).setMemBits(16);
        reg(I64_LOAD32_S, "i64.load32_s", MemInsn, pop(I32).and(push(I64))).setMemBits(32);
        reg(I64_LOAD32_U, "i64.load32_u", MemInsn, pop(I32).and(push(I64))).setMemBits(32);
        reg(I32_STORE, "i32.store", MemInsn, pop(I32, I32)).setMemBits(32);
        reg(I64_STORE, "i64.store", MemInsn, pop(I32, I64)).setMemBits(64);
        reg(F32_STORE, "f32.store", MemInsn, pop(I32, F32)).setMemBits(32);
        reg(F64_STORE, "f64.store", MemInsn, pop(I32, F64)).setMemBits(64);
        reg(I32_STORE8, "i32.store8", MemInsn, pop(I32, I32)).setMemBits(8);
        reg(I32_STORE16, "i32.store16", MemInsn, pop(I32, I32)).setMemBits(16);
        reg(I64_STORE8, "i64.store8", MemInsn, pop(I32, I64)).setMemBits(8);
        reg(I64_STORE16, "i64.store16", MemInsn, pop(I32, I64)).setMemBits(16);
        reg(I64_STORE32, "i64.store32", MemInsn, pop(I32, I64)).setMemBits(32);
        reg(MEMORY_SIZE, "memory.size", Insn, push(I32));
        reg(MEMORY_GROW, "memory.grow", Insn, pop(I32).and(push(I32)));
        reg(MEMORY_INIT, "memory.init", IndexedMemInsn, pop(I32, I32, I32));
        reg(DATA_DROP, "data.drop", IndexedMemInsn, pop());
        reg(MEMORY_COPY, "memory.copy", PrefixInsn, pop(I32, I32, I32));
        reg(MEMORY_FILL, "memory.fill", PrefixInsn, pop(I32, I32, I32));
        reg(I32_CONST, "i32.const", ConstInsn, push(I32));
        reg(I64_CONST, "i64.const", ConstInsn, push(I64));
        reg(F32_CONST, "f32.const", ConstInsn, push(F32));
        reg(F64_CONST, "f64.const", ConstInsn, push(I64));
        reg(I32_EQZ, "i32.eqz", Insn, testOp(I32));
        reg(I32_EQ, "i32.eq", Insn, relOp(I32));
        reg(I32_NE, "i32.ne", Insn, relOp(I32));
        reg(I32_LT_S, "i32.lt_s", Insn, relOp(I32));
        reg(I32_LT_U, "i32.lt_u", Insn, relOp(I32));
        reg(I32_GT_S, "i32.gt_s", Insn, relOp(I32));
        reg(I32_GT_U, "i32.gt_u", Insn, relOp(I32));
        reg(I32_LE_S, "i32.le_s", Insn, relOp(I32));
        reg(I32_LE_U, "i32.le_u", Insn, relOp(I32));
        reg(I32_GE_S, "i32.ge_s", Insn, relOp(I32));
        reg(I32_GE_U, "i32.ge_u", Insn, relOp(I32));
        reg(I64_EQZ, "i64.eqz", Insn, testOp(I64));
        reg(I64_EQ, "i64.eq", Insn, relOp(I64));
        reg(I64_NE, "i64.ne", Insn, relOp(I64));
        reg(I64_LT_S, "i64.lt_s", Insn, relOp(I64));
        reg(I64_LT_U, "i64.lt_u", Insn, relOp(I64));
        reg(I64_GT_S, "i64.gt_s", Insn, relOp(I64));
        reg(I64_GT_U, "i64.gt_u", Insn, relOp(I64));
        reg(I64_LE_S, "i64.le_s", Insn, relOp(I64));
        reg(I64_LE_U, "i64.le_u", Insn, relOp(I64));
        reg(I64_GE_S, "i64.ge_s", Insn, relOp(I64));
        reg(I64_GE_U, "i64.ge_u", Insn, relOp(I64));
        reg(F32_EQ, "f32.eq", Insn, relOp(F32));
        reg(F32_NE, "f32.ne", Insn, relOp(F32));
        reg(F32_LT, "f32.lt", Insn, relOp(F32));
        reg(F32_GT, "f32.gt", Insn, relOp(F32));
        reg(F32_LE, "f32.le", Insn, relOp(F32));
        reg(F32_GE, "f32.ge", Insn, relOp(F32));
        reg(F64_EQ, "f64.eq", Insn, relOp(F64));
        reg(F64_NE, "f64.ne", Insn, relOp(F64));
        reg(F64_LT, "f64.lt", Insn, relOp(F64));
        reg(F64_GT, "f64.gt", Insn, relOp(F64));
        reg(F64_LE, "f64.le", Insn, relOp(F64));
        reg(F64_GE, "f64.ge", Insn, relOp(F64));
        reg(I32_CLZ, "i32.clz", Insn, unOp(I32));
        reg(I32_CTZ, "i32.ctz", Insn, unOp(I32));
        reg(I32_POPCNT, "i32.popcnt", Insn, unOp(I32));
        reg(I32_ADD, "i32.add", Insn, binOp(I32));
        reg(I32_SUB, "i32.sub", Insn, binOp(I32));
        reg(I32_MUL, "i32.mul", Insn, binOp(I32));
        reg(I32_DIV_S, "i32.div_s", Insn, binOp(I32));
        reg(I32_DIV_U, "i32.div_u", Insn, binOp(I32));
        reg(I32_REM_S, "i32.rem_s", Insn, binOp(I32));
        reg(I32_REM_U, "i32.rem_u", Insn, binOp(I32));
        reg(I32_AND, "i32.and", Insn, binOp(I32));
        reg(I32_OR, "i32.or", Insn, binOp(I32));
        reg(I32_XOR, "i32.xor", Insn, binOp(I32));
        reg(I32_SHL, "i32.shl", Insn, binOp(I32));
        reg(I32_SHR_S, "i32.shr_s", Insn, binOp(I32));
        reg(I32_SHR_U, "i32.shr_u", Insn, binOp(I32));
        reg(I32_ROTL, "i32.rotl", Insn, binOp(I32));
        reg(I32_ROTR, "i32.rotr", Insn, binOp(I32));
        reg(I64_CLZ, "i64.clz", Insn, unOp(I64));
        reg(I64_CTZ, "i64.ctz", Insn, unOp(I64));
        reg(I64_POPCNT, "i64.popcnt", Insn, unOp(I64));
        reg(I64_ADD, "i64.add", Insn, binOp(I64));
        reg(I64_SUB, "i64.sub", Insn, binOp(I64));
        reg(I64_MUL, "i64.mul", Insn, binOp(I64));
        reg(I64_DIV_S, "i64.div_s", Insn, binOp(I64));
        reg(I64_DIV_U, "i64.div_u", Insn, binOp(I64));
        reg(I64_REM_S, "i64.rem_s", Insn, binOp(I64));
        reg(I64_REM_U, "i64.rem_u", Insn, binOp(I64));
        reg(I64_AND, "i64.and", Insn, binOp(I64));
        reg(I64_OR, "i64.or", Insn, binOp(I64));
        reg(I64_XOR, "i64.xor", Insn, binOp(I64));
        reg(I64_SHL, "i64.shl", Insn, binOp(I64));
        reg(I64_SHR_S, "i64.shr_s", Insn, binOp(I64));
        reg(I64_SHR_U, "i64.shr_u", Insn, binOp(I64));
        reg(I64_ROTL, "i64.rotl", Insn, binOp(I64));
        reg(I64_ROTR, "i64.rotr", Insn, binOp(I64));
        reg(F32_ABS, "f32.abs", Insn, unOp(F32));
        reg(F32_NEG, "f32.neg", Insn, unOp(F32));
        reg(F32_CEIL, "f32.ceil", Insn, unOp(F32));
        reg(F32_FLOOR, "f32.floor", Insn, unOp(F32));
        reg(F32_TRUNC, "f32.trunc", Insn, unOp(F32));
        reg(F32_NEAREST, "f32.nearest", Insn, unOp(F32));
        reg(F32_SQRT, "f32.sqrt", Insn, unOp(F32));
        reg(F32_ADD, "f32.add", Insn, binOp(F32));
        reg(F32_SUB, "f32.sub", Insn, binOp(F32));
        reg(F32_MUL, "f32.mul", Insn, binOp(F32));
        reg(F32_DIV, "f32.div", Insn, binOp(F32));
        reg(F32_MIN, "f32.min", Insn, binOp(F32));
        reg(F32_MAX, "f32.max", Insn, binOp(F32));
        reg(F32_COPYSIGN, "f32.copysign", Insn, binOp(F32));
        reg(F64_ABS, "f64.abs", Insn, unOp(F64));
        reg(F64_NEG, "f64.neg", Insn, unOp(F64));
        reg(F64_CEIL, "f64.ceil", Insn, unOp(F64));
        reg(F64_FLOOR, "f64.floor", Insn, unOp(F64));
        reg(F64_TRUNC, "f64.trunc", Insn, unOp(F64));
        reg(F64_NEAREST, "f64.nearest", Insn, unOp(F64));
        reg(F64_SQRT, "f64.sqrt", Insn, unOp(F64));
        reg(F64_ADD, "f64.add", Insn, binOp(F64));
        reg(F64_SUB, "f64.sub", Insn, binOp(F64));
        reg(F64_MUL, "f64.mul", Insn, binOp(F64));
        reg(F64_DIV, "f64.div", Insn, binOp(F64));
        reg(F64_MIN, "f64.min", Insn, binOp(F64));
        reg(F64_MAX, "f64.max", Insn, binOp(F64));
        reg(F64_COPYSIGN, "f64.copysign", Insn, binOp(F64));
        reg(I32_WRAP_I64, "i32.wrap_i64", Insn, convertOp(I32, I64));
        reg(I32_TRUNC_F32_S, "i32.trunc_f32_s", Insn, convertOp(I32, F32));
        reg(I32_TRUNC_F32_U, "i32.trunc_f32_u", Insn, convertOp(I32, F32));
        reg(I32_TRUNC_F64_S, "i32.trunc_f64_s", Insn, convertOp(I32, F64));
        reg(I32_TRUNC_F64_U, "i32.trunc_f64_u", Insn, convertOp(I32, F64));
        reg(I64_EXTEND_I32_S, "i64.extend_i32_s", Insn, convertOp(I64, I32));
        reg(I64_EXTEND_I32_U, "i64.extend_i32_u", Insn, convertOp(I64, I32));
        reg(I64_TRUNC_F32_S, "i64.trunc_f32_s", Insn, convertOp(I64, F32));
        reg(I64_TRUNC_F32_U, "i64.trunc_f32_u", Insn, convertOp(I64, F32));
        reg(I64_TRUNC_F64_S, "i64.trunc_f64_s", Insn, convertOp(I64, F64));
        reg(I64_TRUNC_F64_U, "i64.trunc_f64_u", Insn, convertOp(I64, F64));
        reg(F32_CONVERT_I32_S, "f32.convert_i32_s", Insn, convertOp(F32, I32));
        reg(F32_CONVERT_I32_U, "f32.convert_i32_u", Insn, convertOp(F32, I32));
        reg(F32_CONVERT_I64_S, "f32.convert_i64_s", Insn, convertOp(F32, I64));
        reg(F32_CONVERT_I64_U, "f32.convert_i64_u", Insn, convertOp(F32, I64));
        reg(F32_DEMOTE_F64, "f32.demote_f64", Insn, convertOp(F32, F64));
        reg(F64_CONVERT_I32_S, "f64.convert_i32_s", Insn, convertOp(F64, I32));
        reg(F64_CONVERT_I32_U, "f64.convert_i32_u", Insn, convertOp(F64, I32));
        reg(F64_CONVERT_I64_S, "f64.convert_i64_s", Insn, convertOp(F64, I64));
        reg(F64_CONVERT_I64_U, "f64.convert_i64_u", Insn, convertOp(F64, I64));
        reg(F64_PROMOTE_F32, "f64.promote_f32", Insn, convertOp(F64, F32));
        reg(I32_REINTERPRET_F32, "i32.reinterpret_f32", Insn, convertOp(I32, F32));
        reg(I64_REINTERPRET_F64, "i64.reinterpret_f64", Insn, convertOp(I64, F64));
        reg(F32_REINTERPRET_I32, "f32.reinterpret_i32", Insn, convertOp(F32, I32));
        reg(F64_REINTERPRET_I64, "f64.reinterpret_i64", Insn, convertOp(F64, I64));
        reg(I32_EXTEND8_S, "i32.extend8_s", Insn, unOp(I32));
        reg(I32_EXTEND16_S, "i32.extend16_s", Insn, unOp(I32));
        reg(I64_EXTEND8_S, "i64.extend8_s", Insn, unOp(I64));
        reg(I64_EXTEND16_S, "i64.extend16_s", Insn, unOp(I64));
        reg(I64_EXTEND32_S, "i64.extend32_s", Insn, unOp(I64));
        reg(I32_TRUNC_SAT_F32_S, "i32.trunc_sat_f32_s", PrefixInsn, convertOp(I32, F32));
        reg(I32_TRUNC_SAT_F32_U, "i32.trunc_sat_f32_u", PrefixInsn, convertOp(I32, F32));
        reg(I32_TRUNC_SAT_F64_S, "i32.trunc_sat_f64_s", PrefixInsn, convertOp(I32, F64));
        reg(I32_TRUNC_SAT_F64_U, "i32.trunc_sat_f64_u", PrefixInsn, convertOp(I32, F64));
        reg(I64_TRUNC_SAT_F32_S, "i64.trunc_sat_f32_s", PrefixInsn, convertOp(I64, F32));
        reg(I64_TRUNC_SAT_F32_U, "i64.trunc_sat_f32_u", PrefixInsn, convertOp(I64, F32));
        reg(I64_TRUNC_SAT_F64_S, "i64.trunc_sat_f64_s", PrefixInsn, convertOp(I64, F64));
        reg(I64_TRUNC_SAT_F64_U, "i64.trunc_sat_f64_u", PrefixInsn, convertOp(I64, F64));

        regV(V128_LOAD, null, "v128.load", VectorMemInsn, pop(I32).and(push(V128))).setMemBits(128);
        regV(V128_LOAD8X8_S, null, "v128.load8x8_s", VectorMemInsn, pop(I32).and(push(V128))).setMemBits(8 * 8);
        regV(V128_LOAD8X8_U, null, "v128.load8x8_u", VectorMemInsn, pop(I32).and(push(V128))).setMemBits(8 * 8);
        regV(V128_LOAD16X4_S, null, "v128.load16x4_s", VectorMemInsn, pop(I32).and(push(V128))).setMemBits(16 * 4);
        regV(V128_LOAD16X4_U, null, "v128.load16x4_u", VectorMemInsn, pop(I32).and(push(V128))).setMemBits(16 * 4);
        regV(V128_LOAD32X2_S, null, "v128.load32x2_s", VectorMemInsn, pop(I32).and(push(V128))).setMemBits(32 * 2);
        regV(V128_LOAD32X2_U, null, "v128.load32x2_u", VectorMemInsn, pop(I32).and(push(V128))).setMemBits(32 * 2);
        regV(V128_LOAD8_SPLAT, null, "v128.load8_splat", VectorMemInsn, pop(I32).and(push(V128))).setMemBits(8);
        regV(V128_LOAD16_SPLAT, null, "v128.load16_splat", VectorMemInsn, pop(I32).and(push(V128))).setMemBits(16);
        regV(V128_LOAD32_SPLAT, null, "v128.load32_splat", VectorMemInsn, pop(I32).and(push(V128))).setMemBits(32);
        regV(V128_LOAD64_SPLAT, null, "v128.load64_splat", VectorMemInsn, pop(I32).and(push(V128))).setMemBits(64);
        regV(V128_LOAD32_ZERO, null, "v128.load32_zero", VectorMemInsn, pop(I32).and(push(V128))).setMemBits(32);
        regV(V128_LOAD64_ZERO, null, "v128.load64_zero", VectorMemInsn, pop(I32).and(push(V128))).setMemBits(64);
        regV(V128_STORE, null, "v128.store", VectorMemInsn, pop(I32, V128)).setMemBits(128);
        regV(V128_LOAD8_LANE, I8X16, "v128.load8_lane", VectorMemLaneInsn, pop(I32, V128).and(push(V128))).setMemBits(8);
        regV(V128_LOAD16_LANE, I16X8, "v128.load16_lane", VectorMemLaneInsn, pop(I32, V128).and(push(V128))).setMemBits(16);
        regV(V128_LOAD32_LANE, I32X4, "v128.load32_lane", VectorMemLaneInsn, pop(I32, V128).and(push(V128))).setMemBits(32);
        regV(V128_LOAD64_LANE, I64X2, "v128.load64_lane", VectorMemLaneInsn, pop(I32, V128).and(push(V128))).setMemBits(64);
        regV(V128_STORE8_LANE, I8X16, "v128.store8_lane", VectorMemLaneInsn, pop(I32, V128)).setMemBits(8);
        regV(V128_STORE16_LANE, I16X8, "v128.store16_lane", VectorMemLaneInsn, pop(I32, V128)).setMemBits(16);
        regV(V128_STORE32_LANE, I32X4, "v128.store32_lane", VectorMemLaneInsn, pop(I32, V128)).setMemBits(32);
        regV(V128_STORE64_LANE, I64X2, "v128.store64_lane", VectorMemLaneInsn, pop(I32, V128)).setMemBits(64);
        regV(V128_CONST, null, "v128.const", VectorConstOrShuffleInsn, push(V128));
        regV(I8X16_SHUFFLE, I8X16, "i8x16.shuffle", VectorConstOrShuffleInsn, binOp(V128));
        regV(I8X16_EXTRACT_LANE_S, I8X16, "i8x16.extract_lane_s", VectorLaneInsn, pop(V128).and(push(I32)));
        regV(I8X16_EXTRACT_LANE_U, I8X16, "i8x16.extract_lane_u", VectorLaneInsn, pop(V128).and(push(I32)));
        regV(I8X16_REPLACE_LANE, I8X16, "i8x16.replace_lane", VectorLaneInsn, pop(V128, I32).and(push(V128)));
        regV(I16X8_EXTRACT_LANE_S, I16X8, "i16x8.extract_lane_s", VectorLaneInsn, pop(V128).and(push(I32)));
        regV(I16X8_EXTRACT_LANE_U, I16X8, "i16x8.extract_lane_u", VectorLaneInsn, pop(V128).and(push(I32)));
        regV(I16X8_REPLACE_LANE, I16X8, "i16x8.replace_lane", VectorLaneInsn, pop(V128, I32).and(push(V128)));
        regV(I32X4_EXTRACT_LANE, I32X4, "i32x4.extract_lane", VectorLaneInsn, pop(V128).and(push(I32)));
        regV(I32X4_REPLACE_LANE, I32X4, "i32x4.replace_lane", VectorLaneInsn, pop(V128, I32).and(push(V128)));
        regV(I64X2_EXTRACT_LANE, I64X2, "i64x2.extract_lane", VectorLaneInsn, pop(V128).and(push(I64)));
        regV(I64X2_REPLACE_LANE, I64X2, "i64x2.replace_lane", VectorLaneInsn, pop(V128, I64).and(push(V128)));
        regV(F32X4_EXTRACT_LANE, F32X4, "f32x4.extract_lane", VectorLaneInsn, pop(V128).and(push(F32)));
        regV(F32X4_REPLACE_LANE, F32X4, "f32x4.replace_lane", VectorLaneInsn, pop(V128, F32).and(push(V128)));
        regV(F64X2_EXTRACT_LANE, F64X2, "f64x2.extract_lane", VectorLaneInsn, pop(V128).and(push(F64)));
        regV(F64X2_REPLACE_LANE, F64X2, "f64x2.replace_lane", VectorLaneInsn, pop(V128, F64).and(push(V128)));
        regV(I8X16_SWIZZLE, I8X16, "i8x16.swizzle", VectorInsn, binOp(V128));
        regV(I8X16_SPLAT, I8X16, "i8x16.splat", VectorInsn, pop(I32).and(push(V128)));
        regV(I16X8_SPLAT, I16X8, "i16x8.splat", VectorInsn, pop(I32).and(push(V128)));
        regV(I32X4_SPLAT, I32X4, "i32x4.splat", VectorInsn, pop(I32).and(push(V128)));
        regV(I64X2_SPLAT, I64X2, "i64x2.splat", VectorInsn, pop(I64).and(push(V128)));
        regV(F32X4_SPLAT, F32X4, "f32x4.splat", VectorInsn, pop(F32).and(push(V128)));
        regV(F64X2_SPLAT, F64X2, "f64x2.splat", VectorInsn, pop(F64).and(push(V128)));
        regV(I8X16_EQ, I8X16, "i8x16.eq", VectorInsn, binOp(V128));
        regV(I8X16_NE, I8X16, "i8x16.ne", VectorInsn, binOp(V128));
        regV(I8X16_LT_S, I8X16, "i8x16.lt_s", VectorInsn, binOp(V128));
        regV(I8X16_LT_U, I8X16, "i8x16.lt_u", VectorInsn, binOp(V128));
        regV(I8X16_GT_S, I8X16, "i8x16.gt_s", VectorInsn, binOp(V128));
        regV(I8X16_GT_U, I8X16, "i8x16.gt_u", VectorInsn, binOp(V128));
        regV(I8X16_LE_S, I8X16, "i8x16.le_s", VectorInsn, binOp(V128));
        regV(I8X16_LE_U, I8X16, "i8x16.le_u", VectorInsn, binOp(V128));
        regV(I8X16_GE_S, I8X16, "i8x16.ge_s", VectorInsn, binOp(V128));
        regV(I8X16_GE_U, I8X16, "i8x16.ge_u", VectorInsn, binOp(V128));
        regV(I16X8_EQ, I16X8, "i16x8.eq", VectorInsn, binOp(V128));
        regV(I16X8_NE, I16X8, "i16x8.ne", VectorInsn, binOp(V128));
        regV(I16X8_LT_S, I16X8, "i16x8.lt_s", VectorInsn, binOp(V128));
        regV(I16X8_LT_U, I16X8, "i16x8.lt_u", VectorInsn, binOp(V128));
        regV(I16X8_GT_S, I16X8, "i16x8.gt_s", VectorInsn, binOp(V128));
        regV(I16X8_GT_U, I16X8, "i16x8.gt_u", VectorInsn, binOp(V128));
        regV(I16X8_LE_S, I16X8, "i16x8.le_s", VectorInsn, binOp(V128));
        regV(I16X8_LE_U, I16X8, "i16x8.le_u", VectorInsn, binOp(V128));
        regV(I16X8_GE_S, I16X8, "i16x8.ge_s", VectorInsn, binOp(V128));
        regV(I16X8_GE_U, I16X8, "i16x8.ge_u", VectorInsn, binOp(V128));
        regV(I32X4_EQ, I32X4, "i32x4.eq", VectorInsn, binOp(V128));
        regV(I32X4_NE, I32X4, "i32x4.ne", VectorInsn, binOp(V128));
        regV(I32X4_LT_S, I32X4, "i32x4.lt_s", VectorInsn, binOp(V128));
        regV(I32X4_LT_U, I32X4, "i32x4.lt_u", VectorInsn, binOp(V128));
        regV(I32X4_GT_S, I32X4, "i32x4.gt_s", VectorInsn, binOp(V128));
        regV(I32X4_GT_U, I32X4, "i32x4.gt_u", VectorInsn, binOp(V128));
        regV(I32X4_LE_S, I32X4, "i32x4.le_s", VectorInsn, binOp(V128));
        regV(I32X4_LE_U, I32X4, "i32x4.le_u", VectorInsn, binOp(V128));
        regV(I32X4_GE_S, I32X4, "i32x4.ge_s", VectorInsn, binOp(V128));
        regV(I32X4_GE_U, I32X4, "i32x4.ge_u", VectorInsn, binOp(V128));
        regV(I64X2_EQ, I64X2, "i64x2.eq", VectorInsn, binOp(V128));
        regV(I64X2_NE, I64X2, "i64x2.ne", VectorInsn, binOp(V128));
        regV(I64X2_LT_S, I64X2, "i64x2.lt_s", VectorInsn, binOp(V128));
        regV(I64X2_GT_S, I64X2, "i64x2.gt_s", VectorInsn, binOp(V128));
        regV(I64X2_LE_S, I64X2, "i64x2.le_s", VectorInsn, binOp(V128));
        regV(I64X2_GE_S, I64X2, "i64x2.ge_s", VectorInsn, binOp(V128));
        regV(F32X4_EQ, F32X4, "f32x4.eq", VectorInsn, binOp(V128));
        regV(F32X4_NE, F32X4, "f32x4.ne", VectorInsn, binOp(V128));
        regV(F32X4_LT, F32X4, "f32x4.lt", VectorInsn, binOp(V128));
        regV(F32X4_GT, F32X4, "f32x4.gt", VectorInsn, binOp(V128));
        regV(F32X4_LE, F32X4, "f32x4.le", VectorInsn, binOp(V128));
        regV(F32X4_GE, F32X4, "f32x4.ge", VectorInsn, binOp(V128));
        regV(F64X2_EQ, F64X2, "f64x2.eq", VectorInsn, binOp(V128));
        regV(F64X2_NE, F64X2, "f64x2.ne", VectorInsn, binOp(V128));
        regV(F64X2_LT, F64X2, "f64x2.lt", VectorInsn, binOp(V128));
        regV(F64X2_GT, F64X2, "f64x2.gt", VectorInsn, binOp(V128));
        regV(F64X2_LE, F64X2, "f64x2.le", VectorInsn, binOp(V128));
        regV(F64X2_GE, F64X2, "f64x2.ge", VectorInsn, binOp(V128));
        regV(V128_NOT, null, "v128.not", VectorInsn, unOp(V128));
        regV(V128_AND, null, "v128.and", VectorInsn, binOp(V128));
        regV(V128_ANDNOT, null, "v128.andnot", VectorInsn, binOp(V128));
        regV(V128_OR, null, "v128.or", VectorInsn, binOp(V128));
        regV(V128_XOR, null, "v128.xor", VectorInsn, binOp(V128));
        regV(V128_BITSELECT, null, "v128.bitselect", VectorInsn, pop(V128, V128, V128).and(push(V128)));
        regV(V128_ANY_TRUE, null, "v128.any_true", VectorInsn, testOp(V128));
        regV(I8X16_ABS, I8X16, "i8x16.abs", VectorInsn, unOp(V128));
        regV(I8X16_NEG, I8X16, "i8x16.neg", VectorInsn, unOp(V128));
        regV(I8X16_POPCNT, I8X16, "i8x16.popcnt", VectorInsn, unOp(V128));
        regV(I8X16_ALL_TRUE, I8X16, "i8x16.all_true", VectorInsn, testOp(V128));
        regV(I8X16_BITMASK, I8X16, "i8x16.bitmask", VectorInsn, testOp(V128));
        regV(I8X16_NARROW_I16X8_S, I8X16, "i8x16.narrow_i16x8_s", VectorInsn, binOp(V128));
        regV(I8X16_NARROW_I16X8_U, I8X16, "i8x16.narrow_i16x8_u", VectorInsn, binOp(V128));
        regV(I8X16_SHL, I8X16, "i8x16.shl", VectorInsn, pop(V128, I32).and(push(V128)));
        regV(I8X16_SHR_S, I8X16, "i8x16.shr_s", VectorInsn, pop(V128, I32).and(push(V128)));
        regV(I8X16_SHR_U, I8X16, "i8x16.shr_u", VectorInsn, pop(V128, I32).and(push(V128)));
        regV(I8X16_ADD, I8X16, "i8x16.add", VectorInsn, binOp(V128));
        regV(I8X16_ADD_SAT_S, I8X16, "i8x16.add_sat_s", VectorInsn, binOp(V128));
        regV(I8X16_ADD_SAT_U, I8X16, "i8x16.add_sat_u", VectorInsn, binOp(V128));
        regV(I8X16_SUB, I8X16, "i8x16.sub", VectorInsn, binOp(V128));
        regV(I8X16_SUB_SAT_S, I8X16, "i8x16.sub_sat_s", VectorInsn, binOp(V128));
        regV(I8X16_SUB_SAT_U, I8X16, "i8x16.sub_sat_u", VectorInsn, binOp(V128));
        regV(I8X16_MIN_S, I8X16, "i8x16.min_s", VectorInsn, binOp(V128));
        regV(I8X16_MIN_U, I8X16, "i8x16.min_u", VectorInsn, binOp(V128));
        regV(I8X16_MAX_S, I8X16, "i8x16.max_s", VectorInsn, binOp(V128));
        regV(I8X16_MAX_U, I8X16, "i8x16.max_u", VectorInsn, binOp(V128));
        regV(I8X16_AVGR_U, I8X16, "i8x16.avgr_u", VectorInsn, binOp(V128));
        regV(I16X8_EXTADD_PAIRWISE_I8X16_S, I16X8, "i16x8.extadd_pairwise_i8x16_s", VectorInsn, unOp(V128));
        regV(I16X8_EXTADD_PAIRWISE_I8X16_U, I16X8, "i16x8.extadd_pairwise_i8x16_u", VectorInsn, unOp(V128));
        regV(I16X8_ABS, I16X8, "i16x8.abs", VectorInsn, unOp(V128));
        regV(I16X8_NEG, I16X8, "i16x8.neg", VectorInsn, unOp(V128));
        regV(I16X8_Q15MULR_SAT_S, I16X8, "i16x8.q15mulr_sat_s", VectorInsn, binOp(V128));
        regV(I16X8_ALL_TRUE, I16X8, "i16x8.all_true", VectorInsn, testOp(V128));
        regV(I16X8_BITMASK, I16X8, "i16x8.bitmask", VectorInsn, testOp(V128));
        regV(I16X8_NARROW_I32X4_S, I16X8, "i16x8.narrow_i32x4_s", VectorInsn, binOp(V128));
        regV(I16X8_NARROW_I32X4_U, I16X8, "i16x8.narrow_i32x4_u", VectorInsn, binOp(V128));
        regV(I16X8_EXTEND_LOW_I8X16_S, I16X8, "i16x8.extend_low_i8x16_s", VectorInsn, unOp(V128));
        regV(I16X8_EXTEND_HIGH_I8X16_S, I16X8, "i16x8.extend_high_i8x16_s", VectorInsn, unOp(V128));
        regV(I16X8_EXTEND_LOW_I8X16_U, I16X8, "i16x8.extend_low_i8x16_u", VectorInsn, unOp(V128));
        regV(I16X8_EXTEND_HIGH_I8X16_U, I16X8, "i16x8.extend_high_i8x16_u", VectorInsn, unOp(V128));
        regV(I16X8_SHL, I16X8, "i16x8.shl", VectorInsn, pop(V128, I32).and(push(V128)));
        regV(I16X8_SHR_S, I16X8, "i16x8.shr_s", VectorInsn, pop(V128, I32).and(push(V128)));
        regV(I16X8_SHR_U, I16X8, "i16x8.shr_u", VectorInsn, pop(V128, I32).and(push(V128)));
        regV(I16X8_ADD, I16X8, "i16x8.add", VectorInsn, binOp(V128));
        regV(I16X8_ADD_SAT_S, I16X8, "i16x8.add_sat_s", VectorInsn, binOp(V128));
        regV(I16X8_ADD_SAT_U, I16X8, "i16x8.add_sat_u", VectorInsn, binOp(V128));
        regV(I16X8_SUB, I16X8, "i16x8.sub", VectorInsn, binOp(V128));
        regV(I16X8_SUB_SAT_S, I16X8, "i16x8.sub_sat_s", VectorInsn, binOp(V128));
        regV(I16X8_SUB_SAT_U, I16X8, "i16x8.sub_sat_u", VectorInsn, binOp(V128));
        regV(I16X8_MUL, I16X8, "i16x8.mul", VectorInsn, binOp(V128));
        regV(I16X8_MIN_S, I16X8, "i16x8.min_s", VectorInsn, binOp(V128));
        regV(I16X8_MIN_U, I16X8, "i16x8.min_u", VectorInsn, binOp(V128));
        regV(I16X8_MAX_S, I16X8, "i16x8.max_s", VectorInsn, binOp(V128));
        regV(I16X8_MAX_U, I16X8, "i16x8.max_u", VectorInsn, binOp(V128));
        regV(I16X8_AVGR_U, I16X8, "i16x8.avgr_u", VectorInsn, binOp(V128));
        regV(I16X8_EXTMUL_LOW_I8X16_S, I16X8, "i16x8.extmul_low_i8x16_s", VectorInsn, binOp(V128));
        regV(I16X8_EXTMUL_HIGH_I8X16_S, I16X8, "i16x8.extmul_high_i8x16_s", VectorInsn, binOp(V128));
        regV(I16X8_EXTMUL_LOW_I8X16_U, I16X8, "i16x8.extmul_low_i8x16_u", VectorInsn, binOp(V128));
        regV(I16X8_EXTMUL_HIGH_I8X16_U, I16X8, "i16x8.extmul_high_i8x16_u", VectorInsn, binOp(V128));
        regV(I32X4_EXTADD_PAIRWISE_I16X8_S, I32X4, "i32x4.extadd_pairwise_i16x8_s", VectorInsn, unOp(V128));
        regV(I32X4_EXTADD_PAIRWISE_I16X8_U, I32X4, "i32x4.extadd_pairwise_i16x8_u", VectorInsn, unOp(V128));
        regV(I32X4_ABS, I32X4, "i32x4.abs", VectorInsn, unOp(V128));
        regV(I32X4_NEG, I32X4, "i32x4.neg", VectorInsn, unOp(V128));
        regV(I32X4_ALL_TRUE, I32X4, "i32x4.all_true", VectorInsn, testOp(V128));
        regV(I32X4_BITMASK, I32X4, "i32x4.bitmask", VectorInsn, testOp(V128));
        regV(I32X4_EXTEND_LOW_I16X8_S, I32X4, "i32x4.extend_low_i16x8_s", VectorInsn, unOp(V128));
        regV(I32X4_EXTEND_HIGH_I16X8_S, I32X4, "i32x4.extend_high_i16x8_s", VectorInsn, unOp(V128));
        regV(I32X4_EXTEND_LOW_I16X8_U, I32X4, "i32x4.extend_low_i16x8_u", VectorInsn, unOp(V128));
        regV(I32X4_EXTEND_HIGH_I16X8_U, I32X4, "i32x4.extend_high_i16x8_u", VectorInsn, unOp(V128));
        regV(I32X4_SHL, I32X4, "i32x4.shl", VectorInsn, pop(V128, I32).and(push(V128)));
        regV(I32X4_SHR_S, I32X4, "i32x4.shr_s", VectorInsn, pop(V128, I32).and(push(V128)));
        regV(I32X4_SHR_U, I32X4, "i32x4.shr_u", VectorInsn, pop(V128, I32).and(push(V128)));
        regV(I32X4_ADD, I32X4, "i32x4.add", VectorInsn, binOp(V128));
        regV(I32X4_SUB, I32X4, "i32x4.sub", VectorInsn, binOp(V128));
        regV(I32X4_MUL, I32X4, "i32x4.mul", VectorInsn, binOp(V128));
        regV(I32X4_MIN_S, I32X4, "i32x4.min_s", VectorInsn, binOp(V128));
        regV(I32X4_MIN_U, I32X4, "i32x4.min_u", VectorInsn, binOp(V128));
        regV(I32X4_MAX_S, I32X4, "i32x4.max_s", VectorInsn, binOp(V128));
        regV(I32X4_MAX_U, I32X4, "i32x4.max_u", VectorInsn, binOp(V128));
        regV(I32X4_DOT_I16X8_S, I32X4, "i32x4.dot_i16x8_s", VectorInsn, binOp(V128));
        regV(I32X4_EXTMUL_LOW_I16X8_S, I32X4, "i32x4.extmul_low_i16x8_s", VectorInsn, binOp(V128));
        regV(I32X4_EXTMUL_HIGH_I16X8_S, I32X4, "i32x4.extmul_high_i16x8_s", VectorInsn, binOp(V128));
        regV(I32X4_EXTMUL_LOW_I16X8_U, I32X4, "i32x4.extmul_low_i16x8_u", VectorInsn, binOp(V128));
        regV(I32X4_EXTMUL_HIGH_I16X8_U, I32X4, "i32x4.extmul_high_i16x8_u", VectorInsn, binOp(V128));
        regV(I64X2_ABS, I64X2, "i64x2.abs", VectorInsn, unOp(V128));
        regV(I64X2_NEG, I64X2, "i64x2.neg", VectorInsn, unOp(V128));
        regV(I64X2_ALL_TRUE, I64X2, "i64x2.all_true", VectorInsn, testOp(V128));
        regV(I64X2_BITMASK, I64X2, "i64x2.bitmask", VectorInsn, testOp(V128));
        regV(I64X2_EXTEND_LOW_I32X4_S, I64X2, "i64x2.extend_low_i32x4_s", VectorInsn, unOp(V128));
        regV(I64X2_EXTEND_HIGH_I32X4_S, I64X2, "i64x2.extend_high_i32x4_s", VectorInsn, unOp(V128));
        regV(I64X2_EXTEND_LOW_I32X4_U, I64X2, "i64x2.extend_low_i32x4_u", VectorInsn, unOp(V128));
        regV(I64X2_EXTEND_HIGH_I32X4_U, I64X2, "i64x2.extend_high_i32x4_u", VectorInsn, unOp(V128));
        regV(I64X2_SHL, I64X2, "i64x2.shl", VectorInsn, pop(V128, I32).and(push(V128)));
        regV(I64X2_SHR_S, I64X2, "i64x2.shr_s", VectorInsn, pop(V128, I32).and(push(V128)));
        regV(I64X2_SHR_U, I64X2, "i64x2.shr_u", VectorInsn, pop(V128, I32).and(push(V128)));
        regV(I64X2_ADD, I64X2, "i64x2.add", VectorInsn, binOp(V128));
        regV(I64X2_SUB, I64X2, "i64x2.sub", VectorInsn, binOp(V128));
        regV(I64X2_MUL, I64X2, "i64x2.mul", VectorInsn, binOp(V128));
        regV(I64X2_EXTMUL_LOW_I32X4_S, I64X2, "i64x2.extmul_low_i32x4_s", VectorInsn, binOp(V128));
        regV(I64X2_EXTMUL_HIGH_I32X4_S, I64X2, "i64x2.extmul_high_i32x4_s", VectorInsn, binOp(V128));
        regV(I64X2_EXTMUL_LOW_I32X4_U, I64X2, "i64x2.extmul_low_i32x4_u", VectorInsn, binOp(V128));
        regV(I64X2_EXTMUL_HIGH_I32X4_U, I64X2, "i64x2.extmul_high_i32x4_u", VectorInsn, binOp(V128));
        regV(F32X4_CEIL, F32X4, "f32x4.ceil", VectorInsn, unOp(V128));
        regV(F32X4_FLOOR, F32X4, "f32x4.floor", VectorInsn, unOp(V128));
        regV(F32X4_TRUNC, F32X4, "f32x4.trunc", VectorInsn, unOp(V128));
        regV(F32X4_NEAREST, F32X4, "f32x4.nearest", VectorInsn, unOp(V128));
        regV(F32X4_ABS, F32X4, "f32x4.abs", VectorInsn, unOp(V128));
        regV(F32X4_NEG, F32X4, "f32x4.neg", VectorInsn, unOp(V128));
        regV(F32X4_SQRT, F32X4, "f32x4.sqrt", VectorInsn, unOp(V128));
        regV(F32X4_ADD, F32X4, "f32x4.add", VectorInsn, binOp(V128));
        regV(F32X4_SUB, F32X4, "f32x4.sub", VectorInsn, binOp(V128));
        regV(F32X4_MUL, F32X4, "f32x4.mul", VectorInsn, binOp(V128));
        regV(F32X4_DIV, F32X4, "f32x4.div", VectorInsn, binOp(V128));
        regV(F32X4_MIN, F32X4, "f32x4.min", VectorInsn, binOp(V128));
        regV(F32X4_MAX, F32X4, "f32x4.max", VectorInsn, binOp(V128));
        regV(F32X4_PMIN, F32X4, "f32x4.pmin", VectorInsn, binOp(V128));
        regV(F32X4_PMAX, F32X4, "f32x4.pmax", VectorInsn, binOp(V128));
        regV(F64X2_CEIL, F64X2, "f64x2.ceil", VectorInsn, unOp(V128));
        regV(F64X2_FLOOR, F64X2, "f64x2.floor", VectorInsn, unOp(V128));
        regV(F64X2_TRUNC, F64X2, "f64x2.trunc", VectorInsn, unOp(V128));
        regV(F64X2_NEAREST, F64X2, "f64x2.nearest", VectorInsn, unOp(V128));
        regV(F64X2_ABS, F64X2, "f64x2.abs", VectorInsn, unOp(V128));
        regV(F64X2_NEG, F64X2, "f64x2.neg", VectorInsn, unOp(V128));
        regV(F64X2_SQRT, F64X2, "f64x2.sqrt", VectorInsn, unOp(V128));
        regV(F64X2_ADD, F64X2, "f64x2.add", VectorInsn, binOp(V128));
        regV(F64X2_SUB, F64X2, "f64x2.sub", VectorInsn, binOp(V128));
        regV(F64X2_MUL, F64X2, "f64x2.mul", VectorInsn, binOp(V128));
        regV(F64X2_DIV, F64X2, "f64x2.div", VectorInsn, binOp(V128));
        regV(F64X2_MIN, F64X2, "f64x2.min", VectorInsn, binOp(V128));
        regV(F64X2_MAX, F64X2, "f64x2.max", VectorInsn, binOp(V128));
        regV(F64X2_PMIN, F64X2, "f64x2.pmin", VectorInsn, binOp(V128));
        regV(F64X2_PMAX, F64X2, "f64x2.pmax", VectorInsn, binOp(V128));
        regV(I32X4_TRUNC_SAT_F32X4_S, I32X4, "i32x4.trunc_sat_f32x4_s", VectorInsn, unOp(V128));
        regV(I32X4_TRUNC_SAT_F32X4_U, I32X4, "i32x4.trunc_sat_f32x4_u", VectorInsn, unOp(V128));
        regV(F32X4_CONVERT_I32X4_S, F32X4, "f32x4.convert_i32x4_s", VectorInsn, unOp(V128));
        regV(F32X4_CONVERT_I32X4_U, F32X4, "f32x4.convert_i32x4_u", VectorInsn, unOp(V128));
        regV(I32X4_TRUNC_SAT_F64X2_S_ZERO, I32X4, "i32x4.trunc_sat_f64x2_s_zero", VectorInsn, unOp(V128));
        regV(I32X4_TRUNC_SAT_F64X2_U_ZERO, I32X4, "i32x4.trunc_sat_f64x2_u_zero", VectorInsn, unOp(V128));
        regV(F64X2_CONVERT_LOW_I32X4_S, F64X2, "f64x2.convert_low_i32x4_s", VectorInsn, unOp(V128));
        regV(F64X2_CONVERT_LOW_I32X4_U, F64X2, "f64x2.convert_low_i32x4_u", VectorInsn, unOp(V128));
        regV(F32X4_DEMOTE_F64X2_ZERO, F32X4, "f32x4.demote_f64x2_zero", VectorInsn, unOp(V128));
        regV(F64X2_PROMOTE_LOW_F32X4, F64X2, "f64x2.promote_low_f32x4", VectorInsn, unOp(V128));
    }
}
