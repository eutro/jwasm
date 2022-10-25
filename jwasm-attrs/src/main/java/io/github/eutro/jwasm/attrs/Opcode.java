package io.github.eutro.jwasm.attrs;

import io.github.eutro.jwasm.Opcodes;

import java.util.Objects;

public class Opcode {
    public final byte opcode;
    public final int intOpcode;

    public Opcode(byte opcode, int intOpcode) {
        this.opcode = opcode;
        this.intOpcode = intOpcode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Opcode opcode1 = (Opcode) o;
        return opcode == opcode1.opcode && intOpcode == opcode1.intOpcode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(opcode, intOpcode);
    }

    public static Opcode byteOpcode(byte b) {
        return new Opcode(b, 0);
    }

    public static Opcode prefixOpcode(int i) {
        return new Opcode(Opcodes.INSN_PREFIX, i);
    }

    public static Opcode vectorOpcode(int i) {
        return new Opcode(Opcodes.VECTOR_PREFIX, i);
    }

    @Override
    public String toString() {
        return opcode == Opcodes.INSN_PREFIX || opcode == Opcodes.VECTOR_PREFIX
                ? String.format("0x%02X %d", opcode, intOpcode)
                : String.format("0x%02X", opcode);
    }
}
