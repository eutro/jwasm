package io.github.eutro.jwasm.sexp.wast;

import io.github.eutro.jwasm.ByteInputStream;
import io.github.eutro.jwasm.sexp.Parser;
import io.github.eutro.jwasm.sexp.Reader;
import io.github.eutro.jwasm.sexp.internal.ListParser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import static io.github.eutro.jwasm.sexp.internal.ListParser.*;

public class WastReader {
    public static class ExternRef {
        public final BigInteger value;

        public ExternRef(BigInteger value) {
            this.value = value;
        }
    }

    public static class FuncRef {
    }


    private final List<Object> sexps;

    private WastReader(List<Object> sexps) {
        this.sexps = sexps;
    }

    public void accept(WastVisitor wv) {
        for (Object sexp : sexps) {
            ListParser lp = new ListParser(expectList(sexp));
            String macro = expectClass(String.class, lp.expect());
            switch (macro) {
                case "module": {
                    acceptModule(new ListParser(lp.list), wv.visitModule());
                    break;
                }
                case "register":
                    String string = parseUtf8(lp.expect());
                    String name = lp.maybeParseId().orElse(null);
                    lp.expectEnd();
                    wv.visitRegister(string, name);
                    break;
                case "assert_return": {
                    ListParser aP = new ListParser(expectList(lp.expect()));
                    Object[] results = parseXs(lp, WastReader::parseResult).toArray();
                    acceptAction(aP, wv.visitAssertReturn(results));
                    break;
                }
                case "assert_trap":
                case "assert_exhaustion":
                case "assert_malformed":
                case "assert_invalid":
                case "assert_unlinkable":
                    ListParser actionOrModuleP = new ListParser(expectList(lp.expect()));
                    String failure = parseUtf8(lp.expect());
                    if ("assert_trap".equals(macro)) {
                        if ("module".equals(actionOrModuleP.peek().orElse(null))) {
                            acceptModule(actionOrModuleP, wv.visitAssertModuleTrap(failure));
                        } else {
                            acceptAction(actionOrModuleP, wv.visitAssertTrap(failure));
                        }
                    }
                    switch (macro) {
                        case "assert_exhaustion":
                            acceptAction(actionOrModuleP, wv.visitAssertExhaustion(failure));
                            break;
                        case "assert_malformed":
                            acceptModule(actionOrModuleP, wv.visitAssertMalformed(failure));
                            break;
                        case "assert_invalid":
                            acceptModule(actionOrModuleP, wv.visitAssertInvalid(failure));
                            break;
                        case "assert_unlinkable":
                            acceptModule(actionOrModuleP, wv.visitAssertUnlinkable(failure));
                            break;
                    }
                    break;
                case "invoke":
                case "get":
                    acceptAction(new ListParser(lp.list), wv.visitTopAction());
                    break;
                default:
                    wv.visitOther(macro, lp.list);
            }
        }
        wv.visitEnd();
    }

    private static List<Object> parseXs(ListParser lp, BiPredicate<Object, Consumer<Object>> f) {
        List<Object> results = new ArrayList<>();
        Consumer<Object> addResult = results::add;
        while (lp.iter.hasNext()) {
            if (!f.test(lp.iter.next(), addResult)) {
                throw new Parser.ParseException("unrecognised value", lp.iter.previous());
            }
        }
        return results;
    }

    private static boolean parseConst(Object sexp, Consumer<Object> k) {
        ListParser rLp = new ListParser(expectList(sexp));
        String macro = expectClass(String.class, rLp.expect());
        Object val;
        switch (macro) {
            // @formatter:off
            case "i32.const": val = expectClass(BigInteger.class, rLp.expect()).intValue(); break;
            case "i64.const": val = expectClass(BigInteger.class, rLp.expect()).longValue(); break;
            case "f32.const": val = expectClass(Number.class, rLp.expect()).floatValue(); break;
            case "f64.const": val = expectClass(Number.class, rLp.expect()).doubleValue(); break;
            case "v128.const": val = ListParser.parseV128Const(rLp, true); break;
            case "ref.null": val = null; rLp.expect(); break;
            case "ref.extern":
                if (!rLp.iter.hasNext()) return false;
                val = new ExternRef(expectClass(BigInteger.class, rLp.expect()));
                break;
            // @formatter:on
            default:
                return false;
        }
        rLp.expectEnd();
        k.accept(val);
        return true;
    }

    private static boolean parseResult(Object sexp, Consumer<Object> k) {
        if (parseConst(sexp, k)) return true;
        ListParser rLp = new ListParser(expectList(sexp));
        String macro = expectClass(String.class, rLp.expect());
        Object val;
        switch (macro) {
            // @formatter:off
            case "ref.extern": val = new ExternRef(null); break;
            case "ref.func": val = new FuncRef(); break;
            // @formatter:on
            default:
                return false;
        }
        rLp.expectEnd();
        k.accept(val);
        return true;
    }

    private static void acceptModule(ListParser lp, WastModuleVisitor mv) {
        if (mv != null) {
            expectEq("module", lp.expect());
            lp.maybeParseId();
            Optional<Object> maybeType = lp.maybeParse(it -> "binary".equals(it) || "quote".equals(it));
            if (maybeType.isPresent()) {
                if ("binary".equals(maybeType.get())) {
                    mv.visitBinaryModule(lp.list);
                } else {
                    mv.visitQuoteModule(lp.list);
                }
            } else {
                mv.visitWatModule(lp.list);
            }
            mv.visitEnd();
        }
    }

    private static void acceptAction(ListParser lp, ActionVisitor av) {
        if (av != null) {
            String macro = expectClass(String.class, lp.expect());
            switch (macro) {
                case "invoke":
                    av.visitInvoke(
                            lp.maybeParseId().orElse(null),
                            parseUtf8(lp.expect()),
                            parseXs(lp, WastReader::parseConst)
                    );
                    break;
                case "get":
                    av.visitGet(
                            lp.maybeParseId().orElse(null),
                            parseUtf8(lp.expect())
                    );
                    break;
                default:
                    av.visitOther(macro, lp.list);
            }
            av.visitEnd();
        }
    }

    public static WastReader fromSexps(List<Object> sexps) {
        return new WastReader(sexps);
    }

    public static WastReader fromSource(CharSequence source) {
        return fromSexps(Reader.readAll(source));
    }

    public static <E extends Exception> WastReader fromStream(ByteInputStream<E> stream) throws E {
        return fromSexps(Reader.readAll(stream));
    }
}
