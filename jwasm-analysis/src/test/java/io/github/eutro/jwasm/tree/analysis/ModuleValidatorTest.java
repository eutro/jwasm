package io.github.eutro.jwasm.tree.analysis;

import io.github.eutro.jwasm.ModuleReader;
import io.github.eutro.jwasm.ModuleVisitor;
import io.github.eutro.jwasm.test.ModuleTestBase;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

class ModuleValidatorTest extends ModuleTestBase {
    void tryValidate(String name) throws IOException {
        try (InputStream is = openResource(name)) {
            ModuleVisitor mv = new ModuleValidator(); // new StackDumper(System.err);
            ModuleReader.fromInputStream(is).accept(mv);
        }
    }

    @Test
    void hello_world_valid() throws IOException {
        tryValidate(HELLO_WORLD);
    }

    @Test
    void game_of_life_valid() throws IOException {
        tryValidate(HELLO_WORLD);
    }

    @Test
    void reading_and_writing_graphics_assemblyscript() throws IOException {
        tryValidate(RAWG_ASMS);
    }

    @Test
    void aoc() throws Throwable {
        tryValidate(AOC_SOLNS);
    }
}