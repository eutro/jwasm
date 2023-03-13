package io.github.eutro.jwasm.sexp.internal;

import io.github.eutro.jwasm.ByteList;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class RegExp {
    enum Type {
        LITERAL,
        UNION,
        CONCAT,
        KLEENE,
        EOF,
    }

    RegExp(Type type, List<RegExp> children, List<Byte> syms) {
        this.type = type;
        this.children = children;
        this.syms = syms;
    }

    final Type type;
    final List<RegExp> children;
    final List<Byte> syms;

    public static RegExp union(RegExp... children) {
        return new RegExp(Type.UNION, Arrays.asList(children), Collections.emptyList());
    }

    public static RegExp union(byte... bytes) {
        return new RegExp(Type.UNION, Collections.emptyList(), new ByteList(bytes));
    }

    public static RegExp notRange(byte from, byte to) {
        return union(range(Byte.MIN_VALUE, (byte) (from - 1)),
                range((byte) (to + 1), Byte.MAX_VALUE));
    }

    private static byte charToByteExact(char c) {
        if (c <= 0x7F) {
            return (byte) c;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static RegExp union(char... chars) {
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = charToByteExact(chars[i]);
        }
        return union(bytes);
    }

    public static RegExp range(byte from, byte to) {
        return new RegExp(Type.UNION,
                Collections.emptyList(),
                IntStream.rangeClosed(from, to)
                        .mapToObj(it -> (byte) it)
                        .collect(Collectors.toList())
        );
    }

    public static RegExp range(char from, char to) {
        return range(charToByteExact(from), charToByteExact(to));
    }

    public static RegExp concat(RegExp... children) {
        return new RegExp(Type.CONCAT, Arrays.asList(children), Collections.emptyList());
    }

    public static RegExp literal(byte... bytes) {
        return new RegExp(Type.LITERAL, Collections.emptyList(), new ByteList(bytes));
    }

    public static RegExp literal(String str) {
        return literal(str.getBytes(StandardCharsets.UTF_8));
    }

    public static RegExp eof() {
        return new RegExp(Type.EOF, Collections.emptyList(), Collections.emptyList());
    }

    public RegExp star() {
        return new RegExp(Type.KLEENE, Collections.singletonList(this), Collections.emptyList());
    }

    public RegExp plus() {
        return concat(this, this.star());
    }

    public RegExp optional() {
        return union(concat(), this);
    }

    public static class StatePair {
        public final int start, end;

        public StatePair(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    public StatePair addToNfa(NFA nfa) {
        switch (type) {
            case LITERAL: {
                int start = nfa.push();
                int end = start;
                for (byte sym : syms) {
                    BitSet transitions = nfa.states.get(end).transitions[Byte.toUnsignedInt(sym)];
                    transitions.set(end = nfa.push());
                }
                return new StatePair(start, end);
            }
            case UNION: {
                int start = nfa.push(), end = nfa.push();
                for (RegExp child : children) {
                    StatePair childPair = child.addToNfa(nfa);
                    nfa.states.get(start).epsilons.set(childPair.start);
                    nfa.states.get(childPair.end).epsilons.set(end);
                }
                for (byte sym : syms) {
                    nfa.states.get(start).transitions[Byte.toUnsignedInt(sym)].set(end);
                }
                return new StatePair(start, end);
            }
            case CONCAT: {
                if (children.isEmpty()) {
                    int start = nfa.push();
                    return new StatePair(start, start);
                } else {
                    int start, end;
                    Iterator<RegExp> it = children.iterator();
                    StatePair firstPair = it.next().addToNfa(nfa);
                    start = firstPair.start;
                    end = firstPair.end;
                    while (it.hasNext()) {
                        StatePair nextPair = it.next().addToNfa(nfa);
                        nfa.states.get(end).epsilons.set(nextPair.start);
                        end = nextPair.end;
                    }
                    return new StatePair(start, end);
                }
            }
            case KLEENE: {
                RegExp child = children.get(0);
                StatePair childPair = child.addToNfa(nfa);
                nfa.states.get(childPair.start).epsilons.set(childPair.end);
                nfa.states.get(childPair.end).epsilons.set(childPair.start);
                return childPair;
            }
            case EOF: {
                int start = nfa.push(), end = nfa.push();
                nfa.states.get(start).transitions[NFA.EOF].set(end);
                return new StatePair(start, end);
            }
        }
        throw new IllegalStateException();
    }
}
