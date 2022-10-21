package io.github.eutro.jwasm.sexp.internal;

import io.github.eutro.jwasm.sexp.Parser;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ListParser {
    public final List<?> list;
    public final ListIterator<?> iter;

    public ListParser(List<?> list) {
        this(list, list.listIterator());
    }

    public ListParser(List<?> list, int i) {
        this(list, list.listIterator(i));
    }

    ListParser(List<?> list, ListIterator<?> iter) {
        this.list = list;
        this.iter = iter;
    }

    public static boolean isId(Object it) {
        return it instanceof String && ((String) it).startsWith("$");
    }

    public static <T> T expectClass(Class<T> clazz, Object obj) {
        if (!clazz.isInstance(obj)) {
            throw new Parser.ParseException("Expected " + clazz.getSimpleName(), obj,
                    new RuntimeException("unexpected token"));
        }
        return clazz.cast(obj);
    }

    public static List<?> expectList(Object obj) {
        if (!(obj instanceof List)) {
            throw new Parser.ParseException("expected list", obj);
        }
        return (List<?>) obj;
    }

    public static void expectEq(Object expected, Object val) {
        if (!Objects.equals(expected, val)) {
            throw new Parser.ParseException("expected " + expected, val);
        }
    }

    public static <T> T mark(Object obj, Supplier<T> f) {
        try {
            return f.get();
        } catch (Parser.ParseException e) {
            if (e.in != obj) {
                throw new Parser.ParseException(e, obj);
            } else {
                throw e;
            }
        } catch (RuntimeException e) {
            throw new Parser.ParseException("An error occurred", obj, e);
        }
    }

    public static String parseUtf8(Object obj) {
        byte[] bytes = expectClass(byte[].class, obj);
        CharBuffer buf;
        try {
            buf = StandardCharsets.UTF_8
                    .newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes));
        } catch (CharacterCodingException e) {
            throw new Parser.ParseException("Error decoding string", e,
                    new RuntimeException("malformed UTF-8 encoding"));
        }
        return buf.toString();
    }

    public Object expect() {
        if (!iter.hasNext()) throw new Parser.ParseException("Expected more terms", list,
                new RuntimeException("unexpected token"));
        return iter.next();
    }

    public Optional<Object> maybeParse(Predicate<Object> pred) {
        if (!iter.hasNext()) return Optional.empty();
        Object next = iter.next();
        if (pred.test(next)) return Optional.of(next);
        iter.previous();
        return Optional.empty();
    }

    public Optional<String> maybeParseId() {
        return maybeParse(ListParser::isId)
                .map(String.class::cast);
    }

    public void expectEnd() {
        if (iter.hasNext()) {
            throw new Parser.ParseException("too many terms", list);
        }
    }

    public Optional<Object> peek() {
        if (!iter.hasNext()) return Optional.empty();
        Object val = iter.next();
        iter.previous();
        return Optional.of(val);
    }
}
