package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.FunctionsVisitor;

/**
 * A node that represents the type of a function that is defined in the module.
 */
public class FuncNode {
    /**
     * The
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-typeidx">index</a>
     * of the function's type.
     */
    public int type;

    /**
     * Construct a {@link FuncNode} with the given type.
     *
     * @param type The
     *             <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-typeidx">index</a>
     *             of the function's type.
     */
    public FuncNode(int type) {
        this.type = type;
    }

    /**
     * Make the given {@link FunctionsVisitor} visit this function type.
     *
     * @param fv The visitor to visit.
     */
    public void accept(FunctionsVisitor fv) {
        fv.visitFunc(type);
    }
}
