package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.CodesVisitor;

/**
 * A node that represents a the code of a function.
 *
 * @see CodesVisitor#visitCode(byte[])
 * @see CodesNode
 */
public class CodeNode {
    /**
     * The <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-valtype">valtypes</a>
     * of the local variables of the function.
     */
    public byte[] locals;

    /**
     * The body of the function.
     */
    public ExprNode expr;

    /**
     * Construct a {@link CodeNode} with the given locals and expr.
     *
     * @param locals The <a href="https://webassembly.github.io/spec/core/binary/types.html#binary-valtype">valtypes</a>
     *               of the local variables of the function.
     * @param expr   The body of the function.
     */
    public CodeNode(byte[] locals, ExprNode expr) {
        this.locals = locals;
        this.expr = expr;
    }
}
