package io.github.eutro.jwasm.tree.analysis;

import io.github.eutro.jwasm.BlockType;
import io.github.eutro.jwasm.ExprVisitor;
import io.github.eutro.jwasm.Opcodes;
import io.github.eutro.jwasm.ValidationException;
import io.github.eutro.jwasm.tree.GlobalTypeNode;
import org.jetbrains.annotations.Nullable;

import static io.github.eutro.jwasm.tree.analysis.ModuleValidator.assertMsg1;

/**
 * An {@link ExprVisitor} which asserts that the expression is a
 * <a href="https://webassembly.github.io/spec/core/valid/instructions.html#constant-expressions">constant</a>.
 */
class ConstantExprValidator extends ExprVisitor {
    final VerifCtx ctx;

    ConstantExprValidator(VerifCtx ctx, @Nullable ExprVisitor dl) {
        super(dl);
        this.ctx = ctx;
    }

    private void notConstant() {
        throw new ValidationException("Expression is not constant",
                new RuntimeException("constant expression required"));
    }

    @Override
    public void visitInsn(byte opcode) {
        notConstant();
    }

    @Override
    public void visitPrefixInsn(int opcode) {
        notConstant();
    }

    @Override
    public void visitConstInsn(Object v) {
        super.visitConstInsn(v); // ok, constant
    }

    @Override
    public void visitNullInsn(byte type) {
        super.visitNullInsn(type); // ok, constant
    }

    @Override
    public void visitFuncRefInsn(int function) {
        super.visitFuncRefInsn(function); // ok, constant
    }

    @Override
    public void visitSelectInsn(byte[] type) {
        notConstant();
    }

    @Override
    public void visitVariableInsn(byte opcode, int variable) {
        if (opcode != Opcodes.GLOBAL_GET) {
            notConstant();
        }
        super.visitVariableInsn(opcode, variable);
        GlobalTypeNode ty = ctx.globals.get(variable);
        assertMsg1(ty.mut == Opcodes.MUT_CONST, "constant expression required",
                "global %d must be const", variable);
    }

    @Override
    public void visitTableInsn(byte opcode, int table) {
        notConstant();
    }

    @Override
    public void visitPrefixTableInsn(int opcode, int table) {
        notConstant();
    }

    @Override
    public void visitPrefixBinaryTableInsn(int opcode, int firstIndex, int secondIndex) {
        notConstant();
    }

    @Override
    public void visitMemInsn(byte opcode, int align, int offset) {
        notConstant();
    }

    @Override
    public void visitIndexedMemInsn(int opcode, int index) {
        notConstant();
    }

    @Override
    public void visitBlockInsn(byte opcode, BlockType blockType) {
        notConstant();
    }

    @Override
    public void visitElseInsn() {
        notConstant();
    }

    @Override
    public void visitEndInsn() {
        super.visitEndInsn();
    }

    @Override
    public void visitBreakInsn(byte opcode, int label) {
        notConstant();
    }

    @Override
    public void visitTableBreakInsn(int[] labels, int defaultLabel) {
        notConstant();
    }

    @Override
    public void visitCallInsn(int function) {
        notConstant();
    }

    @Override
    public void visitCallIndirectInsn(int table, int type) {
        notConstant();
    }

    @Override
    public void visitVectorInsn(int opcode) {
        notConstant();
    }

    @Override
    public void visitVectorMemInsn(int opcode, int align, int offset) {
        notConstant();
    }

    @Override
    public void visitVectorMemLaneInsn(int opcode, int align, int offset, byte lane) {
        notConstant();
    }

    @Override
    public void visitVectorConstOrShuffleInsn(int opcode, byte[] bytes) {
        if (opcode != Opcodes.V128_CONST) {
            notConstant();
        }
        super.visitVectorConstOrShuffleInsn(opcode, bytes);
    }

    @Override
    public void visitVectorLaneInsn(int opcode, byte lane) {
        notConstant();
    }
}
