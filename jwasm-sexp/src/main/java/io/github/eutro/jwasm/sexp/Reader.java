package io.github.eutro.jwasm.sexp;

import io.github.eutro.jwasm.ValidationException;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.eutro.jwasm.sexp.Reader.Token.Type.*;

public class Reader {
    public static final Pattern SIGN = Pattern.compile("[+\\-]");
    public static final Pattern DIGIT = Pattern.compile("\\d");
    public static final Pattern HEXDIGIT = Pattern.compile("[\\da-fA-F]");
    public static final Pattern NUM = Pattern.compile(DIGIT + "(?:_?" + DIGIT + ")*");
    public static final Pattern HEXNUM = Pattern.compile(HEXDIGIT + "(?:_?" + HEXDIGIT + ")*");

    public static final Pattern LETTER = Pattern.compile("[a-zA-Z]");
    public static final Pattern SYMBOL = Pattern.compile("[+\\-*/\\\\^~=<>!?@#$%&|:`.']");

    public static final Pattern SPACE = Pattern.compile("[ \t\n\r]");
    public static final Pattern CONTROL = Pattern.compile("[\\x00-\\x1f]");
    public static final Pattern EOF = Pattern.compile("$");
    public static final Pattern NO_NL = Pattern.compile("[^\n]*");
    public static final Pattern LINE = Pattern.compile(NO_NL + "(\n|$)");

    public static final Pattern LINE_COMMENT = Pattern.compile(";;" + LINE);
    public static final Pattern NESTED_COMMENT_START = Pattern.compile("\\(;");
    public static final Pattern NESTED_COMMENT_END = Pattern.compile(";\\)");
    public static final Pattern NESTED_COMMENT_ANY = Pattern.compile(NESTED_COMMENT_START + "|" + NESTED_COMMENT_END);

    public static final Pattern ESCAPE = Pattern.compile("[nrt\\\\'\"]");
    public static final Pattern CHARACTER =
            Pattern.compile("[^\"\\\\\\x00-\\x1f]"
                    + "|\\\\" + ESCAPE
                    + "|\\\\" + HEXDIGIT + HEXDIGIT
                    + "|\\\\u\\{" + HEXNUM + "}");

    public static final Pattern NAT = Pattern.compile("(" + "0x" + HEXNUM + "|" + NUM + ")");
    public static final Pattern INT = Pattern.compile("" + SIGN + NAT);
    public static final Pattern FRAC = NUM;
    public static final Pattern HEXFRAC = HEXNUM;
    public static final Pattern FLOAT = Pattern.compile(
            // @formatter:off
            SIGN + "?"
                    + "(" + "(" + NUM +
                                "(" + "(\\.(" + FRAC + ")?)?[eE]" + SIGN + "?" + NUM +
                                "|" + "\\.(" + FRAC + ")?" + "))"
                    + "|" + "(0x" + HEXNUM + "(\\.(" + HEXFRAC + ")?)?" + "([pP]" + SIGN + "?" + NUM + ")?)"
                    + "|" + "inf"
                    + "|" + "nan(:" + "(0x" + HEXNUM + "|canonical|arithmetic))?"
                    + ")"
            // @formatter:on
    );

    public static final Pattern STRING_START = Pattern.compile("\"");
    public static final Pattern STRING_END = STRING_START;

    public static final Pattern IDCHAR = Pattern.compile(LETTER + "|" + DIGIT + "|_|" + SYMBOL);
    public static final Pattern NAME = Pattern.compile("(?:" + IDCHAR + ")" + "+");
    public static final Pattern ID = Pattern.compile("\\$" + NAME);

    public static final Pattern KEYWORD = Pattern.compile("[a-z](" + LETTER + "|" + DIGIT + "|" + "[_.:]" + ")+");
    public static final Pattern RESERVED = Pattern.compile(
            "(" + IDCHAR + ")+"
                    + "|[,;\\[\\]{}]"
    );

    public static final Pattern PAROPEN = Pattern.compile("\\(");
    public static final Pattern PARCLOSE = Pattern.compile("\\)");

    public static final Pattern ALIGN_EQ = Pattern.compile("align=" + NAT);
    public static final Pattern OFFSET_EQ = Pattern.compile("offset=" + NAT);

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

        static Function<String, Object> parseWith(Type type) {
            return s -> new MemArgPart(type, (BigInteger) parseInt(s.substring(s.indexOf('=') + 1)));
        }

        @Override
        public String toString() {
            return type.name().toLowerCase(Locale.ROOT) + "=" + value;
        }
    }

    public static class Token {
        public final Type ty;
        public final String value;

        public Token(Type ty, String value) {
            this.ty = ty;
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Token token = (Token) o;
            return ty == token.ty && Objects.equals(value, token.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ty, value);
        }

        Object interpret() {
            return ty.interpret.apply(value);
        }

        public enum Type {
            T_KEYWORD,
            T_ID,
            T_INT(Reader::parseInt),
            T_FLOAT(Reader::parseFloat),
            T_STRING(Reader::parseString),
            T_BR_OPEN($ -> {
                throw new IllegalStateException();
            }),
            T_BR_CLOSE(T_BR_OPEN.interpret),
            T_RESERVED(T_BR_OPEN.interpret),
            T_OFFSET_EQ(MemArgPart.parseWith(MemArgPart.Type.OFFSET)),
            T_ALIGN_EQ(MemArgPart.parseWith(MemArgPart.Type.ALIGN)),
            ;

            final Function<String, Object> interpret;

            Type(Function<String, Object> interpret) {
                this.interpret = interpret;
            }

            Type() {
                this(x -> x);
            }
        }
    }

    static Object parseInt(String num) {
        Object o = parseFloat(num);
        if (!(o instanceof BigInteger)) throw new IllegalStateException();
        return o;
    }

    static Object parseFloat(String num) {
        num = num.replaceAll("_", "");

        int i = 0;
        int sign = 1;
        if (num.startsWith("-")) {
            sign = -1;
            i++;
        } else if (num.startsWith("+")) {
            i++;
        }
        int radix = 10;
        if (num.startsWith("0x", i)) {
            i += 2;
            radix = 16;
        }

        if (num.startsWith("inf", i)) {
            return Double.POSITIVE_INFINITY * sign;
        }

        if (num.startsWith("nan", i)) {
            if (!num.startsWith("nan:", i)) {
                return Double.NaN;
            }
            i += 4;
            if (num.startsWith("0x", i)) {
                i += 2;
                long bits = Long.parseLong(num.substring(i), 16);
                if ((int) bits == bits) {
                    return Float.intBitsToFloat((int) bits);
                } else {
                    return Double.longBitsToDouble(bits);
                }
            }
            String nanType = num.substring(i);
            switch (nanType) {
                case "canonical":
                case "arithmetic": // ???
                    return Double.NaN;
                default:
                    throw new UnsupportedOperationException("nan:" + nanType);
            }
        }

        Matcher matcher = Pattern.compile(radix == 10 ? "[eE]" : "[pP]")
                .matcher(num);
        String base;
        double exponent;
        if (matcher.region(i, num.length()).find()) {
            base = num.substring(i, matcher.start());
            exponent = new BigInteger(num.substring(matcher.end())).doubleValue();
        } else {
            base = num.substring(i);
            exponent = 1;
        }

        double value;
        if (radix == 10) {
            if (exponent == 1 && base.indexOf('.') == -1) {
                return new BigInteger(base);
            }
            value = Double.parseDouble(base);
        } else {
            String[] parts = base.split("\\.", 2);
            String whole = parts[0];

            double fracPart, wholePart;
            if (parts.length == 2) {
                String frac = parts[1];
                if (frac.length() == 0) {
                    fracPart = 0.;
                } else {
                    fracPart = new BigInteger(frac, 16).doubleValue();
                    fracPart /= BigInteger.ONE.shiftLeft(frac.length() * 4).doubleValue();
                }
            } else if (exponent == 1) {
                return new BigInteger(whole, 16);
            } else {
                fracPart = 0;
            }

            wholePart = new BigInteger(whole, 16).doubleValue();

            value = wholePart + fracPart;
        }
        value *= sign;
        value = Math.pow(value, exponent);

        return value;
    }

    static Object parseString(String str) {
        char[] chars = str.toCharArray();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 1; i < chars.length - 1; i++) {
            int codePoint;
            if (chars[i] == '\\') {
                switch (chars[++i]) {
                    case 'n':
                        baos.write('\n');
                        continue;
                    case 'r':
                        baos.write('\r');
                        continue;
                    case 't':
                        baos.write('\t');
                        continue;
                    case '\\':
                        baos.write('\\');
                        continue;
                    case '\'':
                        baos.write('\'');
                        continue;
                    case '\"':
                        baos.write('\"');
                        continue;
                    case 'u':
                        ++i;
                        codePoint = 0;
                        while (chars[i] != '}') {
                            if (chars[i] == '_') {
                                ++i;
                                continue;
                            }
                            codePoint <<= 4;
                            codePoint |= Character.digit(chars[i], 16);
                            ++i;
                        }
                        break;
                    default: {
                        int lo = Character.digit(chars[i++], 16);
                        int hi = Character.digit(chars[i], 16);
                        baos.write(lo << 4 | hi);
                        continue;
                    }
                }
            } else {
                if (Character.isHighSurrogate(chars[i])) {
                    codePoint = Character.toCodePoint(chars[i], chars[i + 1]);
                    ++i;
                } else {
                    codePoint = chars[i];
                }
            }
            writeUTF8CodePoint(baos, codePoint);
        }
        return baos.toByteArray();
    }

    static void writeUTF8CodePoint(ByteArrayOutputStream baos, int codePoint) {
        // Java it should not be this hard...
        if (codePoint <= 0x7F) {
            baos.write(codePoint);
        } else if (codePoint <= 0x7FF) {
            baos.write(0b11000000
                    | (0b00011111 & (codePoint >>> 6)));
            baos.write(0b10000000
                    | (0b00111111 & codePoint));
        } else if (codePoint <= 0xFFFF) {
            baos.write(0b11100000
                    | (0b00001111 & (codePoint >>> 12)));
            baos.write(0b10000000
                    | (0b00111111 & (codePoint >>> 6)));
            baos.write(0b10000000
                    | (0b00111111 & codePoint));
        } else {
            baos.write(0b11110000
                    | (0b00000111 & (codePoint >>> 18)));
            baos.write(0b10000000
                    | (0b00111111 & (codePoint >>> 12)));
            baos.write(0b10000000
                    | (0b00111111 & (codePoint >>> 6)));
            baos.write(0b10000000
                    | (0b00111111 & codePoint));
        }
    }

    private static Token token(Matcher m) {
        do {
            if (m.usePattern(NESTED_COMMENT_START).lookingAt()) {
                comment(m);
                continue;
            }
            if (m.usePattern(LINE_COMMENT).lookingAt() || m.usePattern(SPACE).lookingAt()) {
                m.region(m.end(), m.regionEnd());
                continue;
            }

            if (m.usePattern(PAROPEN).lookingAt()) return new Token(T_BR_OPEN, m.group());
            if (m.usePattern(PARCLOSE).lookingAt()) return new Token(T_BR_CLOSE, m.group());
            if (m.usePattern(FLOAT).lookingAt()) return new Token(T_FLOAT, m.group());
            if (m.usePattern(NAT).lookingAt()) return new Token(T_INT, m.group());
            if (m.usePattern(INT).lookingAt()) return new Token(T_INT, m.group());
            if (m.usePattern(STRING_START).lookingAt()) {
                // strings can be huge and contain weird characters,
                // Java's regex isn't really suited for it, so let's just go manually here
                int start = m.regionStart();
                StringBuilder sb = new StringBuilder(m.group());
                m.usePattern(CHARACTER);
                m.region(m.end(), m.regionEnd());
                while (m.lookingAt()) {
                    sb.append(m.group());
                    m.region(m.end(), m.regionEnd());
                }
                if (!m.usePattern(STRING_END).lookingAt()) {
                    throw new ValidationException("illegal string literal");
                }
                sb.append(m.group());
                if (m.usePattern(IDCHAR).lookingAt()) {
                    throw new ValidationException("reserved token");
                }

                // reset it for the outer loop...
                m.region(start, m.regionEnd());
                return new Token(T_STRING, sb.toString());
            }
            if (m.usePattern(OFFSET_EQ).lookingAt()) return new Token(T_OFFSET_EQ, m.group());
            if (m.usePattern(ALIGN_EQ).lookingAt()) return new Token(T_ALIGN_EQ, m.group());
            if (m.usePattern(KEYWORD).lookingAt()) return new Token(T_KEYWORD, m.group());
            if (m.usePattern(ID).lookingAt()) return new Token(T_ID, m.group());

            if (m.usePattern(EOF).lookingAt()) return null;

            if (m.usePattern(RESERVED).lookingAt()) {
                throw new ValidationException("reserved token");
            }
            if (m.usePattern(CONTROL).lookingAt()) throw new ValidationException("misplaced control character");
            throw new ValidationException("misplaced unicode character");
        } while (true);
    }

    private static void comment(Matcher m) {
        int depth = 1;
        while (depth > 0) {
            if (!m.usePattern(NESTED_COMMENT_ANY).find()) {
                throw new ValidationException("unclosed comment");
            }
            if (";)".equals(m.group())) {
                depth--;
                m.region(m.end(), m.regionEnd());
            } else depth++;
        }
    }

    static List<Token> tokenise(CharSequence cs) {
        Matcher matcher = ID.matcher(cs);
        List<Token> tokens = new ArrayList<>();
        Token tok;
        try {
            while ((tok = token(matcher)) != null) {
                tokens.add(tok);
                matcher.region(matcher.regionStart() + tok.value.length(), matcher.regionEnd());
            }
        } catch (ValidationException | StackOverflowError e) {
            String preds = cs.subSequence(0, matcher.regionStart()).toString();
            String[] lines = preds.split("\n");
            e.addSuppressed(new RuntimeException(
                    "error occurred at " +
                            "line: " + lines.length +
                            " col: " + lines[lines.length - 1].length()
            ));
            throw e;
        }
        return tokens;
    }

    public static List<Object> readAll(CharSequence source) {
        ListIterator<Token> li = tokenise(source).listIterator();
        List<Object> res = new ArrayList<>();
        while (li.hasNext()) {
            res.add(read0(li));
        }
        return res;
    }

    private static Object read0(ListIterator<Token> tokens) {
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
                    throw new ValidationException("unclosed list");
                }
                return ls;
            }
            case T_BR_CLOSE:
                throw new ValidationException("unexpected )");
            default:
                return nextTok.interpret();
        }
    }
}
