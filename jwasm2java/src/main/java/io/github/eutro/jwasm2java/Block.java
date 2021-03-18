package io.github.eutro.jwasm2java;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public abstract class Block {
    public final int type;

    public Block(int type) {
        this.type = type;
    }

    abstract void end(MethodVisitor mv);

    public abstract Label label();

    public static class If extends Block {
        public Label elseLabel = new Label();
        public Label endLabel = null;

        public If(int type) {
            super(type);
        }

        public Label endLabel() {
            return endLabel = new Label();
        }

        @Override
        public void end(MethodVisitor mv) {
            mv.visitLabel(label());
        }

        @Override
        public Label label() {
            return endLabel == null ? elseLabel : endLabel;
        }
    }

    public static class BBlock extends Block {
        public Label label = new Label();

        public BBlock(int type) {
            super(type);
        }

        @Override
        void end(MethodVisitor mv) {
            mv.visitLabel(label());
        }

        @Override
        public Label label() {
            return label;
        }
    }

    public static class Loop extends Block {
        private final Label label;

        public Loop(int type, MethodVisitor mv) {
            super(type);
            label = new Label();
            mv.visitLabel(label);
        }

        @Override
        void end(MethodVisitor mv) {
        }

        @Override
        public Label label() {
            return label;
        }
    }
}
