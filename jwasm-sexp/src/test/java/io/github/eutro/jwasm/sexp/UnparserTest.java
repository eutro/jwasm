package io.github.eutro.jwasm.sexp;

import io.github.eutro.jwasm.tree.ModuleNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class UnparserTest {
    @TestFactory
    Stream<DynamicTest> testTestSuite() {
        return ReaderTest.runForTestSuite((name, src) -> {
            List<Object> script = Reader.readAll(src);

            List<ModuleNode> modules;
            try {
                modules = ParserTest.parseAllModules(script);
            } catch (Error ignored) {
                return;
            }

            assertDoesNotThrow(() -> {
                Writer writer = new Writer(System.out);
                for (ModuleNode module : modules) {
                    writer.write(Unparser.unparse(module));
                    System.out.println();
                }
            });
        });
    }
}
