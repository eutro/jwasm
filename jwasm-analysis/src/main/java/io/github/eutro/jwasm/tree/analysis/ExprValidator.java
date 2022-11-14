package io.github.eutro.jwasm.tree.analysis;

import io.github.eutro.jwasm.*;
import io.github.eutro.jwasm.attrs.InsnAttributes;
import io.github.eutro.jwasm.attrs.Opcode;
import io.github.eutro.jwasm.attrs.StackType;
import io.github.eutro.jwasm.attrs.VisitTarget;
import io.github.eutro.jwasm.tree.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static io.github.eutro.jwasm.Opcodes.*;
import static io.github.eutro.jwasm.attrs.Opcode.*;
import static io.github.eutro.jwasm.tree.analysis.ModuleValidator.*;

/**
 * An {@link ExprVisitor} that verifies whether the expression is well-formed.
 * <p>
 * Based on the validation algorithm published
 * <a href="https://webassembly.github.io/spec/core/appendix/algorithm.html#algo-valid">here</a>.
 */
public class ExprValidator extends ExprVisitor {

    public static final String TYPE_MISMATCH = "type mismatch";
    public static final String INVALID_LANE_INDEX = "invalid lane index";

    public static class CtrlFrame {
        public int opcode;
        public List<Byte> startTypes, endTypes;
        public int height;
        public boolean unreachable;
    }

    public final List<Byte> vals = new ArrayList<>();
    public final List<CtrlFrame> ctrls = new ArrayList<>();

    protected final VerifCtx ctx;
    protected int insn = 0;

    public ExprValidator(
            VerifCtx ctx,
            List<Byte> expectedType,
            @Nullable ExprVisitor dl
    ) {
        super(dl);
        this.ctx = ctx;
        pushC(END, Collections.emptyList(), expectedType);
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
        return isNum(ty) || isRef(ty) || ty == V128;
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
                throw new ValidationException("Pop underflows current block",
                        typeMismatch());
            }
        }
        return vals.remove(vals.size() - 1);
    }

    private @Nullable Byte popV(Byte expect) {
        Byte actual = popV();
        if (actual != null && expect != null && (byte) actual != expect) {
            throw new ValidationException(String.format("Mismatched types, expected 0x%02x, got 0x%02x", expect, actual),
                    typeMismatch());
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
        List<Byte> popped = new ArrayList<>(types.size());
        for (int i = types.size() - 1; i >= 0; i--) {
            popped.add(popV(types.get(i)));
        }
        Collections.reverse(popped);
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
            throw new ValidationException("Attempted to pop empty control stack", typeMismatch());
        }
        CtrlFrame frame = ctrlsRef(0);
        popVs(frame.endTypes);
        if (vals.size() != frame.height) {
            throw new ValidationException("Stack height does not match frame height", typeMismatch());
        }
        ctrls.remove(ctrls.size() - 1);
        return frame;
    }

    @NotNull
    private static RuntimeException typeMismatch() {
        return new RuntimeException(TYPE_MISMATCH);
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

    private void applyType(@Nullable StackType type) {
        if (type == null) {
            throw new IllegalArgumentException();
        }
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
                assertMsg(ctx.returns != null, "expression does not have a return type");
                popVs(ctx.returns);
                unreachable();
                break;
            case REF_IS_NULL:
                assertMsg1(isRef(popV()), TYPE_MISMATCH,
                        "ref.is_null argument must be a reference type");
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
                assertMsg1(isNum(t1) && isNum(t2), TYPE_MISMATCH,
                        "select arguments must both be numbers");
                assertMsg1(!(t1 != null && t2 != null && (byte) t1 != t2), TYPE_MISMATCH,
                        "select argument types do not match");
                pushV(t1 == null ? t2 : t1);
                break;
            }

            case MEMORY_SIZE:
                assertMemExists();
                pushV(I32);
                break;
            case MEMORY_GROW:
                assertMemExists();
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
            case MEMORY_COPY:
            case MEMORY_FILL:
                assertMemExists();
        }
        InsnAttributes attrs = InsnAttributes.lookupPrefix(opcode);
        checkVisitTarget(VisitTarget.PrefixInsn, attrs, Opcode.prefixOpcode(opcode));
        applyType(Objects.requireNonNull(attrs.getType()));
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
        if (!ctx.refs.contains(function)) {
            if (ctx.funcs.size() > function) {
                throw new ValidationException(String.format("Function reference %d not declared", function),
                        new RuntimeException("undeclared function reference"));
            } else {
                throw new ValidationException("unknown function " + function);
            }
        }
        pushV(FUNCREF);
        bumpI();
    }

    @Override
    public void visitSelectInsn(byte[] type) {
        super.visitSelectInsn(type);
        assertMsg1(type.length == 1, "invalid result arity",
                "the length of type in a select must be 1, (got %d)", type.length);
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
            case LOCAL_GET:
                isLocal = true;
                pushes = true;
                break;
            case LOCAL_SET:
                isLocal = true;
                pops = true;
                break;
            case LOCAL_TEE:
                isLocal = true;
                pops = true;
                pushes = true;
                break;
            case GLOBAL_GET:
                isLocal = false;
                pushes = true;
                break;
            case GLOBAL_SET:
                isLocal = false;
                pops = true;
                break;
            default:
                throw new ValidationException(String.format("0x%02x is not a valid variable insn", opcode), null);
        }
        if (isLocal) {
            assertExists(ctx.locals, variable, "local");
            type = ctx.locals.get(variable);
        } else {
            assertExists(ctx.globals, variable, "global");
            GlobalTypeNode gn = ctx.globals.get(variable);
            if (pops) {
                assertMsg1(gn.mut == MUT_VAR, "global is immutable",
                        "global %d is not mutable", variable);
            }
            type = gn.type;
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
        assertExists(ctx.tables, table, "table");
        TableNode tn = ctx.tables.get(table);
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
            assertExists(ctx.elems, table, "elem segment");
        } else {
            assertExists(ctx.tables, table, "table");
            TableNode tn = ctx.tables.get(table);
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
                    break;
                default: {
                    Opcode opc = prefixOpcode(opcode);
                    InsnAttributes attrs = InsnAttributes.lookup(opc);
                    checkVisitTarget(VisitTarget.PrefixTableInsn, attrs, opc);
                    throw new IllegalStateException(attrs.getMnemonic());
                }
            }
        }
        bumpI();
    }

    @Override
    public void visitPrefixBinaryTableInsn(int opcode, int firstIndex, int secondIndex) {
        super.visitPrefixBinaryTableInsn(opcode, firstIndex, secondIndex);
        switch (opcode) {
            case TABLE_INIT:
                assertExists(ctx.tables, firstIndex, "table");
                assertExists(ctx.elems, secondIndex, "element");
                assertMsg1(ctx.tables.get(firstIndex).type == ctx.elems.get(secondIndex), TYPE_MISMATCH,
                        "element and table types don't match");
                popVs(I32, I32, I32);
                break;
            case TABLE_COPY:
                assertExists(ctx.tables, firstIndex, "table");
                assertExists(ctx.tables, secondIndex, "table");
                assertMsg1(ctx.tables.get(firstIndex).type == ctx.tables.get(secondIndex).type, TYPE_MISMATCH,
                        "table types don't match");
                popVs(I32, I32, I32);
                break;
            default:
                throw new ValidationException(String.format("0x%02x %d is not a valid table insn", INSN_PREFIX, opcode), null);
        }
        bumpI();
    }

    private void assertMemExists() {
        assertExists(ctx.mems, 0, "memory");
    }

    @Override
    public void visitMemInsn(byte opcode, int align, int offset) {
        super.visitMemInsn(opcode, align, offset);
        assertMemExists();
        checkAlign(byteOpcode(opcode), align);
        InsnAttributes attrs = InsnAttributes.lookup(opcode);
        checkVisitTarget(VisitTarget.MemInsn, attrs, byteOpcode(opcode));
        applyType(Objects.requireNonNull(attrs.getType()));
        bumpI();
    }

    private void checkAlign(Opcode opcode, int align) {
        int memBits = InsnAttributes.lookup(opcode).getMemBits();
        if (memBits == -1) throw new IllegalStateException();
        // 2^align > n/8
        // => align > lg(n/8)
        if (align > 64
                || (1L << align) > (memBits / 8)) {
            throw new ValidationException(
                    String.format("Alignment (2^%d) exceeds natural alignment (%d)", align, memBits / 8),
                    new RuntimeException("alignment must not be larger than natural"));
        }
    }

    @Override
    public void visitIndexedMemInsn(int opcode, int index) {
        super.visitIndexedMemInsn(opcode, index);
        if (opcode == MEMORY_INIT) {
            assertMemExists();
        }
        assertExists(ctx.datas, index, "data segment");
        InsnAttributes attrs = InsnAttributes.lookupPrefix(opcode);
        checkVisitTarget(VisitTarget.IndexedMemInsn, attrs, prefixOpcode(opcode));
        applyType(Objects.requireNonNull(attrs.getType()));
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
            assertMsg(blockType.get() < ctx.types.size(),
                    "Block type %d does not exist", blockType.get());
            TypeNode funcTy = ctx.types.get(blockType.get());
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
        assertExists(ctrls, label, "label");
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
        assertExists(ctrls, defaultLabel, "label");
        popV(I32);
        List<Byte> defaultLabelTys = labelTypes(ctrlsRef(defaultLabel));
        int arity = defaultLabelTys.size();
        for (int n : labels) {
            assertExists(ctrls, n, "label");
            List<Byte> labelTys = labelTypes(ctrlsRef(n));
            assertMsg1(labelTys.size() == arity, TYPE_MISMATCH,
                    "block type mismatch");
            pushVs(popVs(labelTys));
        }
        popVs(defaultLabelTys);
        unreachable();
        bumpI();
    }

    @Override
    public void visitCallInsn(int function) {
        super.visitCallInsn(function);
        assertExists(ctx.funcs, function, "function");
        TypeNode type = ctx.funcs.get(function);
        popVs(type.params);
        pushVs(type.returns);
        bumpI();
    }

    @Override
    public void visitCallIndirectInsn(int table, int type) {
        super.visitCallIndirectInsn(table, type);
        assertExists(ctx.tables, table, "table");
        TableNode tab = ctx.tables.get(table);
        assertMsg(tab.type == FUNCREF, "table type must be funcref");
        assertExists(ctx.types, type, "type");
        TypeNode tn = ctx.types.get(type);
        popV(I32);
        popVs(tn.params);
        pushVs(tn.returns);
        bumpI();
    }

    private InsnAttributes checkVectorAgainstAttrs(int opcode, VisitTarget target) {
        Opcode opc = vectorOpcode(opcode);
        InsnAttributes attrs = InsnAttributes.lookup(opc);
        checkVisitTarget(target, attrs, opc);
        applyType(attrs.getType());
        return attrs;
    }

    private static void checkVisitTarget(VisitTarget target, InsnAttributes attrs, Opcode opcode) {
        if (attrs == null || attrs.getVisitTarget() != target) {
            throw new ValidationException("Unrecognised " + target + " instruction " +
                    (attrs == null ? opcode : attrs.getMnemonic()));
        }
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
        checkAlign(vectorOpcode(opcode), align);
    }

    @Override
    public void visitVectorMemLaneInsn(int opcode, int align, int offset, byte lane) {
        super.visitVectorMemLaneInsn(opcode, align, offset, lane);
        assertMemExists();
        InsnAttributes attrs = checkVectorAgainstAttrs(opcode, VisitTarget.VectorMemLaneInsn);
        checkAlign(vectorOpcode(opcode), align);
        checkLane(lane, attrs);
    }

    @Override
    public void visitVectorConstOrShuffleInsn(int opcode, byte[] bytes) {
        super.visitVectorConstOrShuffleInsn(opcode, bytes);
        checkVectorAgainstAttrs(opcode, VisitTarget.VectorConstOrShuffleInsn);
        if (opcode == I8X16_SHUFFLE) {
            for (byte lane : bytes) {
                if (Byte.toUnsignedInt(lane) >= 32) {
                    throw new ValidationException("All 18x16.shuffle lanes must be less than 32",
                            new RuntimeException(INVALID_LANE_INDEX));
                }
            }
        }
    }

    @Override
    public void visitVectorLaneInsn(int opcode, byte lane) {
        super.visitVectorLaneInsn(opcode, lane);
        InsnAttributes attrs = checkVectorAgainstAttrs(opcode, VisitTarget.VectorLaneInsn);
        checkLane(lane, attrs);
    }

    private static void checkLane(byte lane, InsnAttributes attrs) {
        if (Byte.toUnsignedInt(lane) >= attrs.getVectorShape().dim) {
            throw new ValidationException("Lane index must be smaller than vector dimension",
                    new RuntimeException(INVALID_LANE_INDEX));
        }
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        assertMsg(ctrls.isEmpty(), "not all blocks have been ended");
    }
}
