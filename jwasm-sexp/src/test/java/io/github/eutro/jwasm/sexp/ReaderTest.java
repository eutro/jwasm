package io.github.eutro.jwasm.sexp;

import io.github.eutro.jwasm.sexp.Reader.Token;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.github.eutro.jwasm.sexp.Reader.Token.*;
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
                Reader.tokenise("(ab cd)")
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
        );
    }

    static Stream<DynamicTest> runForTestSuite(Consumer<String> sourceConsumer) {
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
                    sourceConsumer.accept(source);
                }));
    }

    @TestFactory
    Stream<DynamicTest> parseTestSuite() {
        return runForTestSuite(Reader::readAll);
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

            Reader.writeUTF8CodePoint(tested, codePoint);

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