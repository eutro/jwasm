package io.github.eutro.jwasm.sexp.internal;

import io.github.eutro.jwasm.ByteInputStream;
import io.github.eutro.jwasm.ByteOutputStream;
import io.github.eutro.jwasm.ValidationException;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DFA {
    public final int start;
    public final State[] states;

    DFA(int start, State[] states) {
        this.start = start;
        this.states = states;
    }

    public int step(int state, int sym) {
        return states[state].transitions[sym];
    }

    @Nullable
    public Token.Type checkType(int state) {
        return states[state].type;
    }

    @Nullable
    public <E extends Exception> Token readNext(LineCountingPushbackByteInputStream<E> pis) throws E {
        int state = start;
        if (state == -1) return null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (true) {
            int c = pis.get();
            int nextState = step(state, c == -1 ? NFA.EOF : c);
            if (nextState == -1) {
                if (c != -1) pis.unread(c);
                break;
            }
            baos.write(c);
            state = nextState;
        }
        if (baos.size() == 0) return null;
        Token.Type type = checkType(state);
        if (type == null) type = Token.Type.T_INVALID;

        CharBuffer decoded;
        try {
            decoded = StandardCharsets.UTF_8
                    .newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(baos.toByteArray()));
        } catch (CharacterCodingException e) {
            ValidationException ve = new ValidationException("Input stream contains invalid UTF-8",
                    new RuntimeException("malformed UTF-8 encoding"));
            ve.addSuppressed(e);
            throw ve;
        }
        return new Token(type, decoded.toString(), pis.srcLoc());
    }

    public static class State {
        public final @Nullable Token.Type type;
        public final int[] transitions;

        State(@Nullable Token.Type type, int[] transitions) {
            this.type = type;
            this.transitions = transitions;
        }

        State(@Nullable Token.Type type) {
            this(type, new int[NFA.ALPHABET_SIZE]);
            Arrays.fill(transitions, -1);
        }

        static <E extends Exception> State readFrom(ByteInputStream<E> stream) throws E {
            int typeIndex = stream.getVarSInt32();
            Token.Type type = typeIndex < 0 ? null : Token.Type.values()[typeIndex];
            int len = stream.getVarUInt32();
            int[] transitions = new int[len];
            for (int i = 0; i < transitions.length; i++) {
                transitions[i] = stream.getVarSInt32();
            }
            return new State(type, transitions);
        }

        <E extends Exception> void writeTo(ByteOutputStream<E> stream) throws E {
            stream.putVarSInt(type == null ? -1 : type.ordinal());
            stream.putVarUInt(transitions.length);
            for (int trans : transitions) {
                stream.putVarSInt(trans);
            }
        }
    }

    public static <E extends Exception> DFA readFrom(ByteInputStream<E> stream) throws E {
        int start = stream.getVarUInt32();
        int len = stream.getVarUInt32();
        State[] states = new State[len];
        for (int i = 0; i < states.length; i++) {
            states[i] = State.readFrom(stream);
        }
        return new DFA(start, states);
    }

    <E extends Exception> void writeTo(ByteOutputStream<E> stream) throws E {
        stream.putVarUInt(start);
        stream.putVarUInt(states.length);
        for (State state : states) {
            state.writeTo(stream);
        }
    }


    public DFA minimise() {
        // states are distinguishable iff there exists a string for which they produce a different match

        // we will be starting with this optimistic "all states are indistinguishable" to
        // the more pessimistic (and realistic) result that some states just aren't indistinguishable

        // table of which states are distinguishable from each other; the extra cells represent a failed match
        boolean[][] distinguishable = new boolean[states.length + 1][states.length + 1];

        // the diagonal is already set to false (all states are indistinguishable from themselves)

        // states with differing finish types are distinguishable
        for (int i = 0; i < states.length; ++i) {
            for (int j = i + 1; j < states.length; ++j) {
                distinguishable[i][j] = distinguishable[j][i]
                        = states[i].type != states[j].type;
            }
        }

        // all states are distinguishable from failure (except failure itself)
        for (int i = 0; i < states.length; i++) {
            distinguishable[i][states.length] = true;
            distinguishable[states.length][i] = true;
        }
        distinguishable[states.length][states.length] = false;

        // then just iterate until fixed point...
        boolean changed;
        do {
            changed = false;
            // check every "indistinguishable" state pair
            // and see if they transition to distinguishable states,
            // in which case they too are distinguishable
            for (int i = 0; i < states.length - 1; i++) {
                for (int j = i + 1; j < states.length; ++j) {
                    if (distinguishable[i][j]) continue;

                    // check every symbol
                    for (int sym = 0; sym < NFA.ALPHABET_SIZE; ++sym) {
                        int iNext = states[i].transitions[sym];
                        int iTrans = iNext == -1 ? states.length : iNext;

                        int jNext = states[j].transitions[sym];
                        int jTrans = jNext == -1 ? states.length : jNext;

                        if (distinguishable[iTrans][jTrans]) {
                            distinguishable[i][j] = distinguishable[j][i] = true;
                            changed = true;
                            break;
                        }
                    }
                }
            }
        } while (changed);

        // merge indistinguishable states
        List<State> mergedStates = new ArrayList<>();
        Map<Integer, Integer> grouped = new HashMap<>();
        iLoop:
        for (int i = 0; i < states.length; i++) {
            for (int j = 0; j < states.length; j++) {
                if (distinguishable[i][j]) continue;
                Integer jGrouped = grouped.get(j);
                if (jGrouped != null) {
                    grouped.put(i, jGrouped);
                    continue iLoop;
                }
            }
            int newState = mergedStates.size();
            grouped.put(i, newState);
            mergedStates.add(new State(states[i].type));
        }

        // rebuild transitions
        for (int i = 0; i < states.length; i++) {
            int groupedI = grouped.get(i);
            int[] transitions = states[i].transitions;
            for (int sym = 0; sym < transitions.length; sym++) {
                int target = transitions[sym];
                if (target == -1) continue;
                mergedStates.get(groupedI).transitions[sym] = grouped.get(target);
            }
        }

        return new DFA(grouped.get(start), mergedStates.toArray(new State[0]));
    }
}
