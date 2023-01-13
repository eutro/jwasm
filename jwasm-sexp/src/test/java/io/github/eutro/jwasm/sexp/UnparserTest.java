package io.github.eutro.jwasm.sexp;

import io.github.eutro.jwasm.tree.ModuleNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnparserTest {
    Stream<DynamicTest> runTestSuite(Consumer<ModuleNode> task) {
        return ReaderTest.runForTestSuite((name, src) -> {
            List<Object> script = Reader.readAll(src);

            List<ModuleNode> modules;
            try {
                modules = ParserTest.parseAllModules(script);
            } catch (Error ignored) {
                return;
            }

            for (ModuleNode module : modules) {
                assertDoesNotThrow(() -> task.accept(module));
            }
        });
    }

    @TestFactory
    Stream<DynamicTest> unparseTestSuite() {
        return runTestSuite(Unparser::unparse);
    }

    @TestFactory
    Stream<DynamicTest> unparseReparseTestSuite() {
        return runTestSuite(module -> Parser.parseModule(Unparser.unparse(module)));
    }

    @TestFactory
    Stream<DynamicTest> unparseWriteReparseTestSuite() {
        return runTestSuite(module -> {
            Object unparsed = Unparser.unparse(module);
            String written = Writer.writeToString(unparsed);
            try {
                List<Object> read = Reader.readAll(written);
                assertEquals(read.size(), 1);
                Parser.parseModule(unparsed);
            } catch (Throwable t) {
                System.err.println(written);
                throw t;
            }
        });
    }
}
