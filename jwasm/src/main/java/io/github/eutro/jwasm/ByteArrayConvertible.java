package io.github.eutro.jwasm;

/**
 * An interface for classes that can be converted to a byte array using {@link #toByteArray()}.
 */
public interface ByteArrayConvertible {
    /**
     * Convert this to a byte array.
     *
     * @return The byte array.
     */
    byte[] toByteArray();
}
