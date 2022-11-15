package io.github.eutro.jwasm.sexp;

import io.github.eutro.jwasm.ByteInputStream;
import io.github.eutro.jwasm.sexp.internal.Token;
import io.github.eutro.jwasm.test.ModuleTestBase;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
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
    void testHexInts() {
        assertEquals(
                Arrays.asList(BigInteger.valueOf(0x1), BigInteger.valueOf(0x2)),
                Reader.readAll("0x1 0x2")
                        .stream()
                        .map(Reader.ParsedNumber.class::cast)
                        .map(Reader.ParsedNumber::toBigInt)
                        .collect(Collectors.toList())
        );
    }

    @Test
    void testHexFloats() {
        assertEquals(
                Collections.singletonList(0xa0ff + (double) 0xf141a59aL / 0x100000000L),
                Reader.readAll("0xa0_ff.f141_a59a")
                        .stream()
                        .map(it -> ((Reader.ParsedNumber) it).toDouble(false))
                        .collect(Collectors.toList())
        );
    }

    @Test
    void testHexLongs() {
        assertEquals(
                Arrays.asList(
                        BigInteger.valueOf(-0x80000001L),
                        BigInteger.valueOf(-2147483649L)
                ),
                Reader.readAll("-0x80000001 -2147483649")
                        .stream()
                        .map(Reader.ParsedNumber.class::cast)
                        .map(Reader.ParsedNumber::toBigInt)
                        .collect(Collectors.toList())
        );
    }

    @Test
    void testHexVeryLongs() {
        assertEquals(
                Arrays.asList(
                        new BigInteger("18446744073709551616"),
                        new BigInteger("-9223372036854775809")
                ),
                Reader.readAll("18446744073709551616 -9223372036854775809")
                        .stream()
                        .map(Reader.ParsedNumber.class::cast)
                        .map(Reader.ParsedNumber::toBigInt)
                        .collect(Collectors.toList())
        );
    }

    @Test
    void testWeirdDoubles() {
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
    }

    @Test
    void testDoubleNans() {
        assertEquals(
                Arrays.asList(
                        0x7FF0_0000_0000_0000L,
                        0xFFF0_0000_0000_0000L,
                        0x7FF8_0000_0000_0000L,
                        0xFFF8_0000_0000_0000L,
                        0x7FF4_0000_0000_0000L,
                        0xFFF4_0000_0000_0000L
                ),
                Reader.readAll("inf -inf nan -nan nan:0x4000000000000 -nan:0x4000000000000")
                        .stream()
                        .map(it -> Double.doubleToRawLongBits(((Reader.ParsedNumber) it).doubleValue()))
                        .collect(Collectors.toList())
        );
    }

    @Test
    void testFloatNans() {
        assertEquals(
                Arrays.asList(
                        0x7F80_0000,
                        0xFF80_0000,
                        0x7FC0_0000,
                        0xFFC0_0000,
                        0x7FA0_0000,
                        0xFFA0_0000
                ),
                Reader.readAll("inf -inf nan -nan nan:0x200000 -nan:0x200000")
                        .stream()
                        .map(it -> Float.floatToRawIntBits(((Reader.ParsedNumber) it).floatValue()))
                        .collect(Collectors.toList())
        );
    }

    @Test
    void testFloatRounding() {
        assertEquals(
                Arrays.asList(
                        3.3f,
                        34.8f
                ),
                Reader.readAll("3.3 34.8")
                        .stream()
                        .map(it -> ((Reader.ParsedNumber) it).floatValue())
                        .collect(Collectors.toList())
        );
    }

    @Test
    void testDoubleSmallRounding() {
        assertEquals(
                Collections.singletonList(-0x0.0000000000001p-1022),
                Reader.readAll("-0x0.0000000000001p-1022")
                        .stream()
                        .map(it -> ((Reader.ParsedNumber) it).doubleValue())
                        .collect(Collectors.toList())
        );
    }

    @Test
    void testAnnoyingFloats() {
        assertEquals(
                Arrays.asList(1.1754944e-38f, 0x1.921fb6p+2f),
                Reader.readAll("1.1754944e-38 0x1.921fb6p+2")
                        .stream()
                        .map(it -> ((Reader.ParsedNumber) it).floatValue())
                        .collect(Collectors.toList())
        );
    }

    @Test
    void testHexNanUnderscore() {
        Object val = Reader.readAll("nan:0x80_0000").get(0);
        assertEquals(Reader.ParsedNumber.class, val.getClass());
        assertThrows(RuntimeException.class, () ->
                ((Reader.ParsedNumber) val).toFloat(false));
    }

    @Test
    void testRange() {
        assertEquals(
                Collections.singletonList(+0x1.fffffefffffffffffp127f),
                Reader.readAll("+0x1.fffffefffffffffffp127")
                .stream()
                .map(it -> ((Reader.ParsedNumber) it).floatValue())
                .collect(Collectors.toList()));
        assertThrows(RuntimeException.class, () -> Reader
                .readAll("0x1p1024 -0x1p1024 0x1.fffffffffffff8p1023 -0x1.fffffffffffff8p1023")
                .stream()
                .map(it -> ((Reader.ParsedNumber) it).doubleValue())
                .forEach(it -> {}));
    }

    interface SuiteRunner {
        void accept(String scriptName, InputStream source) throws Throwable;
    }

    static Stream<DynamicTest> runForTestSuite(SuiteRunner runner) {
        try {
            return ModuleTestBase.openTestSuite()
                    .filter(entry -> entry.getName().indexOf('/') == -1
                            && entry.getName().endsWith(".wast"))
                    .map(entry -> DynamicTest.dynamicTest(entry.getName(), () ->
                            runner.accept(entry.getName(), new BufferedInputStream(entry.getStream()))))
                    // will be closed!
                    ;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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