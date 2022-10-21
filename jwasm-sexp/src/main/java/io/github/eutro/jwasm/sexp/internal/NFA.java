package io.github.eutro.jwasm.sexp.internal;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public class NFA {
    public static final int EOF = 1 << Byte.SIZE;
    public static final int ALPHABET_SIZE = EOF + 1;

    final BitSet starts = new BitSet();
    final List<State> states = new ArrayList<>();

    public int push() {
        states.add(new State());
        return states.size() - 1;
    }

    void eClosure(BitSet reachable) {
        Deque<Integer> queue = new ArrayDeque<>();
        reachable.stream().forEach(queue::add);
        while (!queue.isEmpty()) {
            int next = queue.removeLast();
            BitSet epsilons = new BitSet();
            epsilons.or(states.get(next).epsilons);
            epsilons.andNot(reachable);
            epsilons.stream().forEach(queue::add);
            reachable.or(epsilons);
        }
    }

    BitSet eClosureNew(BitSet set) {
        BitSet newSet = new BitSet();
        newSet.or(set);
        eClosure(newSet);
        return newSet;
    }

    public BitSet step(BitSet state, int sym) {
        state = eClosureNew(state);

        BitSet next = new BitSet();
        state.stream()
                .mapToObj(states::get)
                .map(it -> it.transitions[sym])
                .forEach(next::or);
        return next;
    }

    @Nullable
    public Token.Type checkType(BitSet state) {
        state = eClosureNew(state);

        return state.stream()
                .mapToObj(states::get)
                .map(it -> it.type)
                .reduce(null, Token.Type::merge);
    }

    static class State {
        @Nullable Token.Type type = null;
        final BitSet[] transitions = new BitSet[ALPHABET_SIZE];

        {
            for (int i = 0; i < transitions.length; i++) {
                transitions[i] = new BitSet();
            }
        }

        final BitSet epsilons = new BitSet();
    }

    public DFA toDfa() {
        DFABuilder builder = new DFABuilder();
        int start = builder.add(starts);
        return new DFA(start, builder.dfaStates.toArray(new DFA.State[0]));
    }

    private class DFABuilder {
        Map<BitSet, Integer> stateMapping = new HashMap<>();

        {
            stateMapping.put(new BitSet(), -1); // map to failure
        }

        List<DFA.State> dfaStates = new ArrayList<>();

        public int add(BitSet nfaState) {
            BitSet canonNfaState = new BitSet();
            canonNfaState.or(nfaState);
            eClosure(canonNfaState);

            Integer maybeMapped = stateMapping.get(canonNfaState);
            if (maybeMapped != null) {
                return maybeMapped;
            }

            int dfaStateIndex = dfaStates.size();
            stateMapping.put(canonNfaState, dfaStateIndex);
            Token.Type type = canonNfaState.stream()
                    .mapToObj(states::get)
                    .map(it -> it.type)
                    .reduce(null, Token.Type::merge);
            DFA.State dfaState = new DFA.State(type);
            dfaStates.add(dfaState);

            BitSet[] allTrans = new BitSet[ALPHABET_SIZE];
            for (int i = 0; i < allTrans.length; i++) {
                allTrans[i] = new BitSet();
            }
            for (State state : (Iterable<? extends State>) canonNfaState
                    .stream()
                    .mapToObj(states::get)::iterator) {
                for (int sym = 0; sym < state.transitions.length; sym++) {
                    allTrans[sym].or(state.transitions[sym]);
                }
            }
            for (int sym = 0; sym < allTrans.length; sym++) {
                dfaState.transitions[sym] = add(allTrans[sym]);
            }

            return dfaStateIndex;
        }
    }
}
