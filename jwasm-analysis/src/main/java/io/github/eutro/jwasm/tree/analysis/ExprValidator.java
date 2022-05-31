package io.github.eutro.jwasm.tree.analysis;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.ValidationException;
import io.github.eutro.jwasm.tree.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.eutro.jwasm.Opcodes.*;
import static io.github.eutro.jwasm.tree.analysis.ModuleValidator.assertMsg;

/**
 * An {@link ExprVisitor} that verifies whether the expression is well-formed.
 * <p>
 * Based on the validation algorithm published
 * <a href="https://webassembly.github.io/spec/core/appendix/algorithm.html#algo-valid">here</a>.
 */
public class ExprValidator extends ExprVisitor implements Validator {
    public static class CtrlFrame {
        public int opcode;
        public List<Byte> startTypes, endTypes;
        public int height;
        public boolean unreachable;
    }

    public final List<Byte> vals = new ArrayList<>();
    public final List<CtrlFrame> ctrls = new ArrayList<>();

    protected ModuleValidator validator;
    protected byte[] locals;
    protected byte @Nullable [] returns;
    protected byte [] result;
    protected int insn = 0;

    public ExprValidator(
            ModuleValidator validator,
            byte[] locals,
            byte @Nullable [] returns,
            byte[] result,
            @Nullable ExprVisitor dl
    ) {
        super(dl);
        this.validator = validator;
        this.locals = locals;
        this.returns = returns;
        this.result = result;
        pushC(END, Collections.emptyList(), new ByteList(result));
    }

    public static boolean isNum(@Nullable Byte ty) {
        if (ty == null) return true;
        switch (ty) {
            case I32:
            case I64:
            case F32:
            case F64:
                return true;
        }
        return false;
    }

    public static boolean isRef(@Nullable Byte ty) {
        if (ty == null) return true;
        switch (ty) {
            case FUNCREF:
            case EXTERNREF:
                return true;
        }
        return false;
    }

    public static boolean isValType(@Nullable Byte ty) {
        return isNum(ty) || isRef(ty);
    }

    protected CtrlFrame ctrlsRef(int idx) {
        if (idx < ctrls.size()) {
            return ctrls.get(ctrls.size() - idx - 1);
        }
        throw new ValidationException("Control frame out of range", null);
    }

    protected void pushV(@Nullable Byte type) {
        vals.add(type);
    }

    protected @Nullable Byte popV() {
        if (vals.size() == ctrlsRef(0).height) {
            if (ctrlsRef(0).unreachable) {
                return null;
            } else {
                throw new ValidationException("Pop underflows current block", null);
            }
        }
        return vals.remove(vals.size() - 1);
    }

    private @Nullable Byte popV(Byte expect) {
        Byte actual = popV();
        if (actual != null && expect != null && (byte) actual != expect) {
            throw new ValidationException(String.format("Mismatched types, expected 0x%02x, got 0x%02x", expect, actual), null);
        }
        return actual;
    }

    protected void pushVs(Iterable<Byte> types) {
        for (Byte type : types) {
            pushV(type);
        }
    }

    private void pushVs(byte... types) {
        pushVs(new ByteList(types));
    }

    protected List<Byte> popVs(List<Byte> types) {
        List<Byte> popped = new ArrayList<>();
        for (int i = types.size() - 1; i >= 0; i--) {
            popped.add(popV(types.get(i)));
        }
        return popped;
    }

    protected void popVs(byte... types) {
        popVs(new ByteList(types));
    }

    protected void pushC(int opcode, List<Byte> ins, List<Byte> outs) {
        CtrlFrame frame = new CtrlFrame();
        frame.opcode = opcode;
        frame.startTypes = ins;
        frame.endTypes = outs;
        frame.height = vals.size();
        frame.unreachable = false;
        ctrls.add(frame);
        pushVs(ins);
    }

    protected CtrlFrame popC() {
        if (ctrls.isEmpty()) {
            throw new ValidationException("Attempted to pop empty control stack", null);
        }
        CtrlFrame frame = ctrlsRef(0);
        popVs(frame.endTypes);
        if (vals.size() != frame.height) {
            throw new ValidationException("Stack height does not match frame height", null);
        }
        ctrls.remove(ctrls.size() - 1);
        return frame;
    }

    protected List<Byte> labelTypes(CtrlFrame frame) {
        return frame.opcode == LOOP ? frame.startTypes : frame.endTypes;
    }

    protected void unreachable() {
        CtrlFrame frame = ctrlsRef(0);
        while (vals.size() > frame.height) {
            vals.remove(vals.size() - 1);
        }
        frame.unreachable = true;
    }

    protected void bumpI() {
        insn++;
    }

    @Override
    public void visitInsn(byte opcode) {
        super.visitInsn(opcode);
        switch (opcode) {
            case UNREACHABLE:
                unreachable();
                break;
            case NOP:
                break;
            case RETURN:
                assertMsg(returns != null, "expression does not have a return type");
                popVs(returns);
                unreachable();
                break;
            case REF_IS_NULL:
                assertMsg(isRef(popV()), "ref.is_null argument must be a reference type");
                pushV(I32);
                break;
            case DROP: {
                popV();
                break;
            }
            case SELECT: {
                popV(I32);
                Byte t1, t2;
                t1 = popV();
                t2 = popV();
                assertMsg(isNum(t1) && isNum(t2), "select arguments must both be numbers");
                assertMsg(!(t1 != null && t2 != null && (byte) t1 != t2), "select argument types do not match");
                pushV(t1 == null ? t2 : t1);
                break;
            }

            case I32_EQ:
            case I32_NE:
            case I32_LT_S:
            case I32_LT_U:
            case I32_GT_S:
            case I32_GT_U:
            case I32_LE_S:
            case I32_LE_U:
            case I32_GE_S:
            case I32_GE_U:
                // and by coincidence...
            case I32_ADD:
            case I32_SUB:
            case I32_MUL:
            case I32_DIV_S:
            case I32_DIV_U:
            case I32_REM_S:
            case I32_REM_U:
            case I32_AND:
            case I32_OR:
            case I32_XOR:
            case I32_SHL:
            case I32_SHR_S:
            case I32_SHR_U:
            case I32_ROTL:
            case I32_ROTR:
                popVs(I32, I32);
                pushV(I32);
                break;

            case I64_EQ:
            case I64_NE:
            case I64_LT_S:
            case I64_LT_U:
            case I64_GT_S:
            case I64_GT_U:
            case I64_LE_S:
            case I64_LE_U:
            case I64_GE_S:
            case I64_GE_U:
                popVs(I64, I64);
                pushV(I32);
                break;

            case F32_EQ:
            case F32_NE:
            case F32_LT:
            case F32_GT:
            case F32_LE:
            case F32_GE:
                popVs(F32, F32);
                pushV(I32);
                break;

            case F64_EQ:
            case F64_NE:
            case F64_LT:
            case F64_GT:
            case F64_LE:
            case F64_GE:
                popVs(F64, F64);
                pushV(I32);
                break;

            case I32_EQZ:
            case I32_CLZ:
            case I32_CTZ:
            case I32_POPCNT:
                popV(I32);
                pushV(I32);
                break;

            case I64_EQZ:

            case I32_WRAP_I64:
                popV(I64);
                pushV(I32);
                break;

            case I64_CLZ:
            case I64_CTZ:
            case I64_POPCNT:
                popV(I64);
                pushV(I64);
                break;

            case I64_ADD:
            case I64_SUB:
            case I64_MUL:
            case I64_DIV_S:
            case I64_DIV_U:
            case I64_REM_S:
            case I64_REM_U:
            case I64_AND:
            case I64_OR:
            case I64_XOR:
            case I64_SHL:
            case I64_SHR_S:
            case I64_SHR_U:
            case I64_ROTL:
            case I64_ROTR:
                popVs(I64, I64);
                pushV(I64);
                break;

            case F32_ABS:
            case F32_NEG:
            case F32_CEIL:
            case F32_FLOOR:
            case F32_TRUNC:
            case F32_NEAREST:
            case F32_SQRT:
                popV(F32);
                pushV(F32);
                break;

            case F32_ADD:
            case F32_SUB:
            case F32_MUL:
            case F32_DIV:
            case F32_MIN:
            case F32_MAX:
            case F32_COPYSIGN:
                popVs(F32, F32);
                pushV(F32);
                break;

            case F64_ABS:
            case F64_NEG:
            case F64_CEIL:
            case F64_FLOOR:
            case F64_TRUNC:
            case F64_NEAREST:
            case F64_SQRT:
                popV(F64);
                pushV(F64);
                break;

            case F64_ADD:
            case F64_SUB:
            case F64_MUL:
            case F64_DIV:
            case F64_MIN:
            case F64_MAX:
            case F64_COPYSIGN:
                popVs(F64, F64);
                pushV(F64);
                break;
            case I32_TRUNC_F32_S:
            case I32_TRUNC_F32_U:
            case I32_REINTERPRET_F32:
                popV(F32);
                pushV(I32);
                break;
            case I32_TRUNC_F64_S:
            case I32_TRUNC_F64_U:
                popV(F64);
                pushV(I32);
                break;
            case I64_EXTEND_I32_S:
            case I64_EXTEND_I32_U:
                popV(I32);
                pushV(I64);
                break;
            case I64_TRUNC_F32_S:
            case I64_TRUNC_F32_U:
                popV(F32);
                pushV(I64);
                break;
            case I64_TRUNC_F64_S:
            case I64_TRUNC_F64_U:
            case I64_REINTERPRET_F64:
                popV(F64);
                pushV(I64);
                break;
            case F32_CONVERT_I32_S:
            case F32_CONVERT_I32_U:
            case F32_REINTERPRET_I32:
                popV(I32);
                pushV(F32);
                break;
            case F32_CONVERT_I64_S:
            case F32_CONVERT_I64_U:
                popV(I64);
                pushV(F32);
                break;
            case F32_DEMOTE_F64:
                popV(F64);
                pushV(F32);
                break;
            case F64_CONVERT_I32_S:
            case F64_CONVERT_I32_U:
                popV(I32);
                pushV(F64);
                break;
            case F64_CONVERT_I64_S:
            case F64_CONVERT_I64_U:
            case F64_REINTERPRET_I64:
                popV(I64);
                pushV(F64);
                break;
            case F64_PROMOTE_F32:
                popV(F32);
                pushV(F64);
                break;

            case I32_EXTEND8_S:
            case I32_EXTEND16_S:
                popVs(I32);
                pushV(I32);
                break;
            case I64_EXTEND8_S:
            case I64_EXTEND16_S:
            case I64_EXTEND32_S:
                popVs(I32);
                pushV(I64);
                break;

            case MEMORY_SIZE:
                assertMsg(validator.mems != null && validator.mems.memories != null && !validator.mems.memories.isEmpty(),
                        "memory 0 does not exist");
                pushV(I32);
                break;
            case MEMORY_GROW:
                assertMsg(validator.mems != null && validator.mems.memories != null && !validator.mems.memories.isEmpty(),
                        "memory 0 does not exist");
                popV(I32);
                pushV(I32);
                break;

            default:
                throw new ValidationException(String.format("0x%02x is not a valid zero-arity instruction", opcode), null);
        }
        bumpI();
    }

    @Override
    public void visitPrefixInsn(int opcode) {
        super.visitPrefixInsn(opcode);
        switch (opcode) {
            case I32_TRUNC_SAT_F32_S:
            case I32_TRUNC_SAT_F32_U:
                popV(F32);
                pushV(I32);
                break;
            case I32_TRUNC_SAT_F64_S:
            case I32_TRUNC_SAT_F64_U:
                popV(F64);
                pushV(I32);
                break;
            case I64_TRUNC_SAT_F32_S:
            case I64_TRUNC_SAT_F32_U:
                popV(F32);
                pushV(I64);
                break;
            case I64_TRUNC_SAT_F64_S:
            case I64_TRUNC_SAT_F64_U:
                popV(F64);
                pushV(I64);
                break;
            default:
                throw new ValidationException(String.format("opcode 0x%02x %d does not exist", INSN_PREFIX, opcode), null);
        }
        bumpI();
    }

    @Override
    public void visitConstInsn(Object v) {
        super.visitConstInsn(v);
        if (v instanceof Integer) {
            pushV(I32);
        } else if (v instanceof Long) {
            pushV(I64);
        } else if (v instanceof Float) {
            pushV(F32);
        } else if (v instanceof Double) {
            pushV(F64);
        } else {
            throw new IllegalArgumentException();
        }
        bumpI();
    }

    @Override
    public void visitNullInsn(byte type) {
        super.visitNullInsn(type);
        assertMsg(isRef(type), "0x%02x is not a reference type", type);
        pushV(type);
        bumpI();
    }

    @Override
    public void visitFuncRefInsn(int function) {
        super.visitFuncRefInsn(function);
        assertMsg(validator.referencableFuncs.size() > function, "function %d does not exist", function);
        pushV(FUNCREF);
        bumpI();
    }

    @Override
    public void visitSelectInsn(byte[] type) {
        super.visitSelectInsn(type);
        assertMsg(type.length == 1, "the length of type in a select must be 1");
        byte t = type[0];
        popVs(t, t, I32);
        pushV(t);
        bumpI();
    }

    @Override
    public void visitVariableInsn(byte opcode, int variable) {
        super.visitVariableInsn(opcode, variable);
        boolean isLocal, pops = false, pushes = false;
        byte type;
        switch (opcode) {
            case LOCAL_GET: isLocal = true; pushes = true; break;
            case LOCAL_SET: isLocal = true; pops = true; break;
            case LOCAL_TEE: isLocal = true; pops = true; pushes = true; break;
            case GLOBAL_GET: isLocal = false; pushes = true; break;
            case GLOBAL_SET: isLocal = false; pops = true; break;
            default:
                throw new ValidationException(String.format("0x%02x is not a valid variable insn", opcode), null);
        }
        if (isLocal) {
            assertMsg(locals.length > variable, "local %d does not exist", variable);
            type = locals[variable];
        } else {
            assertMsg(validator.globals != null && validator.globals.globals != null && validator.globals.globals.size() > variable,
                    "global %d does not exist", variable);
            GlobalNode gn = validator.globals.globals.get(variable);
            if (pops) {
                assertMsg(gn.type.mut == MUT_VAR, "global %d is not mutable", variable);
            }
            type = gn.type.type;
        }
        if (pops) {
            popV(type);
        }
        if (pushes) {
            pushV(type);
        }
        bumpI();
    }

    @Override
    public void visitTableInsn(byte opcode, int table) {
        super.visitTableInsn(opcode, table);
        assertMsg(validator.tables != null && validator.tables.tables != null && validator.tables.tables.size() > table,
                "table %d does note exist", table);
        TableNode tn = validator.tables.tables.get(table);
        switch (opcode) {
            case TABLE_GET:
                popV(I32);
                pushV(tn.type);
                break;
            case TABLE_SET:
                popVs(I32, tn.type);
                break;
            default:
                throw new ValidationException(String.format("0x%02x is not a valid table insn", opcode), null);
        }
        bumpI();
    }

    @Override
    public void visitPrefixTableInsn(int opcode, int table) {
        super.visitPrefixTableInsn(opcode, table);
        if (opcode == ELEM_DROP) {
            assertMsg(validator.elems != null && validator.elems.elems != null && validator.elems.elems.size() > table,
                    "elem %d does not exist", table);
        } else {
            assertMsg(validator.tables != null && validator.tables.tables != null && validator.tables.tables.size() > table,
                    "table %d does not exist", table);
            TableNode tn = validator.tables.tables.get(table);
            switch (opcode) {
                case TABLE_SIZE:
                    pushV(I32);
                    break;
                case TABLE_GROW:
                    popVs(tn.type, I32);
                    pushV(I32);
                    break;
                case TABLE_FILL:
                    popVs(I32, tn.type, I32);
                default:
                    throw new ValidationException(String.format("0x%02x %d is not a valid table insn", INSN_PREFIX, opcode), null);
            }
        }
        bumpI();
    }

    @Override
    public void visitPrefixBinaryTableInsn(int opcode, int firstIndex, int secondIndex) {
        super.visitPrefixBinaryTableInsn(opcode, firstIndex, secondIndex);
        switch (opcode) {
            case TABLE_INIT:
                assertMsg(validator.tables != null && validator.tables.tables != null && validator.tables.tables.size() > firstIndex,
                        "table %d does not exist", firstIndex);
                assertMsg(validator.elems != null && validator.elems.elems != null && validator.elems.elems.size() > secondIndex,
                        "element %d does not exist", secondIndex);
                assertMsg(validator.tables.tables.get(firstIndex).type == validator.elems.elems.get(secondIndex).type,
                        "element and table types don't match");
                popVs(I32, I32, I32);
                break;
            case TABLE_COPY:
                assertMsg(validator.tables != null && validator.tables.tables != null && validator.tables.tables.size() > firstIndex,
                        "table %d does not exist", firstIndex);
                assertMsg(validator.tables != null && validator.tables.tables != null && validator.tables.tables.size() > secondIndex,
                        "table %d does not exist", secondIndex);
                assertMsg(validator.tables.tables.get(firstIndex).type == validator.tables.tables.get(secondIndex).type,
                        "table types don't match");
                popVs(I32, I32, I32);
                break;
            default:
                throw new ValidationException(String.format("0x%02x %d is not a valid table insn", INSN_PREFIX, opcode), null);
        }
        bumpI();
    }

    @Override
    public void visitMemInsn(byte opcode, int align, int offset) {
        super.visitMemInsn(opcode, align, offset);
        assertMsg(validator.mems != null && validator.mems.memories != null && !validator.mems.memories.isEmpty(),
                "memory %d does not exist", 0);
        boolean isPop;
        byte type;
        switch (opcode) {
            case I32_LOAD:
            case I32_LOAD8_S:
            case I32_LOAD8_U:
            case I32_LOAD16_S:
            case I32_LOAD16_U: isPop = false; type = I32; break;
            case I64_LOAD:
            case I64_LOAD8_S:
            case I64_LOAD8_U:
            case I64_LOAD16_S:
            case I64_LOAD16_U:
            case I64_LOAD32_S:
            case I64_LOAD32_U: isPop = false; type = I64; break;
            case F32_LOAD: isPop = false; type = F32;  break;
            case F64_LOAD: isPop = false; type = F64;  break;
            case I32_STORE:
            case I32_STORE8:
            case I32_STORE16: isPop = true; type = I32; break;
            case I64_STORE:
            case I64_STORE8:
            case I64_STORE16:
            case I64_STORE32: isPop = true; type = I64; break;
            case F32_STORE: isPop = true; type = F32; break;
            case F64_STORE: isPop = true; type = F64; break;
            default:
                throw new ValidationException(String.format("0x%02d is not a valid memory insn", opcode), null);
        }
        if (isPop) {
            popV(type);
            popV(I32);
        } else {
            popV(I32);
            pushV(type);
        }
        bumpI();
    }

    @Override
    public void visitIndexedMemInsn(int opcode, int index) {
        super.visitIndexedMemInsn(opcode, index);
        if (opcode == DATA_DROP) {
            assertMsg(validator.dataCount != null && validator.dataCount > index,
                    "data %d does not exist", index);
        } else {
            assertMsg(validator.mems != null && validator.mems.memories != null && validator.mems.memories.size() > index,
                    "memory %d does not exist", index);
            switch (opcode) {
                case MEMORY_INIT:
                    assertMsg(validator.dataCount != null && validator.dataCount > index,
                            "data %d is not defined", index);
                    popVs(I32, I32, I32);
                    break;
                case MEMORY_COPY:
                case MEMORY_FILL:
                    popVs(I32, I32, I32);
                    break;
                default:
                    throw new ValidationException(String.format("0x%02d is not a valid indexed memory insn", opcode), null);
            }
        }
        bumpI();
    }

    @Override
    public void visitBlockInsn(byte opcode, int blockType) {
        super.visitBlockInsn(opcode, blockType);
        List<Byte> inputs, outputs;
        switch (blockType) {
            case EMPTY_TYPE:
                inputs = Collections.emptyList();
                outputs = Collections.emptyList();
                break;
            case I32:
            case I64:
            case F32:
            case F64:
            case FUNCREF:
            case EXTERNREF:
                inputs = Collections.emptyList();
                outputs = Collections.singletonList((byte) blockType);
                break;
            default:
                if (blockType >= 0) {
                    if (validator.types != null && validator.types.types != null && blockType < validator.types.types.size()) {
                        TypeNode funcTy = validator.types.types.get(blockType);
                        inputs = new ByteList(funcTy.params);
                        outputs = new ByteList(funcTy.returns);
                        break;
                    }
                }
                throw new ValidationException(String.format("Block type 0x%02x does not exist.", blockType), null);
        }
        switch (opcode) {
            case BLOCK:
            case LOOP: {
                popVs(inputs);
                pushC(opcode, inputs, outputs);
                break;
            }
            case IF: {
                popV(I32);
                popVs(inputs);
                pushC(opcode, inputs, outputs);
                break;
            }
            default:
                throw new ValidationException(String.format("0x%02x is not a valid block insn", opcode), null);
        }
        bumpI();
    }

    @Override
    public void visitElseInsn() {
        super.visitElseInsn();
        CtrlFrame frame = popC();
        assertMsg(frame.opcode == IF, "else instruction not in if block");
        pushC(ELSE, frame.startTypes, frame.endTypes);
        bumpI();
    }

    @Override
    public void visitEndInsn() {
        super.visitEndInsn();
        CtrlFrame frame = popC();
        pushVs(frame.endTypes);
        bumpI();
    }

    @Override
    public void visitBreakInsn(byte opcode, int label) {
        super.visitBreakInsn(opcode, label);
        assertMsg(ctrls.size() > label, "label %d does not exist at this depth", label);
        List<Byte> labelTys = labelTypes(ctrlsRef(label));
        switch (opcode) {
            case BR:
                popVs(labelTys);
                unreachable();
                break;
            case BR_IF:
                popV(I32);
                popVs(labelTys);
                pushVs(labelTys);
                break;
            default:
                throw new ValidationException(String.format("0x%02x is not a valid break insn", opcode), null);
        }
        bumpI();
    }

    @Override
    public void visitTableBreakInsn(int[] labels, int defaultLabel) {
        super.visitTableBreakInsn(labels, defaultLabel);
        assertMsg(ctrls.size() > defaultLabel, "label %d does not exist at this depth", defaultLabel);
        popV(I32);
        List<Byte> defaultLabelTys = labelTypes(ctrlsRef(defaultLabel));
        int arity = defaultLabelTys.size();
        for (int n : labels) {
            assertMsg(ctrls.size() > n, "label %d does not exist at this depth", n);
            List<Byte> labelTys = labelTypes(ctrlsRef(n));
            assertMsg(labelTys.size() == arity, "block type mismatch");
            pushVs(popVs(labelTys));
        }
        popVs(defaultLabelTys);
        unreachable();
        bumpI();
    }

    @Override
    public void visitCallInsn(int function) {
        super.visitCallInsn(function);
        assertMsg(validator.referencableFuncs.size() > function,
                "function %d does not exist", function);
        FuncNode func = validator.referencableFuncs.get(function);
        assertMsg(validator.types != null && validator.types.types != null && validator.types.types.size() > func.type,
                "type %d does not exist", func.type);
        TypeNode type = validator.types.types.get(func.type);
        popVs(type.params);
        pushVs(type.returns);
        bumpI();
    }

    @Override
    public void visitCallIndirectInsn(int table, int type) {
        super.visitCallIndirectInsn(table, type);
        assertMsg(validator.tables != null && validator.tables.tables != null && validator.tables.tables.size() > table,
                "table %d does not exist", table);
        TableNode tab = validator.tables.tables.get(table);
        assertMsg(tab.type == FUNCREF, "table type must be funcref");
        assertMsg(validator.types != null && validator.types.types != null && validator.types.types.size() > type,
                "type %d does not exist", type);
        TypeNode tn = validator.types.types.get(type);
        popV(I32);
        popVs(tn.params);
        pushVs(tn.returns);
        bumpI();
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        assertMsg(ctrls.isEmpty(), "not all blocks have been ended");
    }
}
