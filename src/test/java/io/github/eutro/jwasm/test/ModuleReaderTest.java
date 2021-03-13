package io.github.eutro.jwasm.test;

import io.github.eutro.jwasm.ModuleReader;
import io.github.eutro.jwasm.ModuleVisitor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ModuleReaderTest {

    // there's nothing particularly special about these modules
    public static final String HELLO_WORLD = "wasm_hello_world_bg.wasm";
    public static final String GAME_OF_LIFE = "wasm_game_of_life_bg.wasm";

    private InputStream openResource(String resource) throws IOException {
        URL url = ModuleReaderTest.class.getClassLoader().getResource(resource);
        assertNotNull(url, "Couldn't find " + resource);
        return url.openStream();
    }

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
