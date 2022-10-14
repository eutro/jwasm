package io.github.eutro.jwasm.sexp;

import io.github.eutro.jwasm.tree.ModuleNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ParserTest {
    @TestFactory
    Stream<DynamicTest> evaluateTestSuite() {
        return ReaderTest.runForTestSuite(src -> {
            List<Object> script = Reader.readAll(src);

            List<ModuleNode> modules = new ArrayList<>();
            assertDoesNotThrow(() -> {
                for (Object stmt : script) {
                    if (stmt instanceof List<?>) {
                        List<?> list = (List<?>) stmt;
                        if ("module".equals(list.get(0))) {
                            if (list.contains("binary")) {
                                modules.add(Parser.parseBinaryModule(stmt));
                            } else {
                                modules.add(Parser.parseModule(stmt));
                            }
                        }
                    }
                }
            });
            System.out.println(modules);
        });
    }

    @Test
    void parseWat() throws Throwable {
        String src = new String(Files.readAllBytes(
                Paths.get(Objects.requireNonNull(ParserTest.class.getResource("/wat/basic.wat"))
                        .toURI())
        ), StandardCharsets.UTF_8);
        List<Object> script = Reader.readAll(src);

        Parser.parseModule(script.get(0));
    }
}
