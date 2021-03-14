package io.github.eutro.jwasm.test;

import io.github.eutro.jwasm.ModuleReader;
import io.github.eutro.jwasm.ModuleVisitor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ModuleReaderTest extends ModuleTestBase {

    @Test
    void hello_world_skipping_sections() throws IOException {
        try (InputStream is = openResource(HELLO_WORLD)) {
            ModuleReader.fromInputStream(is).accept(new ModuleVisitor());
        }
    }

    @Test
    void hello_world_deep() throws IOException {
        try (InputStream is = openResource(HELLO_WORLD)) {
            ModuleReader.fromInputStream(is).accept(new DeepModuleVisitor());
        }
    }

    @Test
    void game_of_life_skipping_sections() throws IOException {
        try (InputStream is = openResource(GAME_OF_LIFE)) {
            ModuleReader.fromInputStream(is).accept(new ModuleVisitor());
        }
    }

    @Test
    void game_of_life_deep() throws IOException {
        try (InputStream is = openResource(GAME_OF_LIFE)) {
            ModuleReader.fromInputStream(is).accept(new DeepModuleVisitor());
        }
    }
}
