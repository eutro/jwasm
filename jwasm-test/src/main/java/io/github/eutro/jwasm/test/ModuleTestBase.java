package io.github.eutro.jwasm.test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A base class for tests that read a module from resources.
 */
public class ModuleTestBase {
    // there's nothing particularly special about these modules
    public static final String HELLO_WORLD = "wasm_hello_world_bg.wasm";
    public static final String GAME_OF_LIFE = "wasm_game_of_life_bg.wasm";
    public static final String RAWG_ASMS = "reading_and_writing_graphics_assemblyscript.wasm";
    public static final String RAWG_RUST = "reading_and_writing_graphics_rust.wasm";
    public static final String AOC_SOLNS = "aoc_bg.wasm";

    protected InputStream openResource(String resource) throws IOException {
        URL url = ModuleTestBase.class.getClassLoader().getResource(resource);
        assertNotNull(url, "Couldn't find " + resource);
        return new BufferedInputStream(url.openStream());
    }
}
