package io.github.eutro.jwasm.sexp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;

public class ErrorAlts {
    private static final ErrorAlts DEFAULT_ALTS = new ErrorAlts()
            .addAlts("i32 constant out of range", "i32 constant")
            .addAlts("unexpected end of section or function", "END opcode expected", "section size mismatch", "unexpected end")
            .addAlts("unexpected end", "length out of bounds", "unexpected end of section or function")
            .addAlts("malformed limit", "integer too large")
            .addAlts("multiple start sections", "unexpected content after last section")
            .addAlts("alignment", "alignment must be a power of two")
            .addAlts("constant out of range", "wrong number of lane literals")
            .addAlts("Unexpected )", "malformed lane index")
            .addAlts("malformed functype", "unexpected end", "malformed limit", "integer representation too long")
            .addAlts("unexpected token", "wrong number of lane literals", "invalid lane length", "unknown operator")
            .addAltsRev("unexpected token", "malformed lane index", "unknown operator")
            .addAltsRev("integer representation too long", "unexpected end", "malformed limit")
            .addAlts("unknown memory", Pattern.compile("unknown memory \\d+"))
            .addAlts("unknown global", Pattern.compile("unknown global \\d+"))
            .addAlts("unknown table", Pattern.compile("unknown table \\d+"))
            .addAlts("unknown data segment", Pattern.compile("unknown data segment \\d+"))
            .addAlts("unknown elem segment", Pattern.compile("unknown elem segment \\d+"))
            .addAlts("unknown function", Pattern.compile("unknown function \\d+"))
            .addAlts("unknown local", Pattern.compile("unknown local \\d+"))

            .addAlts("type mismatch", "invalid lane index") // remove me when simd_lane tests are fixed
            ;

    public static ThreadLocal<ErrorAlts> CURRENT_INSTANCE = ThreadLocal.withInitial(() -> DEFAULT_ALTS);

    public boolean isEquivalent(String failure, String actualMessage) {
        if (Objects.equals(failure, actualMessage)) return true;
        for (Pattern pattern : patterns.getOrDefault(actualMessage, emptySet())) {
            if (pattern.matcher(failure).matches()) return true;
        }
        for (Pattern pattern : patternsRev.getOrDefault(failure, emptySet())) {
            if (pattern.matcher(actualMessage).matches()) return true;
        }
        return false;
    }

    private final Map<String, Set<Pattern>> patterns = new HashMap<>();
    private final Map<String, Set<Pattern>> patternsRev = new HashMap<>();

    public ErrorAlts addAlts(String actualMessage, String... failures) {
        return addAlts(actualMessage, catRaw(failures));
    }

    public ErrorAlts addAlts(String actualMessage, Pattern pattern) {
        patterns.computeIfAbsent(actualMessage, $ -> new HashSet<>()).add(pattern);
        return this;
    }

    public ErrorAlts addAltsRev(String failure, String... actualMessages) {
        return addAltsRev(failure, catRaw(actualMessages));
    }

    public ErrorAlts addAltsRev(String failure, Pattern pattern) {
        patternsRev.computeIfAbsent(failure, $ -> new HashSet<>()).add(pattern);
        return this;
    }

    @NotNull
    private static Pattern catRaw(String[] failures) {
        return Pattern.compile(Arrays.stream(failures)
                .map(Pattern::quote)
                .collect(Collectors.joining("|")));
    }

    public static void assertFailure(String failure, @Nullable Throwable t) {
        if (t == null) {
            Assertions.fail("Expected failure <" + failure + ">, but no exception was thrown.");
        }
        Throwable root = getRootCause(t);
        if (!CURRENT_INSTANCE.get().isEquivalent(failure, root.getMessage())) {
            Assertions.fail("Expected failure <" + failure + ">, but got <" + root.getMessage() + ">.", t);
        }
    }

    public static Throwable getRootCause(Throwable t) {
        Throwable rootCause = t;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }
}
