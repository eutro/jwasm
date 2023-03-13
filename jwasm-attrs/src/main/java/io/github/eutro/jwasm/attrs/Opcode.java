package io.github.eutro.jwasm.attrs;

import io.github.eutro.jwasm.Opcodes;

import java.util.Objects;

/**
 * Represents the opcode of a single WebAssembly instruction.
 */
public final class Opcode {
    /**
     * The first byte of the opcode.
     */
    public final byte opcode;
    /**
     * If the instruction is longer than one byte, the LEB128 decoded
     * suffix of the opcode.
     */
    public final int intOpcode;

    /**
     * Construct an {@link Opcode} with the given prefix and suffix.
     *
     * @param opcode    The first byte of the opcode.
     * @param intOpcode The LEB128 suffix, if any.
     */
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

    /**
     * Construct a single byte opcode.
     *
     * @param b The opcode.
     * @return The new {@link Opcode} object.
     */
    public static Opcode byteOpcode(byte b) {
        return new Opcode(b, 0);
    }

    /**
     * Construct a {@link Opcodes#INSN_PREFIX}-prefixed opcode.
     *
     * @param i The suffix.
     * @return The new {@link Opcode} object.
     */
    public static Opcode prefixOpcode(int i) {
        return new Opcode(Opcodes.INSN_PREFIX, i);
    }

    /**
     * Construct a {@link Opcodes#VECTOR_PREFIX}-prefixed opcode.
     *
     * @param i The suffix.
     * @return The new {@link Opcode} object.
     */
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
