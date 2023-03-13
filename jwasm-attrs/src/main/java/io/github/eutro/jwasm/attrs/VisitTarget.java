package io.github.eutro.jwasm.attrs;

import io.github.eutro.jwasm.ExprVisitor;

/**
 * A method of {@link ExprVisitor}.
 */
public enum VisitTarget {
    /**
     * {@link ExprVisitor#visitInsn}
     */
    Insn,
    /**
     * {@link ExprVisitor#visitPrefixInsn}
     */
    PrefixInsn,
    /**
     * {@link ExprVisitor#visitConstInsn}
     */
    ConstInsn,
    /**
     * {@link ExprVisitor#visitNullInsn}
     */
    NullInsn,
    /**
     * {@link ExprVisitor#visitFuncRefInsn}
     */
    FuncRefInsn,
    /**
     * {@link ExprVisitor#visitSelectInsn}
     */
    SelectInsn,
    /**
     * {@link ExprVisitor#visitVariableInsn}
     */
    VariableInsn,
    /**
     * {@link ExprVisitor#visitTableInsn}
     */
    TableInsn,
    /**
     * {@link ExprVisitor#visitPrefixTableInsn}
     */
    PrefixTableInsn,
    /**
     * {@link ExprVisitor#visitPrefixBinaryTableInsn}
     */
    PrefixBinaryTableInsn,
    /**
     * {@link ExprVisitor#visitMemInsn}
     */
    MemInsn,
    /**
     * {@link ExprVisitor#visitIndexedMemInsn}
     */
    IndexedMemInsn,
    /**
     * {@link ExprVisitor#visitBlockInsn}
     */
    BlockInsn,
    /**
     * {@link ExprVisitor#visitElseInsn}
     */
    ElseInsn,
    /**
     * {@link ExprVisitor#visitEndInsn}
     */
    EndInsn,
    /**
     * {@link ExprVisitor#visitBreakInsn}
     */
    BreakInsn,
    /**
     * {@link ExprVisitor#visitTableBreakInsn}
     */
    TableBreakInsn,
    /**
     * {@link ExprVisitor#visitCallInsn}
     */
    CallInsn,
    /**
     * {@link ExprVisitor#visitCallIndirectInsn}
     */
    CallIndirectInsn,
    /**
     * {@link ExprVisitor#visitVectorInsn}
     */
    VectorInsn,
    /**
     * {@link ExprVisitor#visitVectorMemInsn}
     */
    VectorMemInsn,
    /**
     * {@link ExprVisitor#visitVectorMemLaneInsn}
     */
    VectorMemLaneInsn,
    /**
     * {@link ExprVisitor#visitVectorConstOrShuffleInsn}
     */
    VectorConstOrShuffleInsn,
    /**
     * {@link ExprVisitor#visitVectorLaneInsn}
     */
    VectorLaneInsn,
}
