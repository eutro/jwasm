package io.github.eutro.jwasm.tree.test;

import io.github.eutro.jwasm.ModuleReader;
import io.github.eutro.jwasm.ModuleWriter;
import io.github.eutro.jwasm.test.ModuleTestBase;
import io.github.eutro.jwasm.tree.ModuleNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i) {
            int j = i + 8;
            while (i < bytes.length && i < j) {
                int b = (int) bytes[i] & 0xFF;
                sb.append("0x");
                sb.append(HEX_ARRAY[b >>> 4]);
                sb.append(HEX_ARRAY[b & 0x0F]);
                if (++i < j) {
                    sb.append(' ');
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    void tryRoundRobin(String file) throws IOException {
        ModuleNode mn = new ModuleNode();
        try (InputStream is = openResource(file)) {
            ModuleReader.fromInputStream(is).accept(mn);
        }
        ModuleWriter mw = new ModuleWriter();
        mn.accept(mw);
        byte[] processedBytes = mw.toByteArray();

        ModuleNode mnReparsed = new ModuleNode();
        RuntimeException failure = null;
        try {
            ModuleReader.fromBytes(processedBytes).accept(mnReparsed);
        } catch (RuntimeException e) {
            failure = e;
        }
        ModuleWriter mwReparsed = new ModuleWriter();
        mnReparsed.accept(mwReparsed);
        try {
            assertEquals(
                    bytesToHex(mw.toByteArray()),
                    bytesToHex(mwReparsed.toByteArray())
            );
        } catch (RuntimeException | Error t) {
            if (failure != null) t.addSuppressed(failure);
            throw t;
        }
        if (failure != null) throw failure;
    }

    @Test
    void hello_world_to_tree_rr() throws IOException {
        tryRoundRobin(HELLO_WORLD);
    }

    @Test
    void game_of_life_to_tree_rr() throws IOException {
        tryRoundRobin(GAME_OF_LIFE);
    }

    @Test
    void aoc_rr() throws IOException {
        tryRoundRobin(AOC_SOLNS);
    }
}
