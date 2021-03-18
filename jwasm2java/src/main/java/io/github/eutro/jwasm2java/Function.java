package io.github.eutro.jwasm2java;

import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.function.Consumer;

public class Function {
    public final Consumer<GeneratorAdapter> invoke;

    public Function(Consumer<GeneratorAdapter> invoke) {
        this.invoke = invoke;
    }
}
