package io.github.eutro.jwasm.attrs;

import io.github.eutro.jwasm.Opcodes;

import java.util.Arrays;

public class StackType {
    public final byte[] pops, pushes;

    public StackType(byte[] pops, byte[] pushes) {
        this.pops = pops;
        this.pushes = pushes;
    }

    public static StackType pop(byte... types) {
        return new StackType(types, new byte[0]);
    }

    public static StackType push(byte... types) {
        return new StackType(new byte[0], types);
    }

    public StackType and(StackType o) {
        byte[] newPops = new byte[pops.length + o.pops.length];
        System.arraycopy(pops, 0, newPops, 0, pops.length);
        System.arraycopy(o.pops, 0, newPops, pops.length, o.pops.length);

        byte[] newPushes = new byte[pushes.length + o.pushes.length];
        System.arraycopy(pushes, 0, newPushes, 0, pushes.length);
        System.arraycopy(o.pushes, 0, newPushes, pushes.length, o.pushes.length);

        return new StackType(newPops, newPushes);
    }

    public static StackType unOp(byte type) {
        return pop(type).and(push(type));
    }

    public static StackType binOp(byte type) {
        return pop(type, type).and(push(type));
    }

    public static StackType testOp(byte type) {
        return pop(type).and(push(Opcodes.I32));
    }

    public static StackType relOp(byte type) {
        return pop(type, type).and(push(Opcodes.I32));
    }

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
