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
        addSym(nfa, T_NUMBER, union(integer, floating));
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
        // Raw: 32641 bytes
        // Compressed: 1160 bytes
        return "H4sIAAAAAAAAAO2ch3abMBSGscggo3XbtE3adDdd6d47eag+AMdv1nTvme6993yCgouJZZCudIFrYfTr2MfW5RL9n69kICTOGb/B/JZqteDhJ1RjHlvMGFuyjI35Y4wtn2yJnWZy+UFj7Kwzx7Ub8avbzqeOWKuFmcmhUGukYcAguqwBBQbM/f9x+6z5gXMVoFIh8jiBSUgN5so3cF2XuVj1wZv46J3noyYDmJMbP4Xqj8V1iDO5uNt8RDsbgH82hfTXAw7hQgeUgBwfhfQZDMbiOqAE5PgoVDiDAWnciPJoME++ged5Qx5Ww/AmPnrn+Sj4HOx3o9pc8OKnUCOxuA5xJhf3mo9oZ6O4Mect/fWgvYzaOqAE5PgopM9gUSyuA0pAjo9ChTMYlcZNKA+V84W6Ww8VvKj7dU4dbxPygS3COIFJQDXLwO/LdO68NLdxdFND9vqB328ZYK6htF1l0ztO3BQ+bTHvOJHpMljBJtMYjAsZrBHF15aWAWMrYykxgOIEJiF/hTNYbTwDvfVgwp9gbFWsHjlvRKyJPccAMRc0GUwYzwBRB6nrQaXqIL/vRmMYIOoglUGZ6wDBYF2syGH0RshAHicwCfnTZbAeZDDOe1wLxAlMQv7062BDrIhB9EbgEYoTmIT8Fc5go/EMEOsBxIDxHqE4gUlA5PcfbK4wgylRnMAk5M/+ro2kDrZaBry2mccg01zYXtm5sKPdgwqDXexwrzHgPKgwEGi6xAx2xoocRm+EDORxApOQv8IZTBvPoCtzYXd1GexJjROYBNSlNXFvRRnsS48TmAREXQf7K8zggDBOYBJQl9aDg1VlcCg1TmASEA2D1AsolWJwRBonMAmIfD04ahkwdswyON4ZJzAJiJLBidQ4gUlANAxOSuIEJgFRMDgljROYBES8Hkwl4wQmIWW6V3cmt2F0VQ02E2t2poqaHbb3bPuO7now5zDR/zIIlR6TN1+223NgKgElQMZDhFMJKAEyGOJ5CzEbxAu2EvOpxIV2sUIQLzlVXRQRh+uXnSuJhr/Aa9SBu6dPI8lCkcZVRxS95lgeRpYHYrKgcUBxAruAEDSuO8kWeeW6hExUtiKwDggxbTKSCZaMcqApxaHPTcO/tI2EeKtkRz7aN3vZc+qkDKrEOxZikZV413CIeU3ne3Y6y9UJ8X4xlfgwePmqMhBFfkgrcb7kEB84ydaCyPdpQcSmElCCABkCcb7MEEs1nR9ZiLoQH9tKLKISw/bEQlSH+FQ5lYASIGMhLrRnFqI+xOeaqQSUABkIUd5eWIg6EF/aSsRBVL8cayGmQnyNTCWgBMggiOIN3liI2SHOOW8txKwQ39lKxEJ8r5hKQAmQYRA/IFIJKAEyCOJHZCoBJUAGQUwPyo8cLUQhxM+2ErND/GIhZocobl8tRDWI3zRTCSgBMhCivH3vcYg/7F8VSNRxA+xP51ei9ciN0oP6NJIsFGn8Ft5F/8eYvyowg4cx5YGYLGgcUJzALiAEjb9OskVeuS4hE5WtCKwDQkybjGSCJaMcaP4BBuxwT4F/AAA=";
    }
}
