package io.github.eutro.jwasm;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.List;
import java.util.Objects;

/**
 * A {@link List} implementation which wraps a primitive byte array.
 */
public class ByteList extends AbstractList<Byte> implements List<Byte> {
    private final byte @NotNull [] values;

    /**
     * Construct a wrapper over the given primitive byte array.
     *
     * @param values The primitive byte array to wrap.
     */
    public ByteList(byte @NotNull [] values) {
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
