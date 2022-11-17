package io.github.eutro.jwasm.test;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
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
        private final String name;
        private final URL url;

        public TestSuiteEntry(String name, URL resource) {
            this.name = name;
            this.url = resource;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return getName();
        }

        public InputStream getStream() throws IOException {
            return url.openStream();
        }
    }

    @MustBeInvokedByOverriders
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
