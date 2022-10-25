package io.github.eutro.jwasm.sexp;

import io.github.eutro.jwasm.ByteInputStream;
import io.github.eutro.jwasm.sexp.internal.Token;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.github.eutro.jwasm.sexp.internal.Token.Type;
import static io.github.eutro.jwasm.sexp.internal.Token.writeUTF8CodePoint;
import static org.junit.jupiter.api.Assertions.*;

class ReaderTest {
    @Test
    void testLex() {
        assertEquals(
                Arrays.asList(
                        new Token(Type.T_BR_OPEN, "("),
                        new Token(Type.T_KEYWORD, "ab"),
                        new Token(Type.T_KEYWORD, "cd"),
                        new Token(Type.T_BR_CLOSE, ")")
                ),
                Reader.tokenise(new ByteInputStream.ByteBufferByteInputStream(
                        ByteBuffer.wrap("(ab cd)".getBytes(StandardCharsets.UTF_8))))
        );
    }

    @Test
    void testList() {
        assertEquals(
                Collections.singletonList(
                        Arrays.asList("ab", "cd")
                ),
                Reader.readAll("(ab cd)")
        );
    }

    @Test
    void testStrings() {
        assertArrayEquals(
                new byte[]{0, 'a', 's', 'm', '\n', 'a'},
                (byte[]) Reader.readAll("\"\\00asm\\na\"").get(0)
        );
    }

    @Test
    void testNumbers() {
        assertEquals(
                Arrays.asList(BigInteger.valueOf(0x1), BigInteger.valueOf(0x2)),
                Reader.readAll("0x1 0x2")
        );

        assertEquals(
                Collections.singletonList(0xa0ff + (double) 0xf141a59aL / 0x100000000L),
                Reader.readAll("0xa0_ff.f141_a59a")
                        .stream()
                        .map(it -> ((Reader.ParsedNumber) it).toDouble(false))
                        .collect(Collectors.toList())
        );

        assertEquals(
                Arrays.asList(
                        BigInteger.valueOf(-0x80000001L),
                        BigInteger.valueOf(-2147483649L)
                ),
                Reader.readAll("-0x80000001 -2147483649")
        );

        assertEquals(
                Arrays.asList(
                        new BigInteger("18446744073709551616"),
                        new BigInteger("-9223372036854775809")
                ),
                Reader.readAll("18446744073709551616 -9223372036854775809")
        );

        assertEquals(
                Arrays.asList(
                        0123456789e019D,
                        0123456789e+019D,
                        0123456789e-019D
                ),
                Reader.readAll("0123456789e019 0123456789e+019 0123456789e-019")
                        .stream()
                        .map(it -> ((Reader.ParsedNumber) it).toDouble(false))
                        .collect(Collectors.toList())
        );

        Object val = Reader.readAll("nan:0x80_0000").get(0);
        assertEquals(Reader.ParsedNumber.class, val.getClass());
        assertThrows(RuntimeException.class, () ->
                ((Reader.ParsedNumber) val).toFloat(false));
    }

    interface SuiteRunner {
        void accept(String scriptName, String source);
    }

    static Stream<DynamicTest> runForTestSuite(Consumer<String> stringConsumer) {
        return runForTestSuite((name, source) -> stringConsumer.accept(source));
    }

    static Stream<DynamicTest> runForTestSuite(SuiteRunner runner) {
        String testsuite = System.getenv("WASM_TESTSUITE");
        if (testsuite == null) {
            throw new IllegalStateException("WASM_TESTSUITE not supplied");
        }

        File suiteDir = new File(testsuite);
        File[] files = Objects.requireNonNull(suiteDir.listFiles(($, name) -> name.endsWith(".wast")));
        Arrays.sort(files);
        return Arrays.stream(files)
                .map(script -> DynamicTest.dynamicTest(script.getName(), script.toURI(), () -> {
                    String source = new String(Files.readAllBytes(script.toPath()), StandardCharsets.UTF_8);
                    runner.accept(script.getName(), source);
                }));
    }

    @TestFactory
    Stream<DynamicTest> parseTestSuite() {
        return runForTestSuite((name, src) -> Reader.readAll(src));
    }

    @Test
    void testUTF8() {
        int[] codePoints = IntStream.concat(
                        new Random()
                                .ints(Short.MAX_VALUE,
                                        0,
                                        Character.MAX_CODE_POINT + 1),
                        IntStream.of(
                                0,
                                0x007F,
                                0x07FF,
                                0xFFFF,
                                0x10FFFF
                        ).flatMap(i -> IntStream.of(i - 1, i, i + 1))
                )
                .toArray();

        CharsetEncoder encoder = StandardCharsets.UTF_8
                .newEncoder()
                .onMalformedInput(CodingErrorAction.REPORT);
        for (int codePoint : codePoints) {
            if (!Character.isValidCodePoint(codePoint)) continue;

            ByteBuffer expectedBuf = ByteBuffer.allocate(4);
            ByteArrayOutputStream tested = new ByteArrayOutputStream();

            CoderResult res = encoder.encode(
                    CharBuffer.wrap(Character.toChars(codePoint)),
                    expectedBuf,
                    true
            );
            if (res.isError()) {
                continue; // thanks
            }

            writeUTF8CodePoint(tested, codePoint);

            expectedBuf.flip();
            byte[] expectedBytes = new byte[expectedBuf.remaining()];
            expectedBuf.get(expectedBytes);

            byte[] testedBytes = tested.toByteArray();

            try {
                assertArrayEquals(expectedBytes, testedBytes);
            } catch (Throwable e) {
                e.addSuppressed(
                        new RuntimeException(String.format("for code point: U+%06x", codePoint)));
                e.addSuppressed(
                        new RuntimeException(
                                "expected: <" + Arrays.toString(expectedBytes) + ">" +
                                        " but was: <" + Arrays.toString(testedBytes) + ">"
                        )
                );
                throw e;
            }
        }
    }
}