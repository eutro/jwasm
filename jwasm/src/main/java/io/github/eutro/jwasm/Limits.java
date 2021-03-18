package io.github.eutro.jwasm;

import org.jetbrains.annotations.Nullable;

/**
 * A class that represents a WebAssembly
 * <a href="https://webassembly.github.io/spec/core/binary/types.html#limits">limits</a>
 * value, with a minimum and an optional maximum.
 */
public class Limits {
    /**
     * The minimum of the limit.
     */
    public int min;
    /**
     * The maximum of the limit, or {@code null} if there is none.
     */
    public @Nullable Integer max;

    /**
     * Construct a {@link Limits} value with the given minimum and maximum.
     *
     * @param min The minimum of the limit.
     * @param max The maximum of the limit, or {@code null} if there is none.
     */
    public Limits(int min, @Nullable Integer max) {
        this.min = min;
        this.max = max;
    }
}
