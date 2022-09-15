package io.github.eutro.jwasm.sexp;

import io.github.eutro.jwasm.ValidationException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
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
    public static final Pattern NO_NL = Pattern.compile("[^\n]+");
    public static final Pattern LINE = Pattern.compile(NO_NL + "(\n|$)");

    public static final Pattern LINE_COMMENT = Pattern.compile(";;" + LINE);
    public static final Pattern NESTED_COMMENT_START = Pattern.compile("\\(;");
    public static final Pattern NESTED_COMMENT_END = Pattern.compile(";\\)");
    public static final Pattern NESTED_COMMENT_ANY = Pattern.compile(NESTED_COMMENT_START + "|" + NESTED_COMMENT_END);

    public static final Pattern ESCAPE = Pattern.compile("[nrt\\\\'\"]");
    public static final Pattern CHARACTER =
            Pattern.compile("[^\"\\\\\\x00-\\x1f\\x7f-\\xff]"
                    + "|\\\\" + ESCAPE
                    + "|\\\\" + HEXDIGIT + HEXDIGIT
                    + "|\\\\u\\{" + HEXNUM + "}");

    public static final Pattern NAT = Pattern.compile(NUM + "|0x" + HEXNUM);
    public static final Pattern INT = Pattern.compile("" + SIGN + NAT);
    public static final Pattern FRAC = NUM;
    public static final Pattern HEXFRAC = HEXNUM;
    public static final Pattern FLOAT = Pattern.compile(
            SIGN + "?" + NUM + "\\." + FRAC + "?" +
                    "|" + SIGN + "?" + NUM + "(\\." + FRAC + "?)?[eE]" + SIGN + "?" + NUM +
                    "|" + SIGN + "?" + "0x" + HEXNUM + "\\." + HEXFRAC + "?" +
                    "|" + SIGN + "?" + "0x" + HEXNUM + "(\\." + HEXFRAC + "?)?[pP]" + SIGN + "?" + NUM +
                    "|" + SIGN + "?" + "inf" +
                    "|" + SIGN + "?" + "nan" +
                    "|" + SIGN + "?" + "nan:" + "0x" + HEXNUM
    );

    public static final Pattern STRING_START = Pattern.compile("\"" + "(?:" + CHARACTER + ")*");
    public static final Pattern STRING = Pattern.compile("\"" + "(?:" + CHARACTER + ")*" + "\"");
    public static final Pattern STRING_UNCLOSED = Pattern.compile("\"" + "(?:" + CHARACTER + ")*" + "(\n|$)");
    public static final Pattern STRING_ILLEGAL_CONTROL = Pattern.compile("\"" + "(?:" + CHARACTER + ")*" + "[\\x00-\\x09\\x0b-\\x1f\\x7f]");
    public static final Pattern STRING_ILLEGAL_ESCAPE = Pattern.compile("\"" + "(?:" + CHARACTER + ")*" + "\\\\.");

    public static final Pattern IDCHAR = Pattern.compile(LETTER + "|" + DIGIT + "|_|" + SYMBOL);
    public static final Pattern NAME = Pattern.compile("(?:" + IDCHAR + ")" + "+");
    public static final Pattern ID = Pattern.compile("\\$" + NAME);

    public static final Pattern KEYWORD = Pattern.compile("[a-z](" + LETTER + "|" + DIGIT + "|" + "[_.:]" + ")+");
    public static final Pattern RESERVED = Pattern.compile(
            "(" + IDCHAR + "|" + STRING + ")+"
            + "|[,;\\[\\]{}]"
    );

    public static final Pattern IXX = Pattern.compile("i(32|64)");
    public static final Pattern FXX = Pattern.compile("f(32|64)");
    public static final Pattern NXX = Pattern.compile(IXX + "|" + FXX);
    public static final Pattern VXXX = Pattern.compile("v128");
    public static final Pattern MIXX = Pattern.compile("i(8|16|32|64)");
    public static final Pattern MFXX = Pattern.compile("f(32|64)");
    public static final Pattern SIGNED = Pattern.compile("[su]");
    public static final Pattern MEM_SIZE = Pattern.compile("8|16|32");
    public static final Pattern V128_INT_SHAPE = Pattern.compile("i8x16|i16x8|i32x4|i64x2");
    public static final Pattern V128_FLOAT_SHAPE = Pattern.compile("f32x4|f64x2");
    public static final Pattern V128_SHAPE = Pattern.compile(V128_INT_SHAPE + "|" + V128_FLOAT_SHAPE);

    public static final Pattern PAROPEN = Pattern.compile("\\(");
    public static final Pattern PARCLOSE = Pattern.compile("\\)");

    public static final Pattern ALIGN_EQ = Pattern.compile("align=" + NAT);
    public static final Pattern OFFSET_EQ = Pattern.compile("offset=" + NAT);

    public static Object token(Matcher m) {
        do {
            if (m.usePattern(PAROPEN).lookingAt()) return "LPAR";
            if (m.usePattern(PARCLOSE).lookingAt()) return "RPAR";
            if (m.usePattern(NAT).lookingAt()) return m.group();
            if (m.usePattern(INT).lookingAt()) return m.group();
            if (m.usePattern(FLOAT).lookingAt()) return m.group();
            if (m.usePattern(STRING_START).lookingAt()) {
                m.region(m.start(), m.regionEnd());
                if (m.usePattern(STRING).lookingAt()) return m.group();
                if (m.usePattern(STRING_UNCLOSED).lookingAt()) throw new ValidationException("unclosed string literal");
                if (m.usePattern(STRING_ILLEGAL_CONTROL).lookingAt())
                    throw new ValidationException("illegal control character in string literal");
                if (m.usePattern(STRING_ILLEGAL_ESCAPE).lookingAt()) throw new ValidationException("illegal escape");
                throw new ValidationException("illegal string literal");
            }
            if (m.usePattern(KEYWORD).lookingAt()) {
                return m.group();
            }
            if (m.usePattern(OFFSET_EQ).lookingAt()) return m.group();
            if (m.usePattern(ALIGN_EQ).lookingAt()) return m.group();
            if (m.usePattern(ID).lookingAt()) return m.group();

            if (m.usePattern(LINE_COMMENT).lookingAt()) continue;
            if (m.usePattern(NESTED_COMMENT_START).lookingAt()) {
                comment(m);
                continue;
            }
            if (m.usePattern(SPACE).lookingAt()) continue;
            if (m.usePattern(EOF).lookingAt()) return null;

            if (m.usePattern(RESERVED).lookingAt()) throw new ValidationException("reserved token");
            if (m.usePattern(CONTROL).lookingAt()) throw new ValidationException("misplaced control character");
            throw new ValidationException("misplaced unicode character");
        } while (true);
    }

    public static void comment(Matcher m) {
        int depth = 1;
        while (depth > 0) {
            if (!m.usePattern(NESTED_COMMENT_ANY).find()) {
                throw new ValidationException("unclosed comment");
            }
            if (";)".equals(m.group())) depth--;
            else depth++;
        }
    }
}
