package io.github.eutro.jwasm.sexp;

import io.github.eutro.jwasm.ByteInputStream;
import io.github.eutro.jwasm.ValidationException;
import io.github.eutro.jwasm.sexp.internal.DFA;
import io.github.eutro.jwasm.sexp.internal.LexerDFA;
import io.github.eutro.jwasm.sexp.internal.LineCountingPushbackByteInputStream;
import io.github.eutro.jwasm.sexp.internal.Token;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

import static io.github.eutro.jwasm.sexp.internal.Token.Type.T_BR_CLOSE;

/**
 * A class for reading s-expressions from text.
 * These can then be {@link WatParser parsed} into something more meaningful.
 *
 * @see WatParser
 * @see WatWriter
 */
public class WatReader<E extends Exception> {
    private final LineCountingPushbackByteInputStream<E> stream;

    /**
     * Map from object to position for things with identity.
     */
    private @Nullable Map<Object, SrcLoc> sources;

    /**
     * Construct a reader over the given stream.
     *
     * @param stream The stream to read s-expressions from.
     */
    public WatReader(ByteInputStream<E> stream) {
        this.stream = new LineCountingPushbackByteInputStream<>(stream);
    }

    /**
     * Construct a reader from the given source character sequence.
     *
     * @param source The source string to read from.
     * @return A reader over the string.
     */
    public static WatReader<RuntimeException> fromCharSequence(CharSequence source) {
        return new WatReader<>(new ByteInputStream.ByteBufferByteInputStream(
                ByteBuffer.wrap(source.toString().getBytes(StandardCharsets.UTF_8))
        ));
    }

    /**
     * Construct a reader from the given input stream.
     *
     * @param stream The source stream to read from.
     * @return A reader over the stream.
     */
    public static WatReader<IOException> fromInputStream(InputStream stream) {
        return new WatReader<>(new ByteInputStream.InputStreamByteInputStream(stream));
    }

    /**
     * Read all s-expressions from the stream.
     *
     * @return The list of s-expressions parsed from the stream.
     * @throws E If reading from the stream fails.
     */
    public List<Object> readAll() throws E {
        List<Object> res = new ArrayList<>();
        while (true) {
            List<Token> toks = tokenise(stream);
            if (toks.isEmpty()) break;
            ListIterator<Token> li = toks.listIterator();
            while (li.hasNext()) {
                res.add(read0(li));
            }
        }
        return res;
    }

    /**
     * Read a single s-expression from the stream, if there is one.
     *
     * @return The s-expression, or {@link Optional#empty()} if the end of the stream has been reached.
     * @throws E If reading from the stream fails.
     */
    public Optional<Object> readNext() throws E {
        List<Token> toks = tokenise(stream);
        if (toks.isEmpty()) return Optional.empty();
        return Optional.of(read0(toks.listIterator()));
    }

    /**
     * Read all s-expressions from a source character sequence.
     *
     * @param source The source string to read from.
     * @return The list of s-expressions parsed from the string.
     */
    public static List<Object> readAll(CharSequence source) {
        return fromCharSequence(source).readAll();
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
        return new WatReader<E>(stream).readAll();
    }

    /**
     * Read all s-expressions from an input stream.
     *
     * @param stream The stream to read from.
     * @return The list of s-expressions parsed from the string.
     * @throws IOException If reading from the stream fails.
     */
    public static List<Object> readAll(InputStream stream) throws IOException {
        return fromInputStream(stream).readAll();
    }

    Object read0(ListIterator<Token> tokens) {
        Object result;
        Token firstTok = tokens.next();
        switch (firstTok.ty) {
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
                result = ls;
                break;
            }
            case T_BR_CLOSE:
                throw new ValidationException("Unexpected )");
            default:
                result = firstTok.interpret();
                break;
        }
        if (sources != null) {
            sources.put(result, firstTok.loc);
        }
        return result;
    }

    /**
     * Retrieve the sources map last set with {@link #setSources(Map)}, or null if it hasn't been called.
     *
     * @return The sources map.
     */
    public @Nullable Map<Object, SrcLoc> getSources() {
        return sources;
    }

    /**
     * Set the sources map, further {@link #readNext() reads} will associate new lists, strings and numbers
     * with their source.
     *
     * @param sources The sources map.
     */
    public void setSources(@Nullable Map<Object, SrcLoc> sources) {
        this.sources = sources;
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

    static <E extends Exception> List<Token> tokenise(LineCountingPushbackByteInputStream<E> pis) throws E {
        List<Token> tokens = new ArrayList<>();
        Token tok;
        int depth = 0;
        boolean hasTok = false;
        try {
            do {
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
                    case T_BR_OPEN:
                        tokens.add(tok);
                        hasTok = true;
                        depth++;
                        break;
                    case T_BR_CLOSE:
                        depth--;
                    default:
                        tokens.add(tok);
                        hasTok = true;
                        break;
                }
            } while (!hasTok || depth > 0);
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
            if (!isInteger()) throw new WatParser.ParseException("Expected integer", this,
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
                            throw new WatParser.ParseException(nanType + " NaN forbidden outside of scripts", this,
                                    new RuntimeException("unexpected token"));
                        }
                        v = (nanType == null ? NanType.CANONICAL : nanType).getFloat();
                        if (sign < 0) {
                            // JLS does not guarantee that -v actually flips the sign on NaN
                            v = Float.intBitsToFloat(Float.floatToRawIntBits(v) | 0x8000_0000);
                        }
                    } else {
                        int FLOAT_SIGNIF = 23;
                        if (mantissa.equals(BigInteger.ZERO)
                                || mantissa.compareTo(BigInteger.valueOf(1L << FLOAT_SIGNIF)) >= 0) {
                            throw new WatParser.ParseException("Value out of range for float NaN payload", this,
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
                throw new WatParser.ParseException("Value out of range for float", this,
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
                            throw new WatParser.ParseException(nanType + " NaN forbidden outside of scripts", this,
                                    new RuntimeException("unexpected token"));
                        }
                        v = (nanType == null ? NanType.CANONICAL : nanType).getDouble();
                        if (sign < 0) {
                            // JLS does not guarantee that -v actually flips the sign on NaN
                            v = Double.longBitsToDouble(Double.doubleToRawLongBits(v) | 0x8000_0000_0000_0000L);
                        }
                    } else {
                        int DOUBLE_SIGNIF = 52;
                        if (mantissa.equals(BigInteger.ZERO)
                                || mantissa.compareTo(BigInteger.valueOf(1L << DOUBLE_SIGNIF)) >= 0) {
                            throw new WatParser.ParseException("Value out of range for double NaN payload", this,
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
                throw new WatParser.ParseException("Value out of range for double", this,
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
                    long negCanonicalBits = canonicalBits | 0x8000_0000_0000_0000L;
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
