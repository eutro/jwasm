package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ExprVisitor;

public abstract class AbstractInsnNode {
    abstract void accept(ExprVisitor ev);
}
