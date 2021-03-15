package io.github.eutro.jwasm.test;

import io.github.eutro.jwasm.ModuleReader;
import io.github.eutro.jwasm.ModuleWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class WriterTest extends ModuleTestBase {
    @Test
    void hello_world_in_out() throws IOException {
        try (InputStream is = openResource(HELLO_WORLD)) {
            ModuleWriter mw = new ModuleWriter();
            ModuleReader.fromInputStream(is).accept(mw);
            mw.toByteArray();
        }
    }

    @Test
    void hello_world_in_out_readable() throws IOException {
        ModuleWriter mw = new ModuleWriter();
        try (InputStream is = openResource(HELLO_WORLD)) {
            ModuleReader.fromInputStream(is).accept(mw);
        }
        ModuleReader.fromBytes(mw.toByteArray()).accept(new DeepModuleVisitor());
    }

    @Test
    void hello_world_in_out_same() throws IOException {
        ModuleWriter mw = new ModuleWriter();
        try (InputStream is = openResource(HELLO_WORLD)) {
            ModuleReader.fromInputStream(is).accept(mw);
        }
        byte[] bytes = mw.toByteArray();
        ModuleWriter mw2 = new ModuleWriter();
        ModuleReader.fromBytes(bytes).accept(mw2);
        byte[] mw2Bytes = mw2.toByteArray();
        assertArrayEquals(bytes, mw2Bytes);
    }

    @Test
    void game_of_life_out() throws IOException {
        try (InputStream is = openResource(GAME_OF_LIFE)) {
            ModuleWriter mw = new ModuleWriter();
            ModuleReader.fromInputStream(is).accept(mw);
            mw.toByteArray();
        }
    }

    @Test
    void game_of_life_out_readable() throws IOException {
        ModuleWriter mw = new ModuleWriter();
        try (InputStream is = openResource(GAME_OF_LIFE)) {
            ModuleReader.fromInputStream(is).accept(mw);
        }
        ModuleReader.fromBytes(mw.toByteArray()).accept(new DeepModuleVisitor());
    }

    @Test
    void game_of_life_out_same() throws IOException {
        ModuleWriter mw = new ModuleWriter();
        try (InputStream is = openResource(GAME_OF_LIFE)) {
            ModuleReader.fromInputStream(is).accept(mw);
        }
        byte[] bytes = mw.toByteArray();
        ModuleWriter mw2 = new ModuleWriter();
        ModuleReader.fromBytes(bytes).accept(mw2);
        byte[] mw2Bytes = mw2.toByteArray();
        assertArrayEquals(bytes, mw2Bytes);
    }
}
