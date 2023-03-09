package io.github.eutro.jwasm.sexp;

import io.github.eutro.jwasm.sexp.wast.WastModuleVisitor;
import io.github.eutro.jwasm.sexp.wast.WastReader;
import io.github.eutro.jwasm.sexp.wast.WastVisitor;
import io.github.eutro.jwasm.tree.ModuleNode;
import io.github.eutro.jwasm.tree.analysis.ModuleValidator;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static io.github.eutro.jwasm.sexp.ErrorAlts.assertFailure;

public class WastTest {
    @TestFactory
    Stream<DynamicTest> testWast() {
        return WatReaderTest.runForTestSuite((name, source) -> {
            WastReader reader = WastReader.fromSource(source);
            Assertions.assertDoesNotThrow(() -> reader.accept(new WastVisitor() {
                ModuleNode lastModule;

                public WastModuleVisitor visitModule0() {
                    return new WastModuleVisitor() {
                        @Override
                        public void visitWatModule(Object module) {
                            lastModule = WatParser.DEFAULT.parseModule(module);
                        }

                        @Override
                        public void visitBinaryModule(Object module) {
                            lastModule = WatParser.DEFAULT.parseBinaryModule(module);
                        }

                        @Override
                        public void visitQuoteModule(Object module) {
                            lastModule = WatParser.DEFAULT.parseQuoteModule(module);
                        }
                    };
                }

                int tmc = 0;
                @Override
                public WastModuleVisitor visitModule(@Nullable String name) {
                    tmc++;
                    return new WastModuleVisitor(visitModule0()) {
                        @Override
                        public void visitEnd() {
                            try {
                                Assertions.assertDoesNotThrow(() ->
                                        lastModule.accept(new ModuleValidator()));
                            } catch (RuntimeException | Error t) {
                                t.addSuppressed(new RuntimeException("in top module #" + tmc));
                                throw t;
                            }
                        }
                    };
                }

                int amc = 0;

                @Override
                public WastModuleVisitor visitAssertMalformed(String failure) {
                    amc++;
                    return new WastModuleVisitor(visitModule0()) {
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
                            try {
                                assertFailure(failure, exn);
                            } catch (Error | RuntimeException t) {
                                t.addSuppressed(new RuntimeException("in assert_malformed #" + amc));
                                throw t;
                            }
                        }
                    };
                }


                int aic = 0;

                @Override
                public WastModuleVisitor visitAssertInvalid(String failure) {
                    aic++;
                    return new WastModuleVisitor(visitModule0()) {
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
                            try {
                                if (exn == null) {
                                    try {
                                        lastModule.accept(new ModuleValidator());
                                    } catch (RuntimeException e) {
                                        exn = e;
                                    }
                                }
                                assertFailure(failure, exn);
                            } catch (Error | RuntimeException t) {
                                t.addSuppressed(new RuntimeException("in assert_invalid #" + aic));
                                throw t;
                            }
                        }
                    };
                }
            }));
        });
    }
}
