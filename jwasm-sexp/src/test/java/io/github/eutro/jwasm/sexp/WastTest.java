package io.github.eutro.jwasm.sexp;

import io.github.eutro.jwasm.sexp.wast.WastModuleVisitor;
import io.github.eutro.jwasm.sexp.wast.WastReader;
import io.github.eutro.jwasm.sexp.wast.WastVisitor;
import io.github.eutro.jwasm.tree.ModuleNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class WastTest {
    private static final Map<String, Set<String>> ALT_ERROR_NAMES = new HashMap<String, Set<String>>() {{
        put("i32 constant", Collections.singleton("i32 constant out of range"));
        // The reference interpreter differs here, defining a finite set of operators, catching
        // unrecognised operators when lexing. I'd rather keep our lexer (and reader) more extensible,
        // but that does mean we can't differentiate.
        put("unexpected token", new HashSet<>(Arrays.asList(
                "unknown operator",
                "malformed lane index"
        )));

        put("END opcode expected", Collections.singleton("unexpected end of section or function"));
        put("section size mismatch", Collections.singleton("unexpected end of section or function"));
        put("length out of bounds", Collections.singleton("unexpected end"));
        put("unexpected end of section or function", Collections.singleton("unexpected end"));

        put("integer too large", Collections.singleton("malformed limit"));
        put("unexpected end", Collections.singleton("unexpected end of section or function"));
        put("unexpected content after last section", Collections.singleton("multiple start sections"));
        put("alignment must be a power of two", Collections.singleton("alignment"));
        put("wrong number of lane literals", new HashSet<>(Arrays.asList(
                "unexpected token",
                "constant out of range"
        )));
        put("invalid lane length", Collections.singleton("unexpected token"));
        put("malformed lane index", Collections.singleton("Unexpected )"));

        // many "integer representation too long" tests are broken in other funny ways instead...
        put("integer representation too long", new HashSet<>(Arrays.asList(
                "malformed functype",
                "unexpected end",
                "malformed limit"
        )));
    }};

    @TestFactory
    Stream<DynamicTest> testWast() {
        return ReaderTest.runForTestSuite(source -> {
            WastReader reader = WastReader.fromSource(source);
            Assertions.assertDoesNotThrow(() -> reader.accept(new WastVisitor() {
                ModuleNode lastModule;

                @Override
                public WastModuleVisitor visitModule() {
                    return new WastModuleVisitor() {
                        @Override
                        public void visitWatModule(Object module) {
                            lastModule = Parser.parseModule(module);
                        }

                        @Override
                        public void visitBinaryModule(Object module) {
                            lastModule = Parser.parseBinaryModule(module);
                        }

                        @Override
                        public void visitQuoteModule(Object module) {
                            lastModule = Parser.parseQuoteModule(module);
                        }
                    };
                }

                int amc = -1;

                @Override
                public WastModuleVisitor visitAssertMalformed(String failure) {
                    amc++;
                    return new WastModuleVisitor(visitModule()) {
                        RuntimeException exn = null;

                        @Override
                        public void visitWatModule(Object module) {
                            try {
                                super.visitWatModule(module);
                            } catch (RuntimeException e) {
                                exn = e;
                            }
                        }

                        @Override
                        public void visitBinaryModule(Object module) {
                            try {
                                super.visitBinaryModule(module);
                            } catch (RuntimeException e) {
                                exn = e;
                            }
                        }

                        @Override
                        public void visitQuoteModule(Object module) {
                            try {
                                super.visitQuoteModule(module);
                            } catch (RuntimeException e) {
                                exn = e;
                            }
                        }

                        @Override
                        public void visitEnd() {
                            assertDoesNotThrow(() -> {
                                assertNotNull(exn, () -> "no exception thrown, expected: " + failure);
                                Throwable rootCause = exn;
                                while (rootCause.getCause() != null) {
                                    rootCause = rootCause.getCause();
                                }
                                if (failure.equals(rootCause.getMessage()) ||
                                        ALT_ERROR_NAMES.getOrDefault(failure, Collections.emptySet())
                                                .contains(rootCause.getMessage())) {
                                    return;
                                }
                                try {
                                    assertEquals(failure, rootCause.getMessage());
                                } catch (Throwable t) {
                                    t.addSuppressed(exn);
                                    throw t;
                                }
                            }, "in assert_malformed #" + amc);
                        }
                    };
                }
            }));
        });
    }
}
