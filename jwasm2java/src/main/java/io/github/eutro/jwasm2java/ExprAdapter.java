package io.github.eutro.jwasm2java;

import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import static io.github.eutro.jwasm.Opcodes.*;
import static org.objectweb.asm.Opcodes.*;

public class ExprAdapter extends ExprVisitor {
    private final GeneratorAdapter mv;
    private final String internalName;
    private final FuncType type;
    private final byte[] localTypes;
    private final int[] localIndeces;
    private final Function[] functions;
    private final LinkedList<Block> blocks = new LinkedList<>();
    private final LinkedList<Type> stack = new LinkedList<>();

    public ExprAdapter(GeneratorAdapter mv,
                       String internalName,
                       FuncType type,
                       byte[] localTypes,
                       Function[] functions) {
        this.mv = mv;
        this.localTypes = localTypes;
        localIndeces = new int[localTypes.length];
        for (int i = 0; i < localTypes.length; i++) {
            Type localType = Types.toJava(localTypes[i]);
            int local = localIndeces[i] = mv.newLocal(localType);
            if (localType.equals(Type.INT_TYPE)) mv.push(0);
            else if (localType.equals(Type.LONG_TYPE)) mv.push(0L);
            else if (localType.equals(Type.FLOAT_TYPE)) mv.push(0F);
            else if (localType.equals(Type.DOUBLE_TYPE)) mv.push(0D);
            else mv.push((String) null);
            mv.storeLocal(local);
        }
        this.internalName = internalName;
        this.type = type;
        this.functions = functions;
    }

    private Type localType(int index) {
        return Types.toJava(index < type.params.length ? type.params[index] : localTypes[index - type.params.length]);
    }

    private Label getLabel(int label) {
        return blocks.get(label).label();
    }

    protected void doReturn() {
        if (type.returns.length >= 2) {
            throw new UnsupportedOperationException();
        }
        mv.returnValue();
    }

    @Override
    public void visitInsn(byte opcode) {
        switch (opcode) {
            // region Control
            case UNREACHABLE:
                mv.throwException(Type.getType(AssertionError.class), "Unreachable");
                break;
            case Opcodes.NOP:
                mv.visitInsn(org.objectweb.asm.Opcodes.NOP);
                break;
            case Opcodes.RETURN:
                doReturn();
                break;
            // endregion
            // region Parametric
            case DROP:
                if (stack.pop().getSize() == 2) {
                    mv.pop2();
                } else {
                    mv.pop();
                }
                break;
            case SELECT: {
                stack.pop();
                Type type = stack.pop();
                Label end = new Label();
                mv.ifZCmp(GeneratorAdapter.NE, end);
                mv.swap(type, type);
                mv.mark(end);
                if (type.getSize() == 2) {
                    mv.pop2();
                } else {
                    mv.pop();
                }
                break;
            }
            // endregion
            // region Memory
            case MEMORY_SIZE:
                getMemory();
                mv.invokeVirtual(Type.getType(ByteBuffer.class), new Method("capacity", "()I"));
                mv.push(PAGE_SIZE);
                mv.visitInsn(IDIV);
                break;
            case MEMORY_GROW:
                // TODO
                mv.invokeVirtual(Type.getObjectType(internalName), new Method("__resizeMemory", "(I)I"));
                break;
            // endregion
            // region Comparisons
            // region i32
            case I32_EQZ: {
                zcmpBool(GeneratorAdapter.EQ);
                break;
            }
            case I32_EQ:
                cmpBool(GeneratorAdapter.EQ);
                break;
            case I32_NE:
                cmpBool(GeneratorAdapter.NE);
                break;
            case I32_LT_S:
                cmpBool(GeneratorAdapter.LT);
                break;
            case I32_LT_U: {
                intStatic("compareUnsigned", "(II)I");
                zcmpBool(GeneratorAdapter.LT);
                stack.pop();
                break;
            }
            case I32_GT_S:
                cmpBool(GeneratorAdapter.GT);
                break;
            case I32_GT_U:
                intStatic("compareUnsigned", "(II)I");
                zcmpBool(GeneratorAdapter.GT);
                stack.pop();
                break;
            case I32_LE_S:
                cmpBool(GeneratorAdapter.LE);
                break;
            case I32_LE_U:
                intStatic("compareUnsigned", "(II)I");
                zcmpBool(GeneratorAdapter.LE);
                stack.pop();
                break;
            case I32_GE_S:
                cmpBool(GeneratorAdapter.GE);
                break;
            case I32_GE_U:
                intStatic("compareUnsigned", "(II)I");
                zcmpBool(GeneratorAdapter.GE);
                stack.pop();
                break;
            // endregion
            // endregion
            // region Mathematical
            // region i32
            case I32_CLZ:
                // Integer.numberOfLeadingZeros(i)
                intStatic("numberOfLeadingZeros", "(I)I");
                break;
            case I32_CTZ:
                // Integer.numberOfTrailingZeros(i)
                intStatic("numberOfTrailingZeros", "(I)I");
                break;
            case I32_POPCNT:
                // Integer.bitCount(i)
                intStatic("bitCount", "(I)I");
                break;
            case I32_ADD:
                mv.visitInsn(IADD);
                stack.pop();
                break;
            case I32_SUB:
                mv.visitInsn(ISUB);
                stack.pop();
                break;
            case I32_MUL:
                mv.visitInsn(IMUL);
                stack.pop();
                break;
            case I32_DIV_S:
                mv.visitInsn(IDIV);
                stack.pop();
                break;
            case I32_DIV_U:
                // Integer.divideUnsigned(a, b)
                intStatic("divideUnsigned", "(II)I");
                stack.pop();
                break;
            case I32_REM_S:
                mv.visitInsn(IREM);
                stack.pop();
                break;
            case I32_REM_U:
                // Integer.remainderUnsigned(a, b)
                intStatic("remainderUnsigned", "(II)I");
                break;
            case I32_AND:
                mv.visitInsn(IAND);
                stack.pop();
                break;
            case I32_OR:
                mv.visitInsn(IOR);
                stack.pop();
                break;
            case I32_XOR:
                mv.visitInsn(IXOR);
                stack.pop();
                break;
            case I32_SHL:
                mv.visitInsn(ISHL);
                stack.pop();
                break;
            case I32_SHR_S:
                mv.visitInsn(ISHR);
                stack.pop();
                break;
            case I32_SHR_U:
                mv.visitInsn(IUSHR);
                stack.pop();
                break;
            case I32_ROTL:
                // Integer.rotateLeft(a, b)
                intStatic("rotateLeft", "(II)I");
                stack.pop();
                break;
            case I32_ROTR:
                // Integer.rotateRight(a, b)
                intStatic("rotateRight", "(II)I");
                stack.pop();
                break;
            // endregion
            // region i64
            case I64_CLZ:
                // Long.numberOfLeadingZeros(i)
                longStatic("numberOfLeadingZeros", "(J)I");
                mv.visitInsn(I2L);
                break;
            case I64_CTZ:
                // Long.numberOfTrailingZeros(i)
                longStatic("numberOfTrailingZeros", "(J)I");
                mv.visitInsn(I2L);
                break;
            case I64_POPCNT:
                // Long.bitCount(i)
                longStatic("bitCount", "(J)I");
                mv.visitInsn(I2L);
                break;
            case I64_ADD:
                mv.visitInsn(LADD);
                stack.pop();
                break;
            case I64_SUB:
                mv.visitInsn(LSUB);
                stack.pop();
                break;
            case I64_MUL:
                mv.visitInsn(LMUL);
                stack.pop();
                break;
            case I64_DIV_S:
                mv.visitInsn(LDIV);
                stack.pop();
                break;
            case I64_DIV_U:
                // Long.divideUnsigned(a, b)
                longStatic("divideUnsigned", "(JJ)J");
                stack.pop();
                break;
            case I64_REM_S:
                mv.visitInsn(LREM);
                stack.pop();
                break;
            case I64_REM_U:
                // Long.remainderUnsigned(a, b)
                longStatic("remainderUnsigned", "(JJ)J");
                stack.pop();
                break;
            case I64_AND:
                mv.visitInsn(LAND);
                stack.pop();
                break;
            case I64_OR:
                mv.visitInsn(LOR);
                stack.pop();
                break;
            case I64_XOR:
                mv.visitInsn(LXOR);
                stack.pop();
                break;
            case I64_SHL:
                mv.visitInsn(LSHL);
                stack.pop();
                break;
            case I64_SHR_S:
                mv.visitInsn(LSHR);
                stack.pop();
                break;
            case I64_SHR_U:
                mv.visitInsn(LUSHR);
                stack.pop();
                break;
            case I64_ROTL:
                // Long.rotateLeft(a, b)
                longStatic("rotateLeft", "(JJ)J");
                stack.pop();
                break;
            case I64_ROTR:
                // Long.rotateRight(a, b)
                longStatic("rotateRight", "(JJ)J");
                stack.pop();
                break;
            // endregion
            // region f32
            case F32_ADD:
                mv.visitInsn(FADD);
                stack.pop();
                break;
            case F32_SUB:
                mv.visitInsn(FSUB);
                stack.pop();
                break;
            case F32_MUL:
                mv.visitInsn(FMUL);
                stack.pop();
                break;
            // endregion
            // region f64
            case F64_ADD:
                mv.visitInsn(DADD);
                stack.pop();
                break;
            case F64_SUB:
                mv.visitInsn(DSUB);
                stack.pop();
                break;
            case F64_MUL:
                mv.visitInsn(DMUL);
                stack.pop();
                break;
            // endregion
            // endregion
            default:
                throw new UnsupportedOperationException();
        }
    }

    public void zcmpBool(int type) {
        Label els = new Label();
        mv.ifZCmp(type, els);
        jumpBool(els);
    }

    public void cmpBool(int type) {
        Label els = new Label();
        mv.ifCmp(Type.INT_TYPE, type, els);
        jumpBool(els);
        stack.pop();
    }

    private void jumpBool(Label els) {
        Label end = new Label();
        mv.push(0);
        mv.goTo(end);
        mv.mark(els);
        mv.push(1);
        mv.mark(end);
    }

    private void intStatic(String name, String desc) {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", name, desc, false);
    }

    private void longStatic(String name, String desc) {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", name, desc, false);
    }

    @Override
    public void visitPrefixInsn(int opcode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitConstInsn(Object v) {
        if (v instanceof Integer) {
            mv.push((Integer) v);
            stack.push(Type.INT_TYPE);
        } else if (v instanceof Long) {
            mv.push((Long) v);
            stack.push(Type.LONG_TYPE);
        } else if (v instanceof Float) {
            mv.push((Float) v);
            stack.push(Type.FLOAT_TYPE);
        } else if (v instanceof Double) {
            mv.push((Double) v);
            stack.push(Type.DOUBLE_TYPE);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void visitNullInsn(byte type) {
        mv.push((String) null);
        stack.push(Type.getType(Object.class));
    }

    @Override
    public void visitFuncInsn(int function) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitSelectInsn(byte[] type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitVariableInsn(byte opcode, int index) {
        switch (opcode) {
            case GLOBAL_GET:
            case GLOBAL_SET:
                throw new UnsupportedOperationException();
            case LOCAL_GET:
            case LOCAL_TEE:
            case LOCAL_SET:
                boolean arg;
                int i;
                if (index < type.params.length) {
                    arg = true;
                    i = index;
                } else {
                    arg = false;
                    i = localIndeces[index - type.params.length];
                }
                Type type = localType(index);
                switch (opcode) {
                    case LOCAL_GET:
                        if (arg) mv.loadArg(i);
                        else mv.loadLocal(i);
                        stack.push(type);
                        break;
                    case LOCAL_TEE:
                        if (type.getSize() == 2) {
                            mv.dup2();
                        } else {
                            mv.dup();
                        }
                        if (arg) mv.storeArg(i);
                        else mv.storeLocal(i);
                        break;
                    case LOCAL_SET:
                        stack.pop();
                        if (arg) mv.storeArg(i);
                        else mv.storeLocal(i);
                        break;
                }
                break;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public void visitTableInsn(byte opcode, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitPrefixTableInsn(int opcode, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitPrefixBinaryTableInsn(int opcode, int firstIndex, int secondIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitMemInsn(byte opcode, int align, int offset) {
        if (opcode >= I32_LOAD && opcode <= I64_LOAD32_U) {
            stack.pop();
            getMemory();
            mv.swap();
            if (offset != 0) {
                mv.push(offset);
                mv.visitInsn(IADD);
            }
            Method method;
            switch (opcode) {
                case I32_LOAD8_S:
                case I32_LOAD8_U:
                case I64_LOAD8_S:
                case I64_LOAD8_U:
                    method = new Method("get", "(I)B");
                    break;
                case I32_LOAD16_S:
                case I32_LOAD16_U:
                case I64_LOAD16_S:
                case I64_LOAD16_U:
                    method = new Method("getShort", "(I)S");
                    break;
                case I64_LOAD32_S:
                case I64_LOAD32_U:
                case I32_LOAD:
                    method = new Method("getInt", "(I)I");
                    break;
                case I64_LOAD:
                    method = new Method("getLong", "(I)J");
                    stack.push(Type.LONG_TYPE);
                    break;
                case F32_LOAD:
                    method = new Method("getFloat", "(I)F");
                    stack.push(Type.FLOAT_TYPE);
                    break;
                case F64_LOAD:
                    method = new Method("getDouble", "(I)D");
                    stack.push(Type.DOUBLE_TYPE);
                    break;
                default:
                    throw new AssertionError();
            }
            mv.invokeVirtual(Type.getType(ByteBuffer.class), method);
            switch (opcode) {
                case I32_LOAD:
                case I32_LOAD8_S:
                case I32_LOAD16_S:
                    // Do these casts just magically work?
                    stack.push(Type.INT_TYPE);
                    break;
                case I64_LOAD8_S:
                case I64_LOAD16_S:
                case I64_LOAD32_S:
                    mv.visitInsn(I2L);
                    stack.push(Type.LONG_TYPE);
                    break;

                case I32_LOAD8_U:
                    // Byte.toUnsignedInt(x)
                    mv.invokeStatic(Type.getType(Byte.class), new Method("toUnsignedInt", "(B)I"));
                    break;
                case I32_LOAD16_U:
                    // Short.toUnsignedInt(x)
                    mv.invokeStatic(Type.getType(Short.class), new Method("toUnsignedInt", "(S)I"));
                    break;
                case I64_LOAD8_U:
                    // Byte.toUnsignedLong(x)
                    mv.invokeStatic(Type.getType(Byte.class), new Method("toUnsignedLong", "(B)J"));
                    break;
                case I64_LOAD16_U:
                    // Short.toUnsignedLong(x)
                    mv.invokeStatic(Type.getType(Short.class), new Method("toUnsignedLong", "(S)J"));
                    break;
                case I64_LOAD32_U:
                    // Integer.toUnsignedLong(x)
                    mv.invokeStatic(Type.getType(Integer.class), new Method("toUnsignedLong", "(I)J"));
                    break;
            }
        } else if (opcode >= I32_STORE && opcode <= I64_STORE32) {
            {
                Type type = stack.pop();
                int value = -1;
                if (type.getSize() == 2) {
                    value = mv.newLocal(type);
                    stack.pop();
                    mv.storeLocal(value);
                    getMemory();
                } else {
                    getMemory();
                    mv.dupX2();
                    mv.pop();
                }
                mv.swap();
                if (offset != 0) {
                    mv.push(offset);
                    mv.visitInsn(IADD);
                }
                if (value != -1) {
                    mv.loadLocal(value);
                } else {
                    mv.swap();
                }
            }
            Method method;
            switch (opcode) {
                case I32_STORE:
                    method = new Method("putInt", "(II)Ljava/nio/ByteBuffer;");
                    break;
                case I64_STORE:
                    method = new Method("putLong", "(IJ)Ljava/nio/ByteBuffer;");
                    break;
                case F32_STORE:
                    method = new Method("putFloat", "(IF)Ljava/nio/ByteBuffer;");
                    break;
                case F64_STORE:
                    method = new Method("putDouble", "(ID)Ljava/nio/ByteBuffer;");
                    break;
                default:
                    throw new AssertionError();
            }
            mv.invokeVirtual(Type.getType(ByteBuffer.class), method);
            mv.pop();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void getMemory() {
        mv.loadThis();
        mv.visitFieldInsn(GETFIELD, internalName, "mem0", Type.getDescriptor(ByteBuffer.class));
    }

    @Override
    public void visitIndexedMemInsn(int opcode, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitBlockInsn(byte opcode, int blockType) {
        Block block;
        switch (opcode) {
            case IF:
                Block.If ifb = new Block.If(blockType);
                block = ifb;
                stack.pop();
                mv.ifZCmp(GeneratorAdapter.EQ, ifb.elseLabel);
                break;
            case BLOCK:
                block = new Block.BBlock(blockType);
                break;
            case LOOP:
                block = new Block.Loop(blockType, mv);
                break;
            default:
                throw new AssertionError();
        }
        blocks.push(block);
    }

    @Override
    public void visitElseInsn() {
        Block.If ifBlock = (Block.If) blocks.peek();
        if (ifBlock == null) throw new AssertionError();
        mv.goTo(ifBlock.endLabel());
        mv.mark(ifBlock.elseLabel);
    }

    @Override
    public void visitEndInsn() {
        if (!blocks.isEmpty()) {
            blocks.pop().end(mv);
        }
    }

    @Override
    public void visitBreakInsn(byte opcode, int label) {
        switch (opcode) {
            case BR:
                mv.goTo(getLabel(label));
                break;
            case BR_IF:
                mv.ifZCmp(GeneratorAdapter.NE, getLabel(label));
                break;
        }
    }

    @Override
    public void visitTableBreakInsn(int[] labels, int defaultLabel) {
        Label[] switchLabels = new Label[labels.length];
        for (int i = 0; i < labels.length; i++) {
            switchLabels[i] = getLabel(labels[i]);
        }
        mv.visitTableSwitchInsn(0, labels.length, getLabel(defaultLabel), switchLabels);
    }

    @Override
    public void visitCallInsn(int function) {
        functions[function].invoke.accept(mv);
    }

    @Override
    public void visitCallIndirectInsn(int table, int type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitEnd() {
    }
}
