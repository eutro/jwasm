package io.github.eutro.jwasm.test;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A base class for tests that read a module from resources.
 */
public class ModuleTestBase {
    // there's nothing particularly special about these modules
    /**
     * The name of an example module that can be fetched with {@link #openResource(String)}.
     */
    public static final String
            HELLO_WORLD = "wasm_hello_world_bg.wasm",
            GAME_OF_LIFE = "wasm_game_of_life_bg.wasm",
            RAWG_ASMS = "reading_and_writing_graphics_assemblyscript.wasm",
            RAWG_RUST = "reading_and_writing_graphics_rust.wasm",
            AOC_SOLNS = "aoc_bg.wasm";

    @NotNull
    private static URL getResourceUrl(String resource) {
        URL url = ModuleTestBase.class.getClassLoader().getResource(resource);
        if (url == null) {
            throw new IllegalArgumentException(resource + " was not found");
        }
        return url;
    }

    /**
     * Open a buffered input stream to a named resource in the class loader of this class.
     *
     * @param resource The resource.
     * @return The input stream.
     * @throws IOException If an error occurs opening the resource.
     */
    public static InputStream openResource(String resource) throws IOException {
        URL url = getResourceUrl(resource);
        return new BufferedInputStream(url.openStream());
    }

    /**
     * An entry of the WebAssembly test suite.
     */
    public static class TestSuiteEntry {
        private final String name;
        private final URL url;

        TestSuiteEntry(String name, URL resource) {
            this.name = name;
            this.url = resource;
        }

        /**
         * Get the name of the entry.
         *
         * @return The name.
         */
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return getName();
        }

        /**
         * Open an input stream to read the test suite entry.
         *
         * @return The input stream reading the entry.
         * @throws IOException If opening the entry fails.
         */
        public InputStream getStream() throws IOException {
            return url.openStream();
        }
    }

    /**
     * Open the WebAssembly test suite, returning a stream of entries.
     *
     * @return The test suite entries.
     * @throws IOException If reading the manifest fails.
     */
    public static Stream<TestSuiteEntry> openTestSuite() throws IOException {
        URL testSuiteUrl = getResourceUrl("testsuite/manifest.txt");
        List<String> entries;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(testSuiteUrl.openStream()))) {
            entries = br.lines().collect(Collectors.toList());
        }
        return entries
                .stream()
                .map(name -> {
                    String resName = "/testsuite/" + name;
                    return new TestSuiteEntry(name,
                            Objects.requireNonNull(
                                    ModuleTestBase.class.getResource(resName),
                                    resName
                            ));
                });
    }
}
