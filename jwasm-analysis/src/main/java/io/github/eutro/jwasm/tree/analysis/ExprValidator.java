package io.github.eutro.jwasm.tree.analysis;

import io.github.eutro.jwasm.*;
import io.github.eutro.jwasm.attrs.InsnAttributes;
import io.github.eutro.jwasm.attrs.Opcode;
import io.github.eutro.jwasm.attrs.StackType;
import io.github.eutro.jwasm.attrs.VisitTarget;
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
public class ExprValidator extends ExprVisitor {
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

    private void applyType(StackType type) {
        popVs(type.pops);
        pushVs(type.pushes);
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
                InsnAttributes attrs = InsnAttributes.lookup(opcode);
                if (attrs == null || attrs.getVisitTarget() != VisitTarget.Insn || attrs.getType() == null) {
                    throw new ValidationException(String.format("0x%02x is not a valid no-immediate instruction", opcode), null);
                }
                applyType(attrs.getType());
                break;
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

    private void assertMemExists() {
        assertMsg(validator.mems != null && validator.mems.memories != null && !validator.mems.memories.isEmpty(),
                "memory %d does not exist", 0);
    }

    @Override
    public void visitMemInsn(byte opcode, int align, int offset) {
        super.visitMemInsn(opcode, align, offset);
        assertMemExists();
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
    public void visitBlockInsn(byte opcode, BlockType blockType) {
        super.visitBlockInsn(opcode, blockType);
        List<Byte> inputs, outputs;
        if (blockType.isValtype()) {
            switch (blockType.get()) {
                case EMPTY_TYPE:
                    inputs = Collections.emptyList();
                    outputs = Collections.emptyList();
                    break;
                case I32:
                case I64:
                case F32:
                case F64:
                case V128:
                case FUNCREF:
                case EXTERNREF:
                    inputs = Collections.emptyList();
                    outputs = Collections.singletonList((byte) blockType.get());
                    break;
                default:
                    throw new ValidationException(String.format("Block type 0x%02x does not exist.", blockType.get()), null);
            }
        } else {
            assertMsg(validator.types != null && validator.types.types != null && blockType.get() < validator.types.types.size(),
                    "Block type %d does not exist", blockType.get());
            TypeNode funcTy = validator.types.types.get(blockType.get());
            inputs = new ByteList(funcTy.params);
            outputs = new ByteList(funcTy.returns);
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

    private InsnAttributes checkVectorAgainstAttrs(int opcode, VisitTarget target) {
        InsnAttributes attrs = InsnAttributes.lookup(new Opcode(VECTOR_PREFIX, opcode));
        if (attrs == null || attrs.getVisitTarget() != target) {
            throw new ValidationException("Unrecognised " + target + " instruction");
        }
        assert attrs.getType() != null;
        applyType(attrs.getType());
        return attrs;
    }

    @Override
    public void visitVectorInsn(int opcode) {
        super.visitVectorInsn(opcode);
        checkVectorAgainstAttrs(opcode, VisitTarget.VectorInsn);
    }

    @Override
    public void visitVectorMemInsn(int opcode, int align, int offset) {
        super.visitVectorMemInsn(opcode, align, offset);
        assertMemExists();
        checkVectorAgainstAttrs(opcode, VisitTarget.VectorMemInsn);
    }

    @Override
    public void visitVectorMemLaneInsn(int opcode, int align, int offset, byte lane) {
        super.visitVectorMemLaneInsn(opcode, align, offset, lane);
        assertMemExists();
        checkVectorAgainstAttrs(opcode, VisitTarget.VectorMemLaneInsn);
    }

    @Override
    public void visitVectorConstOrShuffleInsn(int opcode, byte[] bytes) {
        super.visitVectorConstOrShuffleInsn(opcode, bytes);
        checkVectorAgainstAttrs(opcode, VisitTarget.VectorConstOrShuffleInsn);
        if (opcode == I8X16_SHUFFLE) {
            for (byte lane : bytes) {
                if (Byte.toUnsignedInt(lane) >= 32) {
                    throw new ValidationException("All 18x16.shuffle lanes must be less than 32");
                }
            }
        }
    }

    @Override
    public void visitVectorLaneInsn(int opcode, byte lane) {
        super.visitVectorLaneInsn(opcode, lane);
        InsnAttributes attrs = checkVectorAgainstAttrs(opcode, VisitTarget.VectorLaneInsn);
        if (lane >= attrs.getVectorShape().dim) {
            throw new ValidationException("Lane index must be smaller than vector dimension");
        }
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        assertMsg(ctrls.isEmpty(), "not all blocks have been ended");
    }
}
