package io.github.eutro.jwasm.sexp;

import io.github.eutro.jwasm.sexp.wast.WastModuleVisitor;
import io.github.eutro.jwasm.sexp.wast.WastReader;
import io.github.eutro.jwasm.sexp.wast.WastVisitor;
import io.github.eutro.jwasm.tree.ModuleNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class WastTest {
    private static final Map<String, Set<String>> ALT_ERROR_NAMES = new HashMap<String, Set<String>>() {{
        put("i32 constant", Collections.singleton("i32 constant out of range"));
        // The reference interpreter differs here, defining a finite set of operators, catching
        // unrecognised operators when lexing. I'd rather keep our lexer (and reader) more extensible,
        // but that does mean we can't differentiate.
        put("unexpected token", Collections.singleton("unknown operator"));

        // Relevant test is:
        //;; Type section with signed LEB128 encoded type
        //(assert_malformed
        //  (module binary
        //    "\00asm" "\01\00\00\00"
        //    "\01"                     ;; Type section id
        //    "\05"                     ;; Type section length
        //    "\01"                     ;; Types vector length
        //    "\e0\7f"                  ;; Malformed functype, -0x20 in signed LEB128 encoding
        //    "\00\00"
        //  )
        //  "integer representation too long"
        //)
        put("integer representation too long", Collections.singleton("malformed functype"));
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
