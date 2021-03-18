package io.github.eutro.jwasm2java;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static io.github.eutro.jwasm.Opcodes.*;
import static org.objectweb.asm.Opcodes.*;

public class Types {
    public static int offset(int opcode, byte type) {
        return toJava(type).getOpcode(opcode);
    }

    public static Type toJava(byte type) {
        switch (type) {
            case I32:
                return Type.INT_TYPE;
            case I64:
                return Type.LONG_TYPE;
            case F32:
                return Type.FLOAT_TYPE;
            case F64:
                return Type.DOUBLE_TYPE;
            case FUNCREF:
            case EXTERNREF:
                return Type.getType(Object.class);
            default:
                throw new IllegalArgumentException("Not a type");
        }
    }

    public static Type returnType(byte[] returns) {
        switch (returns.length) {
            case 0:
                return Type.VOID_TYPE;
            case 1:
                return Types.toJava(returns[0]);
            default:
                Type lastType = Types.toJava(returns[0]);
                for (int i = 1; i < returns.length; i++) {
                    if (!lastType.equals(Types.toJava(returns[i]))) {
                        lastType = Type.getType(Object.class);
                        break;
                    }
                }
                return Type.getType("[" + lastType);
        }
    }

    public static int size(byte type) {
        return type == F64 || type == I64 ? 2 : 1;
    }

    public static void swap(MethodVisitor mv, byte type) {
        if (size(type) == 1) {
            mv.visitInsn(SWAP);
        } else {
            mv.visitInsn(DUP2_X2);
            mv.visitInsn(POP2);
        }
    }

    public static void dup(MethodVisitor mv, byte type) {
        if (size(type) == 1) {
            mv.visitInsn(DUP);
        } else {
            mv.visitInsn(DUP2);
        }
    }
}
