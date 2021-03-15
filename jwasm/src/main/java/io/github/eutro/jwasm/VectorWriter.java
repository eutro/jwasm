package io.github.eutro.jwasm;

/**
 * An interface for *Writers that write a vector of values.
 */
public interface VectorWriter extends ByteArrayConvertible {
    /**
     * @return The {@link #count()}, encoded as an LEB128 unsigned integer,
     * followed by the {@link #raw()} vector data.
     */
    @Override
    default byte[] toByteArray() {
        ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();
        out.putVarUInt(count());
        out.put(raw());
        return out.toByteArray();
    }

    /**
     * @return The number of elements in the vector.
     */
    int count();

    /**
     * @return The raw contents of the vector, with each element immediately following the last.
     */
    byte[] raw();
}
