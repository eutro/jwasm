package io.github.eutro.jwasm.sexp.internal;

import io.github.eutro.jwasm.ByteInputStream;
import io.github.eutro.jwasm.ByteOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.BitSet;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static io.github.eutro.jwasm.sexp.internal.RegExp.*;
import static io.github.eutro.jwasm.sexp.internal.Token.Type.*;

public class LexerDFA {
    public static DFA getDFA() {
        try {
            return DFA.readFrom(new ByteInputStream.InputStreamByteInputStream(
                    new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(getData())))
            ));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Print out the compiled DFA data as base 64 for inclusion in {@link #getData()}.
     *
     * @param args Program arguments (ignored)
     */
    public static void main(String[] args) {
        byte[] rawBytes;
        {
            ByteOutputStream.BaosByteOutputStream bos = new ByteOutputStream.BaosByteOutputStream();
            buildNfa().toDfa().minimise().writeTo(bos);
            rawBytes = bos.toByteArray();
        }

        byte[] compressedBytes;
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (OutputStream os = new GZIPOutputStream(baos)) {
                os.write(rawBytes);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
            compressedBytes = baos.toByteArray();
        }

        System.out.printf("Raw: %d bytes\n", rawBytes.length);
        System.out.printf("Compressed: %d bytes\n", compressedBytes.length);
        System.out.println(Base64.getEncoder().encodeToString(compressedBytes));
    }

    private static NFA buildNfa() {
        RegExp
                sign = union('+', '-'),
                digit = range('0', '9'),
                hexdigit = union(
                        digit,
                        range('a', 'f'),
                        range('A', 'F')
                ),
                num = concat(digit,
                        concat(
                                literal("_").optional(),
                                digit
                        ).star()),
                hexnum = concat(hexdigit,
                        concat(
                                literal("_").optional(),
                                hexdigit
                        ).star()),
                letter = union(range('a', 'z'), range('A', 'Z')),
                symbol = union("+-*/\\^~=<>!?@#$%&|:`.'".toCharArray()),
                space = union(" \t\n\r".toCharArray()),
                eof = eof(),
                noNl = notRange((byte) '\n', (byte) '\n').star(),
                line = concat(noNl, union(eof, literal("\n"))),
                lineComment = concat(literal(";;"), line),
                nestedCommentStart = literal("(;"),
                character = union(
                        union(((Supplier<byte[]>) () -> {
                            BitSet bits = new BitSet();
                            bits.set(0, Byte.toUnsignedInt((byte) -1));
                            bits.set('"', false);
                            bits.set('\\', false);
                            bits.set(0x00, 0x1f, false);
                            bits.set(0x7f, false);
                            return bitSetToBytes(bits);
                        }).get()),
                        concat(literal("\\"), union("nrt\\'\"".toCharArray())),
                        concat(literal("\\"), hexdigit, hexdigit),
                        concat(literal("\\{"), hexnum, literal("}"))
                ),
                nat = union(concat(literal("0x"), hexnum), num),
                integer = concat(sign.optional(), nat),
                floating = concat(
                        sign.optional(),
                        union(
                                concat(num,
                                        concat(literal("."), num.optional()).optional(),
                                        concat(union('e', 'E'), sign.optional(), num).optional()),
                                concat(literal("0x"), hexnum,
                                        concat(literal("."), hexnum.optional()).optional(),
                                        concat(union('p', 'P'), sign.optional(), num).optional()),
                                literal("inf"),
                                concat(literal("nan"),
                                        concat(
                                                literal(":"),
                                                union(
                                                        concat(literal("0x"), hexnum),
                                                        literal("canonical"),
                                                        literal("arithmetic")
                                                )
                                        ).optional())
                        )
                ),
                stringDelim = literal("\""),
                string = concat(stringDelim, character.star(), stringDelim),
                idchar = union(letter, digit, literal("_"), symbol),
                name = idchar.plus(),
                id = concat(literal("$"), name),
                keyword = concat(range('a', 'z'),
                        union(letter, digit, union('_', '.', ':'))
                                .star()),
                reserved = union(idchar, string).plus(),
                paropen = literal("("),
                parclose = literal(")"),
                alignEq = concat(literal("align="), nat),
                offsetEq = concat(literal("offset="), nat);
        NFA nfa = new NFA();
        addSym(nfa, T_COMMENT_START, nestedCommentStart);
        addSym(nfa, T_LINE_COMMENT, lineComment);
        addSym(nfa, T_BR_OPEN, paropen);
        addSym(nfa, T_BR_CLOSE, parclose);
        addSym(nfa, T_FLOAT, floating);
        addSym(nfa, T_INT, integer);
        addSym(nfa, T_STRING, string);
        addSym(nfa, T_OFFSET_EQ, offsetEq);
        addSym(nfa, T_ALIGN_EQ, alignEq);
        addSym(nfa, T_KEYWORD, keyword);
        addSym(nfa, T_ID, id);
        addSym(nfa, T_SPACE, space);
        addSym(nfa, T_RESERVED, reserved);
        return nfa;
    }

    private static byte[] bitSetToBytes(BitSet bits) {
        byte[] bytes = new byte[bits.size()];
        int i = 0;
        for (int b : (Iterable<? extends Integer>) bits.stream()::iterator) {
            bytes[i++] = (byte) b;
        }
        return bytes;
    }

    private static void addSym(NFA nfa, Token.Type type, RegExp re) {
        StatePair statePair = re.addToNfa(nfa);
        nfa.starts.set(statePair.start);
        nfa.states.get(statePair.end).type = type;
    }

    private static String getData() {
        return "H4sIAAAAAAAAAO2ch3bTMBRAHbnDHRCgQAtlU1bZe7cfxQfo5M8oe8+y997zC4iD40axpSc9xy9yrKuTnMTPcvVunxTHx613htcYb1Kp1B88QYUFbDFjbMkyNsbHGFs+2YSdZmp4vTF21psT2o341W3vU1us2cKeyaFQM1qzYBBdZlDDAfP//7o5a/zChQrQqRB1nCBJiBrz1Tv4vs98LH3wLhx98M7QcAB78uOnkP4YYYO8pxD3G4/oYAPwz6bAfD0QFC5sgDogx0eBuYPBGGED1AE5PgpydzCgjFtRHjUWqHcIgmAowDIM78LRB+8MnPe5z0a9uRDETyEjMcIGeU8hHjQe0cFGcWPuNObrQWsZtWyAOiDHR4G5g0UxwgaoA3J8FOTuYFQZt6E8dL4vVP1qSP1FlVcF2t4m4MAeYZwgSYCKc8D7M313XtqxcXSTYXf9gA84B5hrKC1X2czOEzeFT1vsO09kpg5WsMk0B+NSB2tk8bW2OPDN62BljJYDKE6QJED+DlZb78BsPZjgE4ytiumR742INbHnHCDmgqGDCesdIOogdT0och108bPRGgeIOkh1UOQ6QDhYFxNlGL2ROlDHCZKE8jN1sB50MC7muBaIEyQJgPhc2BATOYjeSHKE4gRJAuTvYKP1DhDrAeSAiTlCcYIkAcjvP9hcYgdTsjhBkgCI9QDnQBonSBKAog62Ogci2+xzkGkubC/tXNjRmoOOg13scK85EHLQcSBhusAOdsZEGUZvpA7UcYIkARDrgaGDaesddGUu7C6vgz2pcYIkAbq0Ju4tqYN96XGCJAGo62B/iR0ckMYJkgTo0npwsKwODqXGCZIEoHGQegGlVA6OKOMESQKQrwdHnQPGjjkHx9vjBEkCUDo4kRonSBKAxsFJRZwgSQAKB6eUcYIkAYjXg6lknCBJiEz36s50bBhdpcZmYmZnysjsiLtnm3um68Gcx2T/yyAkPaZuXHXYc2BXAksA1kuEuxJYArBY4nknMZvEC64SO1OJC+1iiSRe8sq6KCJO1y97VxINf4HXqhP3IXMbSReaNq56sug1z/mwsjwQkwWtA4oTpAuAsHHdS7YoV2GT1InOXgSpAyCmTUYz9SWjGGoKcepz0/IPbSsl3irYmY/xzV7uO3USiyrxjpOYZyXetVxip6bzPTed1bRLvJ9PJT6sv3xVGomyfEgrcb7gEh94ydaUKG4zkojtSmAJALMm5iFxvsgSCzWdHzmJphIfu0rMoxLD9sRJ1Jf4VLsrgSUAayUutGdOornE54ZdCSwBWChR3V44iSYSX7pKxEnUvxzrJKZKfI3sSmAJwCKJ8h3eOInZJc55b53ErBLfuUrESnyv2ZXAEoBlEj8guhJYArBI4kdkVwJLABZJTA+qzxydRKnEz64Ss0v84iRmlyhvX51EPYnfDLsSWAKwUKK6fe9xiT/cXxUoaLsB9qf3K9F65EbpwNxG0oWmjd/Su+j/WPNXBXb4sKY8EJMFrQOKE6QLgLDx10u2KFdhk9SJzl4EqQMgpk1GM/Uloxhq/gHdLaV5gX8AAA==";
    }
}
