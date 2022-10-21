package io.github.eutro.jwasm.sexp.internal;

import io.github.eutro.jwasm.sexp.Parser;
import io.github.eutro.jwasm.sexp.Reader;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ListParser {
    public final List<?> list;
    public final ListIterator<?> iter;

    public ListParser(List<?> list) {
        this(list, list.listIterator());
    }

    public ListParser(List<?> list, int i) {
        this(list, list.listIterator(i));
    }

    ListParser(List<?> list, ListIterator<?> iter) {
        this.list = list;
        this.iter = iter;
    }

    public static boolean isId(Object it) {
        return it instanceof String && ((String) it).startsWith("$");
    }

    public static <T> T expectClass(Class<T> clazz, Object obj) {
        if (!clazz.isInstance(obj)) {
            throw new Parser.ParseException("Expected " + clazz.getSimpleName(), obj,
                    new RuntimeException("unexpected token"));
        }
        return clazz.cast(obj);
    }

    public static List<?> expectList(Object obj) {
        if (!(obj instanceof List)) {
            throw new Parser.ParseException("Expected list", obj,
                    new RuntimeException("unexpected token"));
        }
        return (List<?>) obj;
    }

    public static void expectEq(Object expected, Object val) {
        if (!Objects.equals(expected, val)) {
            throw new Parser.ParseException("expected " + expected, val);
        }
    }

    public static <T> T mark(Object obj, Supplier<T> f) {
        try {
            return f.get();
        } catch (Parser.ParseException e) {
            if (e.in != obj) {
                throw new Parser.ParseException(e, obj);
            } else {
                throw e;
            }
        } catch (RuntimeException e) {
            throw new Parser.ParseException("An error occurred", obj, e);
        }
    }

    public static String parseUtf8(Object obj) {
        byte[] bytes = expectClass(byte[].class, obj);
        CharBuffer buf;
        try {
            buf = StandardCharsets.UTF_8
                    .newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes));
        } catch (CharacterCodingException e) {
            throw new Parser.ParseException("Error decoding string", e,
                    new RuntimeException("malformed UTF-8 encoding"));
        }
        return buf.toString();
    }

    public static byte[] parseV128Const(ListParser lp, boolean acceptScriptNan) {
        String shape = expectClass(String.class, lp.expect());
        byte[] value = new byte[16];
        ByteBuffer buf = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
        String msg = "wrong number of lane literals";
        switch (shape) {
            case "i8x16":
                for (int i = 0; i < 16; i++) {
                    buf.put((byte) parseIX(lp.expect(msg), 8));
                }
                break;
            case "i16x8":
                for (int i = 0; i < 8; i++) {
                    buf.putShort((short) parseIX(lp.expect(msg), 16));
                }
                break;
            case "i32x4":
                for (int i = 0; i < 4; i++) {
                    buf.putInt(parseI32(lp.expect(msg)));
                }
                break;
            case "i64x2":
                for (int i = 0; i < 2; i++) {
                    buf.putLong(parseI64(lp.expect(msg)));
                }
                break;
            case "f32x4":
                for (int i = 0; i < 4; i++) {
                    buf.putFloat(parseF32(lp.expect(msg), acceptScriptNan));
                }
                break;
            case "f64x2":
                for (int i = 0; i < 2; i++) {
                    buf.putDouble(parseF64(lp.expect(msg), acceptScriptNan));
                }
                break;
            default:
                throw new Parser.ParseException("Unrecognised vector shape", shape);
        }
        return value;
    }

    private static long parseIX(Object obj, int x) {
        BigInteger bigInt = expectBigInt(obj);
        if (bigInt.compareTo(BigInteger.ONE.shiftLeft(x - 1).negate()) < 0
                || bigInt.compareTo(BigInteger.ONE.shiftLeft(x).subtract(BigInteger.ONE)) > 0) {
            throw new Parser.ParseException("i" + x + " constant out of range", bigInt,
                    new RuntimeException("constant out of range"));
        }
        return bigInt.longValue();
    }

    public static BigInteger expectBigInt(Object obj) {
        return expectClass(Reader.ParsedNumber.class, obj).toBigInt();
    }

    public static int parseI32(Object obj) {
        return (int) parseIX(obj, 32);
    }

    public static long parseI64(Object obj) {
        return parseIX(obj, 64);
    }

    public static float parseF32(Object val) {
        return parseF32(val, false);
    }

    public static float parseF32(Object val, boolean acceptScriptNan) {
        float f;
        if (val instanceof Reader.ParsedNumber) {
            f = ((Reader.ParsedNumber) val).toFloat(acceptScriptNan);
        } else {
            throw new Parser.ParseException("Expected float", val,
                    val instanceof String ? new RuntimeException("unknown operator") : null);
        }
        return f;
    }

    public static double parseF64(Object val) {
        return parseF64(val, false);
    }

    public static double parseF64(Object val, boolean acceptScriptNan) {
        double f;
        if (val instanceof Reader.ParsedNumber) {
            f = ((Reader.ParsedNumber) val).toDouble(acceptScriptNan);
        } else {
            throw new Parser.ParseException("Expected double", val,
                    val instanceof String ? new RuntimeException("unknown operator") : null);
        }
        return f;
    }

    public Object expect(String msg) {
        if (!iter.hasNext()) throw new Parser.ParseException("Expected more terms", list,
                new RuntimeException(msg));
        return iter.next();
    }

    public Object expect() {
        return expect("unexpected token");
    }

    public Optional<Object> maybeParse(Predicate<Object> pred) {
        if (!iter.hasNext()) return Optional.empty();
        Object next = iter.next();
        if (pred.test(next)) return Optional.of(next);
        iter.previous();
        return Optional.empty();
    }

    public Optional<String> maybeParseId() {
        return maybeParse(ListParser::isId)
                .map(String.class::cast);
    }

    public void expectEnd() {
        if (iter.hasNext()) {
            throw new Parser.ParseException("too many terms", list);
        }
    }

    public Optional<Object> peek() {
        if (!iter.hasNext()) return Optional.empty();
        Object val = iter.next();
        iter.previous();
        return Optional.of(val);
    }
}
