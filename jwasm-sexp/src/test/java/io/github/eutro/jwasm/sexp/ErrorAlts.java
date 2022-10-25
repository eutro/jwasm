package io.github.eutro.jwasm.sexp;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import java.util.*;

import static java.util.Collections.singleton;

public class ErrorAlts {
    private static final Map<String, Set<String>> ALT_ERROR_NAMES = new HashMap<String, Set<String>>() {{
        put("i32 constant", singleton("i32 constant out of range"));
        // The reference interpreter differs here, defining a finite set of operators, catching
        // unrecognised operators when lexing. I'd rather keep our lexer (and reader) more extensible,
        // but that does mean we can't differentiate.
        put("unexpected token", new HashSet<>(Arrays.asList(
                "unknown operator",
                "malformed lane index"
        )));

        put("END opcode expected", singleton("unexpected end of section or function"));
        put("section size mismatch", singleton("unexpected end of section or function"));
        put("length out of bounds", singleton("unexpected end"));
        put("unexpected end of section or function", singleton("unexpected end"));

        put("integer too large", singleton("malformed limit"));
        put("unexpected end", singleton("unexpected end of section or function"));
        put("unexpected content after last section", singleton("multiple start sections"));
        put("alignment must be a power of two", singleton("alignment"));
        put("wrong number of lane literals", new HashSet<>(Arrays.asList(
                "unexpected token",
                "constant out of range"
        )));
        put("invalid lane length", singleton("unexpected token"));
        put("malformed lane index", singleton("Unexpected )"));

        // many "integer representation too long" tests are broken in other funny ways instead...
        put("integer representation too long", new HashSet<>(Arrays.asList(
                "malformed functype",
                "unexpected end",
                "malformed limit"
        )));

        put("unknown memory 0", singleton("unknown memory"));
        put("unknown memory 1", singleton("unknown memory"));
        put("unknown global 0", singleton("unknown global"));
        put("unknown global 1", singleton("unknown global"));
        put("unknown table 0", singleton("unknown table"));
        put("unknown data segment 1", singleton("unknown data segment"));
        put("unknown elem segment 0", singleton("unknown elem segment"));
        put("unknown elem segment 4", singleton("unknown elem segment"));
        put("unknown function 7", singleton("unknown function"));
        put("unknown local 2", singleton("unknown local"));
    }};

    public static boolean check(String expected, String got) {
        return expected.equals(got) ||
                ALT_ERROR_NAMES.getOrDefault(expected, Collections.emptySet())
                        .contains(got);
    }

    public static void assertFailure(String failure, @Nullable Throwable t) {
        if (t == null) {
            Assertions.fail("Expected failure <" + failure + ">, but no exception was thrown.");
        }
        Throwable root = getRootCause(t);
        if (!check(failure, root.getMessage())) {
            Assertions.fail("Expected failure <" + failure +  ">, but got <" + root.getMessage() + ">.", t);
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
