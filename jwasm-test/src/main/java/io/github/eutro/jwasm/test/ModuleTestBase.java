package io.github.eutro.jwasm.test;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

    @NotNull
    private static URL getResourceUrl(String resource) {
        URL url = ModuleTestBase.class.getClassLoader().getResource(resource);
        if (url == null) {
            throw new IllegalArgumentException(resource + " was not found");
        }
        return url;
    }

    public static InputStream openResource(String resource) throws IOException {
        URL url = getResourceUrl(resource);
        return new BufferedInputStream(url.openStream());
    }

    public static class TestSuiteEntry {
        private final ZipEntry entry;
        private final ZipInputStream stream;

        public TestSuiteEntry(ZipEntry entry, ZipInputStream stream) {
            this.entry = entry;
            this.stream = stream;
        }

        public String getName() {
            return entry.getName();
        }

        public String toString() {
            return getName();
        }

        public InputStream getStream() {
            return new FilterInputStream(stream) {
                @Override
                public void close() throws IOException {
                    stream.closeEntry();
                }
            };
        }
    }

    @MustBeInvokedByOverriders
    public static Stream<TestSuiteEntry> openTestSuite() throws IOException {
        URL testSuiteUrl = getResourceUrl("testsuite.zip");
        ZipInputStream zis = new ZipInputStream(testSuiteUrl.openStream());
        Spliterator<TestSuiteEntry> spliterator = Spliterators.spliteratorUnknownSize(
                new Iterator<TestSuiteEntry>() {
                    ZipEntry next = null;

                    @Override
                    public boolean hasNext() {
                        try {
                            if (next == null) next = zis.getNextEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                        return next != null;
                    }

                    @Override
                    public TestSuiteEntry next() {
                        if (!hasNext()) throw new NoSuchElementException();
                        ZipEntry last = next;
                        next = null;
                        return new TestSuiteEntry(last, zis);
                    }
                },
                0);
        return StreamSupport
                .stream(spliterator, false)
                .onClose(() -> {
                    try {
                        zis.close();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }
}
