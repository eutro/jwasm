package io.github.eutro.jwasm.tree.test;

import io.github.eutro.jwasm.ModuleReader;
import io.github.eutro.jwasm.test.ModuleTestBase;
import io.github.eutro.jwasm.tree.ModuleNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

public class ModuleReaderTreeTest extends ModuleTestBase {
    @Test
    void hello_world_to_tree() throws IOException {
        ModuleNode mn = new ModuleNode();
        try (InputStream is = openResource(HELLO_WORLD)) {
            ModuleReader.fromInputStream(is).accept(mn);
        }
    }

    @Test
    void game_of_life_to_tree() throws IOException {
        ModuleNode mn = new ModuleNode();
        try (InputStream is = openResource(GAME_OF_LIFE)) {
            ModuleReader.fromInputStream(is).accept(mn);
        }
    }
}
