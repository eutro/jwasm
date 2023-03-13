package io.github.eutro.jwasm.attrs;

import io.github.eutro.jwasm.Opcodes;

import java.util.Arrays;

/**
 * The type of an instruction: what types it removes from the stack, and what types it pushes.
 */
public class StackType {
    /**
     * The types removed from the stack.
     */
    public final byte[] pops;
    /**
     * The types pushed onto the stack.
     */
    public final byte[] pushes;

    /**
     * Construct a stack type with the given pops and pushes.
     *
     * @param pops   The popped types.
     * @param pushes The pushed types.
     */
    public StackType(byte[] pops, byte[] pushes) {
        this.pops = pops;
        this.pushes = pushes;
    }

    /**
     * A stack type which pops the given types, and pushes nothing.
     *
     * @param types The popped types.
     * @return The stack type.
     */
    public static StackType pop(byte... types) {
        return new StackType(types, new byte[0]);
    }

    /**
     * A stack type which pops nothing, and pushes the given types.
     *
     * @param types The pushed types.
     * @return The stack type.
     */
    public static StackType push(byte... types) {
        return new StackType(new byte[0], types);
    }

    /**
     * Combine this stack type with another, concatenating their respective pops and pushes.
     *
     * @param o The other stack type.
     * @return The new stack type.
     */
    public StackType and(StackType o) {
        byte[] newPops = new byte[pops.length + o.pops.length];
        System.arraycopy(pops, 0, newPops, 0, pops.length);
        System.arraycopy(o.pops, 0, newPops, pops.length, o.pops.length);

        byte[] newPushes = new byte[pushes.length + o.pushes.length];
        System.arraycopy(pushes, 0, newPushes, 0, pushes.length);
        System.arraycopy(o.pushes, 0, newPushes, pushes.length, o.pushes.length);

        return new StackType(newPops, newPushes);
    }

    /**
     * A unary operator that pops and pushes the same type.
     *
     * @param type The type.
     * @return The stack type.
     */
    public static StackType unOp(byte type) {
        return pop(type).and(push(type));
    }

    /**
     * A binary operator that pops two values and pushes one value, all the same type.
     *
     * @param type The type.
     * @return The stack type.
     */
    public static StackType binOp(byte type) {
        return pop(type, type).and(push(type));
    }

    /**
     * An operator that pops one value of a given type and pushes one {@code i32}.
     *
     * @param type The type popped.
     * @return The stack type.
     */
    public static StackType testOp(byte type) {
        return pop(type).and(push(Opcodes.I32));
    }

    /**
     * An operator that pops two values of a given type and pushes one {@code i32}.
     *
     * @param type The type popped.
     * @return The stack type.
     */
    public static StackType relOp(byte type) {
        return pop(type, type).and(push(Opcodes.I32));
    }

    /**
     * An operator that pops a {@code from} value and pushes a {@code to} value.
     *
     * @param to   The pushed type.
     * @param from The popped type.
     * @return The stack type.
     */
    public static StackType convertOp(byte to, byte from) {
        return pop(from).and(push(to));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StackType stackType = (StackType) o;
        return Arrays.equals(pops, stackType.pops) && Arrays.equals(pushes, stackType.pushes);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(pops);
        result = 31 * result + Arrays.hashCode(pushes);
        return result;
    }
}
