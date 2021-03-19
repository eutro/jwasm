package io.github.eutro.jwasm2java;

import io.github.eutro.jwasm.Opcodes;
import io.github.eutro.jwasm.tree.AbstractInsnNode;
import io.github.eutro.jwasm.tree.*;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.*;

import java.lang.invoke.MethodHandle;
import java.util.LinkedList;

import static io.github.eutro.jwasm.Opcodes.*;
import static io.github.eutro.jwasm2java.Util.*;
import static org.objectweb.asm.Opcodes.*;

public class ExprAdapter {
    public static void translateInto(ExprNode expr, Context context) {
        for (AbstractInsnNode insn : expr) {
            Rule<AbstractInsnNode> rule = getRule(insn);
            if (rule == null) {
                throw new UnsupportedOperationException(String.format("Opcode 0x%02x not supported", insn.opcode));
            }
            rule.apply(context, insn);
        }
    }

    private static final Rule<?>[] RULES = new Rule[Byte.MAX_VALUE - Byte.MIN_VALUE];

    static {
        Rule<?>[] PREFIX_RULES = new Rule[TABLE_FILL + 1];
        ExprAdapter.putRule(INSN_PREFIX, new Rule<PrefixInsnNode>() {
            @Override
            public void apply(Context ctx, PrefixInsnNode insn) {
                apply(PREFIX_RULES[insn.intOpcode], ctx, insn);
            }

            @SuppressWarnings("unchecked")
            private <T> void apply(Rule<T> rule, Context ctx, PrefixInsnNode insn) {
                rule.apply(ctx, (T) insn);
            }
        });

        // region Control
        putRule(UNREACHABLE, (ctx, insn) -> ctx.throwException(Type.getType(AssertionError.class), "Unreachable"));
        putRule(Opcodes.NOP, (ctx, insn) -> { /* NOP */ });
        ExprAdapter.<BlockInsnNode>putRule(BLOCK, (ctx, insn) -> ctx.pushBlock(new Block.BBlock(insn.blockType)));
        ExprAdapter.<BlockInsnNode>putRule(LOOP, (ctx, insn) ->
                ctx.pushBlock(new Block.Loop(insn.blockType, ctx.mark())));
        ExprAdapter.<BlockInsnNode>putRule(IF, (ctx, insn) -> {
            ctx.expectType(Type.INT_TYPE);
            Block.If block = new Block.If(insn.blockType, new LinkedList<>(ctx.stack));
            ctx.visitJumpInsn(IFEQ, block.elseLabel);
            ctx.pushBlock(block);
        });
        putRule(ELSE, (ctx, insn) -> {
            Block.If ifBlock = (Block.If) ctx.peekBlock();
            ctx.goTo(ifBlock.endLabel());
            ctx.stack = ifBlock.types;
            ctx.mark(ifBlock.elseLabel);
        });
        putRule(END, (ctx, insn) -> {
            if (ctx.peekBlock() != null) {
                ctx.popBlock().end(ctx);
            }
        });
        ExprAdapter.<BreakInsnNode>putRule(BR, (ctx, insn) -> ctx.goTo(ctx.getLabel(insn.label)));
        ExprAdapter.<BreakInsnNode>putRule(BR_IF, (ctx, insn) -> ctx.expectType(Type.INT_TYPE)
                .visitJumpInsn(IFNE, ctx.getLabel(insn.label)));
        putRule(Opcodes.RETURN, (ctx, insn) -> {
            for (byte retType : ctx.funcType.returns) {
                ctx.expectType(Types.toJava(retType));
            }
            ctx.compress(ctx.funcType.returns).returnValue();
        });
        ExprAdapter.<CallInsnNode>putRule(CALL, (ctx, insn) -> {
            FuncExtern func = ctx.externs.funcs.get(insn.function);
            Type fType = Types.methodDesc(func.type());
            for (Type argType : fType.getArgumentTypes()) {
                ctx.expectType(argType);
            }
            ctx.pushType(fType.getReturnType());
            func.emitInvoke(ctx);
        });
        ExprAdapter.<CallIndirectInsnNode>putRule(CALL_INDIRECT, (ctx, insn) -> {
            Type fType = ctx.funcType(insn.type);
            for (Type argType : fType.getArgumentTypes()) {
                ctx.expectType(argType);
            }
            ctx.pushType(fType.getReturnType());
            ctx.externs.tables.get(insn.table).emitGet(ctx);
            ctx.swap();
            Type mhType = Type.getType(MethodHandle.class);
            ctx.arrayLoad(mhType);
            ctx.invokeVirtual(mhType, new Method("invokeExact", fType.toString()));
        });
        // endregion
        // region Reference
        putRule(REF_NULL, (ctx, insn) -> ctx.pushType(Type.getType(Object.class)).push((String) null));
        putRule(REF_IS_NULL, (ctx, insn) -> ctx.remType().pushType(Type.INT_TYPE).jumpStack(IFNULL));
        ExprAdapter.<FuncInsnNode>putRule(REF_FUNC, (ctx, insn) -> ctx.pushType(Type.getType(MethodHandle.class))
                .externs.funcs.get(insn.function).emitGet(ctx));
        // endregion
        // region Parametric
        putRule(DROP, (ctx, insn) -> {
            if (ctx.popType().getSize() == 2) {
                ctx.pop2();
            } else {
                ctx.pop();
            }
        });
        Rule<AbstractInsnNode> select = (ctx, insn) -> {
            ctx.expectType(Type.INT_TYPE);
            Type type = ctx.popType();
            if (!type.equals(ctx.peekType())) throw new IllegalArgumentException();
            // WASM: if the top stack value is not 0, keep the bottom value, otherwise the top value.
            // JVM: if the top stack value is not zero, pop the top value, otherwise swap before popping
            Label end = new Label();
            ctx.ifZCmp(GeneratorAdapter.NE, end);
            ctx.swap(type, type);
            ctx.visitLabel(end);
            if (type.getSize() == 2) {
                ctx.pop2();
            } else {
                ctx.pop();
            }
        };
        putRule(SELECT, select);
        putRule(SELECTT, select);
        // endregion
        // region Variable
        ExprAdapter.<VariableInsnNode>putRule(LOCAL_GET, (ctx, insn) -> ctx
                .pushType(ctx.localType(insn.index))
                .visitVarInsn(ctx.localType(insn.index).getOpcode(ILOAD), ctx.localIndex(insn.index)));
        ExprAdapter.<VariableInsnNode>putRule(LOCAL_SET, (ctx, insn) -> ctx
                .expectType(ctx.localType(insn.index))
                .visitVarInsn(ctx.localType(insn.index).getOpcode(ISTORE), ctx.localIndex(insn.index)));
        ExprAdapter.<VariableInsnNode>putRule(LOCAL_TEE, (ctx, insn) -> {
            Type type = ctx.localType(insn.index);
            ctx.expectType(type).pushType(type);
            if (type.getSize() == 2) {
                ctx.dup2();
            } else {
                ctx.dup();
            }
            ctx.visitVarInsn(type.getOpcode(ISTORE), ctx.localIndex(insn.index));
        });
        ExprAdapter.<VariableInsnNode>putRule(GLOBAL_GET, (ctx, insn) -> ctx.externs.globals.get(insn.index).emitGet(ctx));
        ExprAdapter.<VariableInsnNode>putRule(GLOBAL_SET, (ctx, insn) -> ctx.externs.globals.get(insn.index).emitSet(ctx));
        // endregion
        // region Table
        ExprAdapter.<TableInsnNode>putRule(TABLE_GET, (ctx, insn) -> {
            ctx.expectType(Type.INT_TYPE);
            TableExtern table = ctx.externs.tables.get(insn.table);
            table.emitGet(ctx);
            ctx.swap();
            ctx.arrayLoad(Types.toJava(table.type()));
        });
        ExprAdapter.<TableInsnNode>putRule(TABLE_SET, (ctx, insn) -> {
            TableExtern table = ctx.externs.tables.get(insn.table);
            Type valType = Types.toJava(table.type());
            ctx.expectType(valType).expectType(Type.INT_TYPE);
            table.emitGet(ctx);
            ctx.dupX2();
            ctx.pop();
            ctx.arrayStore(valType);
        });
        PREFIX_RULES[TABLE_SIZE] = (Rule<PrefixTableInsnNode>) (ctx, insn) -> {
            ctx.pushType(Type.INT_TYPE);
            TableExtern table = ctx.externs.tables.get(insn.table);
            table.emitGet(ctx);
            ctx.arrayLength();
        };
        PREFIX_RULES[TABLE_GROW] = (Rule<PrefixTableInsnNode>) (ctx, insn) -> {
            // TODO maybe implement table growth?
            ctx.expectType(Type.INT_TYPE).remType();
            ctx.pop2();
            ctx.push(-1);
        };
        // endregion
        // region Memory
        // region Load
        ExprAdapter.<MemInsnNode>putRule(I32_LOAD, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(offsetFor(insn))
                .addInsns(getMem(ctx))
                .addInsns(new InsnNode(SWAP), virtualNode("java/nio/ByteBuffer", "getInt", "(I)I")));
        ExprAdapter.<MemInsnNode>putRule(I64_LOAD, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).pushType(Type.LONG_TYPE)
                .addInsns(offsetFor(insn))
                .addInsns(getMem(ctx))
                .addInsns(new InsnNode(SWAP), virtualNode("java/nio/ByteBuffer", "getLong", "(I)J")));
        ExprAdapter.<MemInsnNode>putRule(F32_LOAD, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).pushType(Type.FLOAT_TYPE)
                .addInsns(offsetFor(insn))
                .addInsns(getMem(ctx))
                .addInsns(new InsnNode(SWAP), virtualNode("java/nio/ByteBuffer", "getFloat", "(I)F")));
        ExprAdapter.<MemInsnNode>putRule(F64_LOAD, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).pushType(Type.DOUBLE_TYPE)
                .addInsns(offsetFor(insn))
                .addInsns(getMem(ctx))
                .addInsns(new InsnNode(SWAP), virtualNode("java/nio/ByteBuffer", "getDouble", "(I)D")));
        // endregion
        // region Store
        ExprAdapter.<MemInsnNode>putRule(I32_STORE, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE)
                .addInsns(new InsnNode(SWAP))
                .addInsns(offsetFor(insn))
                .addInsns(new InsnNode(SWAP))
                .addInsns(getMem(ctx))
                .addInsns(new InsnNode(DUP_X2), new InsnNode(POP),
                        virtualNode("java/nio/ByteBuffer", "putInt", "(II)Ljava/nio/ByteBuffer;"),
                        new InsnNode(POP)));
        ExprAdapter.<MemInsnNode>putRule(I64_STORE, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.INT_TYPE)
                .addInsns(new InsnNode(DUP2_X1), new InsnNode(POP2))
                .addInsns(offsetFor(insn))
                .addInsns(getMem(ctx))
                .addInsns(new InsnNode(SWAP), new InsnNode(DUP2_X1), new InsnNode(POP2),
                        virtualNode("java/nio/ByteBuffer", "putLong", "(IJ)Ljava/nio/ByteBuffer;"),
                        new InsnNode(POP)));
        ExprAdapter.<MemInsnNode>putRule(F32_STORE, (ctx, insn) -> ctx.expectType(Type.FLOAT_TYPE).expectType(Type.INT_TYPE)
                .addInsns(new InsnNode(SWAP))
                .addInsns(offsetFor(insn))
                .addInsns(new InsnNode(SWAP))
                .addInsns(getMem(ctx))
                .addInsns(new InsnNode(DUP_X2), new InsnNode(POP),
                        virtualNode("java/nio/ByteBuffer", "putInt", "(IF)Ljava/nio/ByteBuffer;"),
                        new InsnNode(POP)));
        ExprAdapter.<MemInsnNode>putRule(F64_STORE, (ctx, insn) -> ctx.expectType(Type.DOUBLE_TYPE).expectType(Type.INT_TYPE)
                .addInsns(new InsnNode(DUP2_X1), new InsnNode(POP2))
                .addInsns(offsetFor(insn))
                .addInsns(getMem(ctx))
                .addInsns(new InsnNode(SWAP), new InsnNode(DUP2_X1), new InsnNode(POP2),
                        virtualNode("java/nio/ByteBuffer", "putDouble", "(ID)Ljava/nio/ByteBuffer;"),
                        new InsnNode(POP)));
        // endregion
        // endregion
        // region Numeric
        Type[] ptypes = { Type.INT_TYPE, Type.LONG_TYPE, Type.FLOAT_TYPE, Type.DOUBLE_TYPE };
        // region Const
        byte[] consts = { I32_CONST, I64_CONST, F32_CONST, F64_CONST };
        for (int i = 0; i < consts.length; i++) {
            Type type = ptypes[i];
            ExprAdapter.<ConstInsnNode>putRule(consts[i], (ctx, insn) -> ctx.pushType(type).addInsns(constant(insn.value)));
        }
        // endregion
        // region Comparisons
        // region i32
        MethodInsnNode compareUnsignedI = staticNode("java/lang/Integer", "compareUnsigned", "(II)I");
        putRule(I32_EQZ, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).pushType(Type.INT_TYPE).jumpStack(IFEQ));
        putRule(I32_EQ, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .jumpStack(IF_ICMPEQ));
        putRule(I32_NE, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .jumpStack(IF_ICMPNE));
        putRule(I32_LT_S, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .jumpStack(IF_ICMPLT));
        putRule(I32_LT_U, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(compareUnsignedI.clone(null)).jumpStack(IFLT));
        putRule(I32_GT_S, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .jumpStack(IF_ICMPGT));
        putRule(I32_GT_U, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(compareUnsignedI.clone(null)).jumpStack(IFGT));
        putRule(I32_LE_S, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .jumpStack(IF_ICMPLE));
        putRule(I32_LE_U, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(compareUnsignedI.clone(null)).jumpStack(IFLE));
        putRule(I32_GE_S, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .jumpStack(IF_ICMPGE));
        putRule(I32_GE_U, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(compareUnsignedI.clone(null)).jumpStack(IFGE));
        // endregion
        // region i64
        MethodInsnNode compareUnsignedL = staticNode("java/lang/Long", "compareUnsigned", "(JJ)I");
        putRule(I64_EQZ, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(L2I)).jumpStack(IFEQ));
        putRule(I64_EQ, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(LCMP)).jumpStack(IFEQ));
        putRule(I64_NE, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(LCMP)).jumpStack(IFNE));
        putRule(I64_LT_S, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(LCMP)).jumpStack(IFLT));
        putRule(I64_LT_U, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.INT_TYPE)
                .addInsns(compareUnsignedL.clone(null)).jumpStack(IFLT));
        putRule(I64_GT_S, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(LCMP)).jumpStack(IFGT));
        putRule(I64_GT_U, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.INT_TYPE)
                .addInsns(compareUnsignedL.clone(null)).jumpStack(IFGT));
        putRule(I64_LE_S, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(LCMP)).jumpStack(IFLE));
        putRule(I64_LE_U, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.INT_TYPE)
                .addInsns(compareUnsignedL.clone(null)).jumpStack(IFLE));
        putRule(I64_GE_S, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(LCMP)).jumpStack(IFGE));
        putRule(I64_GE_U, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.INT_TYPE)
                .addInsns(compareUnsignedL.clone(null)).jumpStack(IFGE));
        // endregion
        // region f32
        putRule(F32_EQ, (ctx, insn) -> ctx.expectType(Type.FLOAT_TYPE).expectType(Type.FLOAT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(FCMPG)).jumpStack(IFEQ));
        putRule(F32_NE, (ctx, insn) -> ctx.expectType(Type.FLOAT_TYPE).expectType(Type.FLOAT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(FCMPG)).jumpStack(IFNE));
        putRule(F32_LT, (ctx, insn) -> ctx.expectType(Type.FLOAT_TYPE).expectType(Type.FLOAT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(FCMPG)).jumpStack(IFLT));
        putRule(F32_GT, (ctx, insn) -> ctx.expectType(Type.FLOAT_TYPE).expectType(Type.FLOAT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(FCMPL)).jumpStack(IFGT));
        putRule(F32_LE, (ctx, insn) -> ctx.expectType(Type.FLOAT_TYPE).expectType(Type.FLOAT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(FCMPG)).jumpStack(IFLE));
        putRule(F32_GE, (ctx, insn) -> ctx.expectType(Type.FLOAT_TYPE).expectType(Type.FLOAT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(FCMPL)).jumpStack(IFGE));
        // endregion
        // region f64
        putRule(F64_EQ, (ctx, insn) -> ctx.expectType(Type.DOUBLE_TYPE).expectType(Type.DOUBLE_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(DCMPG)).jumpStack(IFEQ));
        putRule(F64_NE, (ctx, insn) -> ctx.expectType(Type.DOUBLE_TYPE).expectType(Type.DOUBLE_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(DCMPG)).jumpStack(IFNE));
        putRule(F64_LT, (ctx, insn) -> ctx.expectType(Type.DOUBLE_TYPE).expectType(Type.DOUBLE_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(DCMPG)).jumpStack(IFLT));
        putRule(F64_GT, (ctx, insn) -> ctx.expectType(Type.DOUBLE_TYPE).expectType(Type.DOUBLE_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(DCMPL)).jumpStack(IFGT));
        putRule(F64_LE, (ctx, insn) -> ctx.expectType(Type.DOUBLE_TYPE).expectType(Type.DOUBLE_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(DCMPG)).jumpStack(IFLE));
        putRule(F64_GE, (ctx, insn) -> ctx.expectType(Type.DOUBLE_TYPE).expectType(Type.DOUBLE_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(DCMPL)).jumpStack(IFGE));
        // endregion
        // endregion
        // region Mathematical
        // region i32
        putRule(I32_CLZ, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(staticNode("java/lang/Integer", "numberOfLeadingZeros", "(I)I")));
        putRule(I32_CTZ, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(staticNode("java/lang/Integer", "numberOfTrailingZeros", "(I)I")));
        putRule(I32_POPCNT, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(staticNode("java/lang/Integer", "numberOfTrailingZeros", "(I)I")));
        putRule(I32_ADD, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(IADD)));
        putRule(I32_SUB, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(ISUB)));
        putRule(I32_MUL, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(IMUL)));
        putRule(I32_DIV_S, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(IDIV)));
        putRule(I32_DIV_U, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(staticNode("java/lang/Integer", "divideUnsigned", "(II)I")));
        putRule(I32_REM_S, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(IREM)));
        putRule(I32_REM_U, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(staticNode("java/lang/Integer", "remainderUnsigned", "(II)I")));
        putRule(I32_AND, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(IAND)));
        putRule(I32_OR, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(IOR)));
        putRule(I32_XOR, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(IXOR)));
        putRule(I32_SHL, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(ISHL)));
        putRule(I32_SHR_S, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(ISHR)));
        putRule(I32_SHR_U, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(new InsnNode(IUSHR)));
        putRule(I32_ROTL, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(staticNode("java/lang/Integer", "rotateLeft", "(II)I")));
        putRule(I32_ROTR, (ctx, insn) -> ctx.expectType(Type.INT_TYPE).expectType(Type.INT_TYPE).pushType(Type.INT_TYPE)
                .addInsns(staticNode("java/lang/Integer", "rotateRight", "(II)I")));
        // endregion
        // region i64
        putRule(I64_CLZ, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).pushType(Type.LONG_TYPE)
                .addInsns(makeList(staticNode("java/lang/Long", "numberOfLeadingZeros", "(J)I"), new InsnNode(I2L))));
        putRule(I64_CTZ, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).pushType(Type.LONG_TYPE)
                .addInsns(makeList(staticNode("java/lang/Long", "numberOfTrailingZeros", "(J)I"), new InsnNode(I2L))));
        putRule(I64_POPCNT, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).pushType(Type.LONG_TYPE)
                .addInsns(makeList(staticNode("java/lang/Long", "bitCount", "(J)I"), new InsnNode(I2L))));
        putRule(I64_ADD, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.LONG_TYPE)
                .addInsns(new InsnNode(LADD)));
        putRule(I64_SUB, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.LONG_TYPE)
                .addInsns(new InsnNode(LSUB)));
        putRule(I64_MUL, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.LONG_TYPE)
                .addInsns(new InsnNode(LMUL)));
        putRule(I64_DIV_S, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.LONG_TYPE)
                .addInsns(new InsnNode(LDIV)));
        putRule(I64_DIV_U, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.LONG_TYPE)
                .addInsns(staticNode("java/lang/Long", "divideUnsigned", "(JJ)J")));
        putRule(I64_REM_S, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.LONG_TYPE)
                .addInsns(new InsnNode(LREM)));
        putRule(I64_REM_U, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.LONG_TYPE)
                .addInsns(staticNode("java/lang/Long", "remainderUnsigned", "(JJ)J")));
        putRule(I64_AND, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.LONG_TYPE)
                .addInsns(new InsnNode(LAND)));
        putRule(I64_OR, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.LONG_TYPE)
                .addInsns(new InsnNode(LOR)));
        putRule(I64_XOR, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.LONG_TYPE)
                .addInsns(new InsnNode(LXOR)));
        putRule(I64_SHL, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.LONG_TYPE)
                .addInsns(new InsnNode(LSHL)));
        putRule(I64_SHR_S, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.LONG_TYPE)
                .addInsns(new InsnNode(LSHR)));
        putRule(I64_SHR_U, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.LONG_TYPE)
                .addInsns(new InsnNode(LUSHR)));
        putRule(I64_ROTL, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.LONG_TYPE)
                .addInsns(new InsnNode(L2I), staticNode("java/lang/Long", "rotateLeft", "(JI)J")));
        putRule(I64_ROTR, (ctx, insn) -> ctx.expectType(Type.LONG_TYPE).expectType(Type.LONG_TYPE).pushType(Type.LONG_TYPE)
                .addInsns(new InsnNode(L2I), staticNode("java/lang/Long", "rotateRight", "(JI)J")));
        // endregion
        // region f32
        putRule(F32_ABS, (ctx, insn) -> ctx.expectType(Type.FLOAT_TYPE).pushType(Type.FLOAT_TYPE)
                .addInsns(staticNode("java/lang/Math", "abs", "(F)F")));
        putRule(F32_NEG, (ctx, insn) -> ctx.expectType(Type.FLOAT_TYPE).pushType(Type.FLOAT_TYPE)
                .addInsns(new InsnNode(FNEG)));
        putRule(F32_CEIL, (ctx, insn) -> ctx.expectType(Type.FLOAT_TYPE).pushType(Type.FLOAT_TYPE)
                .addInsns(
                        new InsnNode(F2D),
                        staticNode("java/lang/Math", "ceil", "(D)D"),
                        new InsnNode(D2F)));
        putRule(F32_FLOOR, (ctx, insn) -> ctx.expectType(Type.FLOAT_TYPE).pushType(Type.FLOAT_TYPE)
                .addInsns(
                        new InsnNode(F2D),
                        staticNode("java/lang/Math", "floor", "(D)D"),
                        new InsnNode(D2F)));
        putRule(F32_TRUNC, (ctx, insn) -> {
            LabelNode els = new LabelNode();
            LabelNode end = new LabelNode();
            ctx.expectType(Type.FLOAT_TYPE).pushType(Type.FLOAT_TYPE)
                    .addInsns(
                            new InsnNode(F2D),
                            new InsnNode(DUP2),
                            new InsnNode(DCMPG),
                            new JumpInsnNode(IFLT, els),
                            staticNode("java/lang/Math", "floor", "(D)D"),
                            new JumpInsnNode(GOTO, end),
                            els,
                            staticNode("java/lang/Math", "ceil", "(D)D"),
                            end,
                            new InsnNode(D2F));
        });
        putRule(F32_NEAREST, (ctx, insn) -> {
            // TODO I can't think of a clean way to do this
            throw new UnsupportedOperationException();
        });
        putRule(F32_SQRT, (ctx, insn) -> ctx.expectType(Type.FLOAT_TYPE).pushType(Type.FLOAT_TYPE)
                .addInsns(
                        new InsnNode(F2D),
                        staticNode("java/lang/Math", "sqrt", "(D)D"),
                        new InsnNode(D2F)));
        putRule(F32_ADD, (ctx, insn) -> ctx.expectType(Type.FLOAT_TYPE).expectType(Type.FLOAT_TYPE).pushType(Type.FLOAT_TYPE)
                .addInsns(new InsnNode(FADD)));
        putRule(F32_SUB, (ctx, insn) -> ctx.expectType(Type.FLOAT_TYPE).expectType(Type.FLOAT_TYPE).pushType(Type.FLOAT_TYPE)
                .addInsns(new InsnNode(FSUB)));
        putRule(F32_MUL, (ctx, insn) -> ctx.expectType(Type.FLOAT_TYPE).expectType(Type.FLOAT_TYPE).pushType(Type.FLOAT_TYPE)
                .addInsns(new InsnNode(FMUL)));
        putRule(F32_DIV, (ctx, insn) -> ctx.expectType(Type.FLOAT_TYPE).expectType(Type.FLOAT_TYPE).pushType(Type.FLOAT_TYPE)
                .addInsns(new InsnNode(FDIV)));
        putRule(F32_MIN, (ctx, insn) -> ctx.expectType(Type.FLOAT_TYPE).expectType(Type.FLOAT_TYPE).pushType(Type.FLOAT_TYPE)
                .addInsns(staticNode("java/lang/Math", "min", "(FF)F")));
        putRule(F32_MAX, (ctx, insn) -> ctx.expectType(Type.FLOAT_TYPE).expectType(Type.FLOAT_TYPE).pushType(Type.FLOAT_TYPE)
                .addInsns(staticNode("java/lang/Math", "max", "(FF)F")));
        putRule(F32_COPYSIGN, (ctx, insn) -> ctx.expectType(Type.FLOAT_TYPE).expectType(Type.FLOAT_TYPE).pushType(Type.FLOAT_TYPE)
                .addInsns(staticNode("java/lang/Math", "copySign", "(FF)F")));
        // endregion
        // region f64
        putRule(F64_ABS, (ctx, insn) -> ctx.expectType(Type.DOUBLE_TYPE).pushType(Type.DOUBLE_TYPE)
                .addInsns(staticNode("java/lang/Math", "abs", "(D)D")));
        putRule(F64_NEG, (ctx, insn) -> ctx.expectType(Type.DOUBLE_TYPE).pushType(Type.DOUBLE_TYPE)
                .addInsns(new InsnNode(DNEG)));
        putRule(F64_CEIL, (ctx, insn) -> ctx.expectType(Type.DOUBLE_TYPE).pushType(Type.DOUBLE_TYPE)
                .addInsns(staticNode("java/lang/Math", "ceil", "(D)D")));
        putRule(F64_FLOOR, (ctx, insn) -> ctx.expectType(Type.DOUBLE_TYPE).pushType(Type.DOUBLE_TYPE)
                .addInsns(staticNode("java/lang/Math", "floor", "(D)D")));
        putRule(F64_TRUNC, (ctx, insn) -> {
            LabelNode els = new LabelNode();
            LabelNode end = new LabelNode();
            ctx.expectType(Type.DOUBLE_TYPE).pushType(Type.DOUBLE_TYPE)
                    .addInsns(
                            new InsnNode(DUP2),
                            new InsnNode(DCMPG),
                            new JumpInsnNode(IFLT, els),
                            staticNode("java/lang/Math", "floor", "(D)D"),
                            new JumpInsnNode(GOTO, end),
                            els,
                            staticNode("java/lang/Math", "ceil", "(D)D"),
                            end);
        });
        putRule(F64_NEAREST, (ctx, insn) -> {
            // TODO I can't think of a clean way to do this
            throw new UnsupportedOperationException();
        });
        putRule(F64_SQRT, (ctx, insn) -> ctx.expectType(Type.DOUBLE_TYPE).pushType(Type.DOUBLE_TYPE)
                .addInsns(staticNode("java/lang/Math", "sqrt", "(D)D")));
        putRule(F64_ADD, (ctx, insn) -> ctx.expectType(Type.DOUBLE_TYPE).expectType(Type.DOUBLE_TYPE).pushType(Type.DOUBLE_TYPE)
                .addInsns(new InsnNode(DADD)));
        putRule(F64_SUB, (ctx, insn) -> ctx.expectType(Type.DOUBLE_TYPE).expectType(Type.DOUBLE_TYPE).pushType(Type.DOUBLE_TYPE)
                .addInsns(new InsnNode(DSUB)));
        putRule(F64_MUL, (ctx, insn) -> ctx.expectType(Type.DOUBLE_TYPE).expectType(Type.DOUBLE_TYPE).pushType(Type.DOUBLE_TYPE)
                .addInsns(new InsnNode(DMUL)));
        putRule(F64_DIV, (ctx, insn) -> ctx.expectType(Type.DOUBLE_TYPE).expectType(Type.DOUBLE_TYPE).pushType(Type.DOUBLE_TYPE)
                .addInsns(new InsnNode(DDIV)));
        putRule(F64_MIN, (ctx, insn) -> ctx.expectType(Type.DOUBLE_TYPE).expectType(Type.DOUBLE_TYPE).pushType(Type.DOUBLE_TYPE)
                .addInsns(staticNode("java/lang/Math", "min", "(DD)D")));
        putRule(F64_MAX, (ctx, insn) -> ctx.expectType(Type.DOUBLE_TYPE).expectType(Type.DOUBLE_TYPE).pushType(Type.DOUBLE_TYPE)
                .addInsns(staticNode("java/lang/Math", "max", "(DD)D")));
        putRule(F64_COPYSIGN, (ctx, insn) -> ctx.expectType(Type.DOUBLE_TYPE).expectType(Type.DOUBLE_TYPE).pushType(Type.DOUBLE_TYPE)
                .addInsns(staticNode("java/lang/Math", "copySign", "(DD)D")));
        // endregion
        // endregion
        // endregion
    }

    @NotNull
    private static InsnList offsetFor(MemInsnNode insn) {
        return insn.offset == 0 ? makeList() : makeList(constant(insn.offset), new InsnNode(IADD));
    }

    @NotNull
    private static InsnList getMem(Context ctx) {
        return fromAdapter(ctx.externs.mems.get(0)::emitGet);
    }

    @SuppressWarnings("unchecked")
    private static <T extends AbstractInsnNode> Rule<T> getRule(T insn) {
        return (Rule<T>) RULES[Byte.toUnsignedInt(insn.opcode)];
    }

    private static <T extends AbstractInsnNode> void putRule(byte opcode, Rule<T> rule) {
        RULES[Byte.toUnsignedInt(opcode)] = rule;
    }

    private interface Rule<T> {
        void apply(Context ctx, T insn);
    }
}
