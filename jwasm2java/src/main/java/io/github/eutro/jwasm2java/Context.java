package io.github.eutro.jwasm2java;

import io.github.eutro.jwasm.tree.TypeNode;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;

import java.util.LinkedList;
import java.util.List;

import static io.github.eutro.jwasm2java.Util.makeList;
import static org.objectweb.asm.Opcodes.*;

class Context extends GeneratorAdapter {
    public LinkedList<Type> stack = new LinkedList<>();
    public Externs externs;
    private final Type[] localTypes;
    public final TypeNode funcType;

    private final LinkedList<Block> blocks = new LinkedList<>();
    private final String internalName;
    private final int[] localIndeces;
    private final List<TypeNode> funcTypes;

    public Context(String internalName,
                   MethodVisitor mv,
                   int access,
                   String name,
                   String desc,
                   List<TypeNode> funcTypes,
                   TypeNode funcType,
                   Externs externs,
                   int[] localIndeces,
                   Type[] localTypes) {
        super(ASM9, mv, access, name, desc);
        this.internalName = internalName;
        this.funcTypes = funcTypes;
        this.funcType = funcType;
        this.localIndeces = localIndeces;
        this.externs = externs;
        this.localTypes = localTypes;
    }

    public Type funcType(int index) {
        return Types.methodDesc(funcTypes.get(index));
    }

    public Type localType(int local) {
        return localTypes[local];
    }

    public Context compress(byte[] types) {
        if (types.length > 1) throw new UnsupportedOperationException();
        return this;
    }

    public int localIndex(int local) {
        return localIndeces[local];
    }

    public String internalName() {
        return internalName;
    }

    public void pushBlock(Block b) {
        blocks.push(b);
    }

    public Block peekBlock() {
        return blocks.peek();
    }

    public Block popBlock() {
        return blocks.pop();
    }

    public Label getLabel(int label) {
        return blocks.get(label).label();
    }

    public Type popType() {
        return stack.pop();
    }

    public Context remType() {
        popType();
        return this;
    }

    public Context expectType(Type t) {
        if (!popType().equals(t)) throw new IllegalStateException();
        return this;
    }

    public Type peekType() {
        return stack.peek();
    }

    public Context pushType(Type t) {
        stack.push(t);
        return this;
    }

    public Context addInsns(org.objectweb.asm.tree.AbstractInsnNode... insns) {
        for (org.objectweb.asm.tree.AbstractInsnNode insn : insns) {
            insn.accept(this);
        }
        return this;
    }

    public Context addInsns(InsnList insns) {
        insns.accept(this);
        return this;
    }

    public void ifThenElse(int opcode, InsnList ifn, InsnList ifj) {
        Label elsl = new Label();
        Label endl = new Label();
        visitJumpInsn(opcode, elsl);
        ifn.accept(this);
        goTo(endl);
        mark(elsl);
        ifj.accept(this);
        mark(endl);
    }

    public void jumpStack(int opcode) {
        ifThenElse(opcode, makeList(new InsnNode(ICONST_0)), makeList(new InsnNode(ICONST_1)));
    }
}
