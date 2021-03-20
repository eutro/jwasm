package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.GlobalsVisitor;
import io.github.eutro.jwasm.ImportsVisitor;

/**
 * A node that represents the type of an imported, exported or a module's own, global.
 *
 * @see GlobalsVisitor#visitGlobal(byte, byte)
 * @see ImportsVisitor#visitGlobalImport(String, String, byte, byte)
 */
public class GlobalTypeNode {
    /**
     * The
     * <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-mut">mutability</a>
     * of the
     * <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-globaltype">globaltype</a>
     * of the global.
     */
    public byte mut;

    /**
     * The
     * <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-valtype">valtype</a>
     * of the
     * <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-globaltype">globaltype</a>
     * of the global.
     */
    public byte type;

    /**
     * Construct a {@link GlobalTypeNode} with the given mutability and type.
     *
     * @param mut  The
     *             <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-mut">mutability</a>
     *             of the
     *             <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-globaltype">globaltype</a>
     *             of the global.
     * @param type The
     *             <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-valtype">valtype</a>
     *             of the
     *             <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-globaltype">globaltype</a>
     *             of the global.
     */
    public GlobalTypeNode(byte mut, byte type) {
        this.mut = mut;
        this.type = type;
    }
}
