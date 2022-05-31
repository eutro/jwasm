package io.github.eutro.jwasm.tree.analysis;

import java.util.AbstractList;
import java.util.List;
import java.util.Objects;

public class ByteList extends AbstractList<Byte> implements List<Byte> {
    private final byte[] values;

    public ByteList(byte[] values) {
        this.values = values;
    }

    @Override
    public Byte get(int index) {
        return values[index];
    }

    @Override
    public Byte set(int index, Byte element) {
        byte old = values[index];
        values[index] = Objects.requireNonNull(element);
        return old;
    }

    @Override
    public int size() {
        return values.length;
    }
}
