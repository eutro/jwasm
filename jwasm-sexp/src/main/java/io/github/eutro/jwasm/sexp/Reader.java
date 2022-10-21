package io.github.eutro.jwasm.sexp;

import io.github.eutro.jwasm.ByteInputStream;
import io.github.eutro.jwasm.ValidationException;
import io.github.eutro.jwasm.sexp.internal.DFA;
import io.github.eutro.jwasm.sexp.internal.LexerDFA;
import io.github.eutro.jwasm.sexp.internal.LineCountingPushbackByteInputStream;
import io.github.eutro.jwasm.sexp.internal.Token;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.function.Function;

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
     * @return The list of s-expressions parsed from the string.
     */
    public static <E extends Exception> List<Object> readAll(ByteInputStream<E> stream) throws E {
        ListIterator<Token> li = tokenise(stream).listIterator();
        List<Object> res = new ArrayList<>();
        while (li.hasNext()) {
            res.add(read0(li));
        }
        return res;
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
            return s -> new MemArgPart(type, (BigInteger) Token.parseInt(s.substring(s.indexOf('=') + 1)));
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

    public static class BigFloat extends Number {
        public final int sign;
        public final BigInteger mantissa, exponent;
        public final ExpType expType;
        public final NanType nanType;

        public BigFloat(int sign, BigInteger mantissa, BigInteger exponent, ExpType expType) {
            this(sign, mantissa, exponent, expType, null);
        }

        public BigFloat(int sign, BigInteger mantissa, BigInteger exponent, ExpType expType, NanType nanType) {
            this.sign = sign;
            this.mantissa = mantissa;
            this.exponent = exponent;
            this.expType = expType;
            this.nanType = nanType;
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
            INF,
            NAN,
            HEX,
            DEC,
        }

        public enum NanType {
            CANONICAL,
            ARITHMETIC,
            ;

            @Override
            public String toString() {
                return name().toLowerCase(Locale.ROOT);
            }
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
                        v = Float.NaN;
                    } else {
                        int FLOAT_SIGNIF = 23;
                        if (mantissa.equals(BigInteger.ZERO)
                                || mantissa.compareTo(BigInteger.valueOf(1L << FLOAT_SIGNIF)) >= 0) {
                            throw new Parser.ParseException("Value out of range for float NaN", this,
                                    new RuntimeException("constant out of range"));
                        }
                        v = Float.intBitsToFloat(mantissa.intValue());
                    }
                    break;
                case DEC: {
                    v = mantissa.floatValue() * (float) Math.pow(10., exponent.doubleValue());
                    break;
                }
                case HEX: {
                    v = mantissa.floatValue() * (float) Math.pow(2., exponent.doubleValue());
                    break;
                }
                default:
                    throw new AssertionError();
            }
            if (Float.isInfinite(v)) {
                throw new Parser.ParseException("Value out of range for float", this,
                        new RuntimeException("constant out of range"));
            }
            return sign < 0 ? -v : v;
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
                        v = Double.NaN;
                    } else {
                        int DOUBLE_SIGNIF = 52;
                        if (mantissa.equals(BigInteger.ZERO)
                                || mantissa.compareTo(BigInteger.valueOf(1L << DOUBLE_SIGNIF)) >= 0) {
                            throw new Parser.ParseException("Value out of range for double NaN", this,
                                    new RuntimeException("constant out of range"));
                        }
                        v = Double.longBitsToDouble(mantissa.longValue());
                    }
                    break;
                case DEC: {
                    v = mantissa.doubleValue() * Math.pow(10., exponent.doubleValue());
                    break;
                }
                case HEX: {
                    v = mantissa.doubleValue() * Math.pow(2., exponent.doubleValue());
                    break;
                }
                default:
                    throw new AssertionError();
            }
            if (Double.isInfinite(v)) {
                throw new Parser.ParseException("Value out of range for double", this,
                        new RuntimeException("constant out of range"));
            }
            return sign < 0 ? -v : v;
        }

        @Override
        public String toString() {
            return "BigFloat{" +
                    "sign=" + sign +
                    ", mantissa=" + mantissa +
                    ", exponent=" + exponent +
                    ", expType=" + expType +
                    '}';
        }
    }
}