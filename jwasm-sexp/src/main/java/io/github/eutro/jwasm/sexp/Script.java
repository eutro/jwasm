package io.github.eutro.jwasm.sexp;

import io.github.eutro.jwasm.tree.ModuleNode;

import java.util.ArrayList;
import java.util.List;

public class Script {
    public final List<Command> commands = new ArrayList<>();

    public static class Command {
        public static class Module extends Command {
            public final ModuleNode node;

            public Module(ModuleNode node) {
                this.node = node;
            }
        }
    }
}
