package io.github.eutro.jwasm.attrs;

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
}
