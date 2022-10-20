package io.github.eutro.jwasm.sexp.internal;

import io.github.eutro.jwasm.sexp.Reader;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Token {
    public final Type ty;
    public final String value;

    public Token(Type ty, String value) {
        this.ty = ty;
        this.value = value;
    }

    public static Object parseInt(String num) {
        Object o = parseFloat(num);
        if (!(o instanceof BigInteger)) throw new IllegalStateException();
        return o;
    }

    public static Object parseFloat(String num) {
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

    public static Object parseString(String str) {
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

    public static void writeUTF8CodePoint(ByteArrayOutputStream baos, int codePoint) {
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

    public Object interpret() {
        return ty.interpret.apply(value);
    }

    public enum Type {
        T_KEYWORD,
        T_ID,
        T_INT(Token::parseInt),
        T_FLOAT(Token::parseFloat),
        T_STRING(Token::parseString),
        T_BR_OPEN($ -> {
            throw new IllegalStateException();
        }),
        T_BR_CLOSE(T_BR_OPEN.interpret),
        T_RESERVED(T_BR_OPEN.interpret),
        T_OFFSET_EQ(Reader.MemArgPart.parseWith(Reader.MemArgPart.Type.OFFSET)),
        T_ALIGN_EQ(Reader.MemArgPart.parseWith(Reader.MemArgPart.Type.ALIGN)),

        T_COMMENT_START,
        T_LINE_COMMENT,
        T_SPACE,
        T_INVALID,
        ;

        final Function<String, Object> interpret;

        Type(Function<String, Object> interpret) {
            this.interpret = interpret;
        }

        Type() {
            this(x -> x);
        }

        public static Type merge(Type lhs, Type rhs) {
            if (lhs == rhs) return lhs;

            if (lhs == null) return rhs;
            if (rhs == null) return lhs;

            // prefer anything to a reserved token
            if (lhs == T_RESERVED) return rhs;
            if (rhs == T_RESERVED) return lhs;

            // prefer anything else to keyword (e.g. nan or inf)
            if (lhs == T_KEYWORD) return rhs;
            if (rhs == T_KEYWORD) return lhs;

            // prefer T_INT over T_FLOAT
            if (lhs == T_INT && rhs == T_FLOAT || lhs == T_FLOAT && rhs == T_INT) {
                return T_INT;
            }

            throw new IllegalStateException("overlapping tokens " + lhs + " and " + rhs);
        }
    }
}
