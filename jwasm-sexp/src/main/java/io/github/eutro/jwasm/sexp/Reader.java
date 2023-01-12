package io.github.eutro.jwasm.sexp;

import io.github.eutro.jwasm.ByteInputStream;
import io.github.eutro.jwasm.ValidationException;
import io.github.eutro.jwasm.sexp.internal.DFA;
import io.github.eutro.jwasm.sexp.internal.LexerDFA;
import io.github.eutro.jwasm.sexp.internal.LineCountingPushbackByteInputStream;
import io.github.eutro.jwasm.sexp.internal.Token;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;

import static io.github.eutro.jwasm.sexp.internal.Token.Type.T_BR_CLOSE;

/**
 * A class for reading s-expressions from text.
 * These can then be {@link Parser parsed} into something more meaningful.
 *
 * @see Parser
 * @see Writer
 */
public class Reader {
    /**
     * Read all s-expressions from a source character sequence.
     *
     * @param source The source string to read from.
     * @return The list of s-expressions parsed from the string.
     */
    public static List<Object> readAll(CharSequence source) {
        return readAll(new ByteInputStream.ByteBufferByteInputStream(
                ByteBuffer.wrap(source.toString().getBytes(StandardCharsets.UTF_8))
        ));
    }

    /**
     * Read all s-expressions from an input stream.
     *
     * @param stream The stream to read from.
     * @param <E>    The stream's exception type.
     * @return The list of s-expressions parsed from the string.
     * @throws E If reading from the stream fails.
     */
    public static <E extends Exception> List<Object> readAll(ByteInputStream<E> stream) throws E {
        ListIterator<Token> li = tokenise(stream).listIterator();
        List<Object> res = new ArrayList<>();
        while (li.hasNext()) {
            res.add(read0(li));
        }
        return res;
    }

    /**
     * Read all s-expressions from an input stream.
     *
     * @param stream The stream to read from.
     * @return The list of s-expressions parsed from the string.
     * @throws IOException If reading from the stream fails.
     */
    public static List<Object> readAll(InputStream stream) throws IOException {
        return readAll(new ByteInputStream.InputStreamByteInputStream(stream));
    }

    static Object read0(ListIterator<Token> tokens) {
        Token nextTok = tokens.next();
        switch (nextTok.ty) {
            case T_BR_OPEN: {
                ArrayList<Object> ls = new ArrayList<>();
                done:
                {
                    while (tokens.hasNext()) {
                        if (tokens.next().ty == T_BR_CLOSE) break done;
                        tokens.previous();
                        ls.add(read0(tokens));
                    }
                    throw new ValidationException("Unclosed list");
                }
                return ls;
            }
            case T_BR_CLOSE:
                throw new ValidationException("Unexpected )");
            default:
                return nextTok.interpret();
        }
    }

    public static class MemArgPart {
        public MemArgPart(Type type, BigInteger value) {
            this.type = type;
            this.value = value;
        }

        public enum Type {
            ALIGN,
            OFFSET,
        }

        public final Type type;
        public final BigInteger value;

        public static Function<String, Object> parseWith(Type type) {
            return s -> new MemArgPart(type, Token.parseNumber(s.substring(s.indexOf('=') + 1)).toBigInt());
        }

        @Override
        public String toString() {
            return type.name().toLowerCase(Locale.ROOT) + "=" + value;
        }
    }

    private static final DFA LEXER_DFA = LexerDFA.getDFA();

    static <E extends Exception> List<Token> tokenise(ByteInputStream<E> is) throws E {
        LineCountingPushbackByteInputStream<E> pis = new LineCountingPushbackByteInputStream<>(is);
        List<Token> tokens = new ArrayList<>();
        Token tok;
        try {
            while (true) {
                tok = LEXER_DFA.readNext(pis);
                if (tok == null) break;
                switch (tok.ty) {
                    case T_SPACE:
                    case T_LINE_COMMENT:
                        break;
                    case T_COMMENT_START:
                        comment(pis);
                        break;
                    case T_RESERVED:
                        throwReserved(tok.value);
                        break;
                    case T_INVALID:
                        throw new ValidationException("Illegal token: " + tok.value);
                    default:
                        tokens.add(tok);
                        break;
                }
            }
        } catch (ValidationException e) {
            throw new ValidationException("Error on line: " + pis.getLine() + ", byte: " + pis.getCol(), e);
        }
        return tokens;
    }

    private static void throwReserved(String value) {
        throw new ValidationException("Reserved token: " + value,
                new RuntimeException("unknown operator"));
    }

    private static <E extends Exception> void comment(LineCountingPushbackByteInputStream<E> pis) throws E {
        int depth = 1;
        while (true) {
            int c = pis.get();
            if (c == ';' && pis.get() == ')') {
                if (--depth <= 0) return;
            } else if (c == '(' && pis.get() == ';') {
                depth++;
            }
        }
    }

    public static class ParsedNumber extends Number {
        public final boolean hasSign;
        public final int sign;
        public final BigInteger mantissa, exponent;
        public final ExpType expType;
        public final NanType nanType;
        public final String token;

        public ParsedNumber(String token, int sign, BigInteger mantissa, BigInteger exponent, ExpType expType, boolean hasSign) {
            this(token, hasSign, sign, mantissa, exponent, expType, null);
        }

        public ParsedNumber(String token, boolean hasSign, int sign, BigInteger mantissa, BigInteger exponent, ExpType expType, NanType nanType) {
            this.token = token;
            this.hasSign = hasSign;
            this.sign = sign;
            this.mantissa = mantissa;
            this.exponent = exponent;
            this.expType = expType;
            this.nanType = nanType;
        }

        public boolean isInteger() {
            return mantissa != null && exponent != null && exponent.equals(BigInteger.ZERO);
        }

        @Override
        public int intValue() {
            return (int) doubleValue();
        }

        @Override
        public long longValue() {
            return (long) doubleValue();
        }

        @Override
        public float floatValue() {
            return toFloat(true);
        }

        @Override
        public double doubleValue() {
            return toDouble(true);
        }

        public enum ExpType {
            DEC(10, 10, 50, 350, Pattern.compile("[eE]")),
            HEX(16, 2, 150, 1100, Pattern.compile("[pP]")),
            INF,
            NAN,
            ;

            public final int radix;
            public final int base;
            // actual values depend on base and exponent sign, but we can be liberal here
            public final int floatLimit, doubleLimit;
            public final Pattern expMarker;

            ExpType(int radix, int base, int floatLimit, int doubleLimit, Pattern expMarker) {
                this.radix = radix;
                this.base = base;
                this.floatLimit = floatLimit;
                this.doubleLimit = doubleLimit;
                this.expMarker = expMarker;
            }

            ExpType() {
                this(-1, -1, -1, -1, null);
            }
        }

        public enum NanType {
            CANONICAL(Float.NaN, Double.NaN),
            ARITHMETIC(-Float.NaN, -Double.NaN),
            ;

            private final float floatValue;
            private final double doubleValue;

            NanType(float floatValue, double doubleValue) {
                this.floatValue = floatValue;
                this.doubleValue = doubleValue;
            }

            public float getFloat() {
                return floatValue;
            }

            public double getDouble() {
                return doubleValue;
            }

            @Override
            public String toString() {
                return name().toLowerCase(Locale.ROOT);
            }
        }

        public BigInteger toBigInt() {
            if (!isInteger()) throw new Parser.ParseException("Expected integer", this,
                    new RuntimeException("unexpected token"));
            return mantissa.multiply(BigInteger.valueOf(sign));
        }

        public float toFloat(boolean acceptScriptNan) {
            float v;
            switch (expType) {
                case INF:
                    return sign < 0 ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
                case NAN:
                    if (mantissa == null) {
                        if (nanType != null && !acceptScriptNan) {
                            throw new Parser.ParseException(nanType + " NaN forbidden outside of scripts", this,
                                    new RuntimeException("unexpected token"));
                        }
                        v = (nanType == null ? NanType.CANONICAL : nanType).getFloat();
                        if (sign < 0) v = -v;
                    } else {
                        int FLOAT_SIGNIF = 23;
                        if (mantissa.equals(BigInteger.ZERO)
                                || mantissa.compareTo(BigInteger.valueOf(1L << FLOAT_SIGNIF)) >= 0) {
                            throw new Parser.ParseException("Value out of range for float NaN payload", this,
                                    new RuntimeException("constant out of range"));
                        }
                        return Float.intBitsToFloat(((sign < 0
                                ? 0x8000_0000
                                : 0x0000_0000)
                                | 0x7F80_0000
                                | mantissa.intValue()));
                    }
                    break;
                case DEC:
                case HEX: {
                    v = Float.parseFloat(toParsableString());
                    break;
                }
                default:
                    throw new AssertionError();
            }
            if (Float.isInfinite(v)) {
                throw new Parser.ParseException("Value out of range for float", this,
                        new RuntimeException("constant out of range"));
            }
            return v;
        }

        public double toDouble(boolean acceptScriptNan) {
            double v;
            switch (expType) {
                case INF:
                    return sign < 0 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                case NAN:
                    if (mantissa == null) {
                        if (nanType != null && !acceptScriptNan) {
                            throw new Parser.ParseException(nanType + " NaN forbidden outside of scripts", this,
                                    new RuntimeException("unexpected token"));
                        }
                        v = (nanType == null ? NanType.CANONICAL : nanType).getDouble();
                        if (sign < 0) v = -v;
                    } else {
                        int DOUBLE_SIGNIF = 52;
                        if (mantissa.equals(BigInteger.ZERO)
                                || mantissa.compareTo(BigInteger.valueOf(1L << DOUBLE_SIGNIF)) >= 0) {
                            throw new Parser.ParseException("Value out of range for double NaN payload", this,
                                    new RuntimeException("constant out of range"));
                        }
                        return Double.longBitsToDouble((sign < 0
                                ? 0x8000_0000_0000_0000L
                                : 0x0000_0000_0000_0000L)
                                | 0x7FF0_0000_0000_0000L
                                | mantissa.longValue());
                    }
                    break;
                case DEC:
                case HEX: {
                    v = Double.parseDouble(toParsableString());
                    break;
                }
                default:
                    throw new AssertionError();
            }
            if (Double.isInfinite(v)) {
                throw new Parser.ParseException("Value out of range for double", this,
                        new RuntimeException("constant out of range"));
            }
            return v;
        }

        public static ParsedNumber of(Object obj) {
            if (obj instanceof String) {
                return Token.parseNumber((String) obj);
            } else if (obj instanceof Float) {
                float f = (float) obj;
                if (Float.isInfinite(f)) {
                    return f < 0 ? of("-inf") : of("inf");
                }
                int asInt = Float.floatToRawIntBits(f);
                if (Float.isNaN(f)) {
                    int canonicalBits = Float.floatToRawIntBits(Float.NaN);
                    // Java optimises -Float.NaN to just NaN, so no toRawIntBits(-Float.NaN)
                    int negCanonicalBits = canonicalBits | 0x8000_0000;
                    if (asInt == canonicalBits) {
                        return of("nan");
                    } else if (asInt == negCanonicalBits) {
                        return of("-nan");
                    } else {
                        return of(String.format(
                                      "%snan:0x%x",
                                      asInt < 0 ? "-" : "",
                                      asInt & ~negCanonicalBits
                                  ));
                    }
                }
                ParsedNumber ret = of(obj.toString());
                if (Float.floatToRawIntBits(ret.floatValue()) == asInt) return ret;
                ret = of(String.format("%g", obj));
                if (Float.floatToRawIntBits(ret.floatValue()) == asInt) return ret;
                throw new IllegalStateException();
            } else if (obj instanceof Double) {
                double f = (double) obj;
                if (Double.isInfinite(f)) {
                    return f < 0 ? of("-inf") : of("inf");
                }
                long asInt = Double.doubleToRawLongBits(f);
                if (Double.isNaN(f)) {
                    long canonicalBits = Double.doubleToRawLongBits(Double.NaN);
                    long negCanonicalBits = canonicalBits | 0x80000000_00000000L;
                    if (asInt == canonicalBits) {
                        return of("nan");
                    } else if (asInt == negCanonicalBits) { 
                        return of("-nan");
                    } else {
                        return of(String.format(
                                      "%snan:0x%x",
                                      asInt < 0 ? "-" : "",
                                      asInt & ~negCanonicalBits
                                  ));
                    }
                }
                ParsedNumber ret = of(obj.toString());
                if (Double.doubleToRawLongBits(ret.doubleValue()) == asInt) return ret;
                ret = of(String.format("%g", obj));
                if (Double.doubleToRawLongBits(ret.doubleValue()) == asInt) return ret;
                throw new IllegalStateException();
            } else {
                return of(obj.toString());
            }
        }

        private String toParsableString() {
            if (expType == ExpType.HEX && token.indexOf('p') == -1 && token.indexOf('P') == -1) {
                return token + "p0";
            } else {
                return token;
            }
        }

        @Override
        public String toString() {
            return token;
        }
    }
}
