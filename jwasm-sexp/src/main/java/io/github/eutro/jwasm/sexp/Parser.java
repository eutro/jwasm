package io.github.eutro.jwasm.sexp;

import io.github.eutro.jwasm.BlockType;
import io.github.eutro.jwasm.Limits;
import io.github.eutro.jwasm.ModuleReader;
import io.github.eutro.jwasm.sexp.Reader.MemArgPart;
import io.github.eutro.jwasm.tree.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.*;

import static io.github.eutro.jwasm.Opcodes.*;
import static io.github.eutro.jwasm.sexp.Parser.Entry.e;
import static io.github.eutro.jwasm.sexp.Parser.IdCtx.Field.*;
import static io.github.eutro.jwasm.sexp.Parser.IdVal.pure;

public class Parser {
    /**
     * Parse a {@link ModuleNode} from a parsed s-expression as obtained from {@link Reader#readAll(CharSequence)}.
     * <p>
     * Note that this does <b>not</b> <i>validate</i> the module, but will fail if the module is syntactically incorrect.
     *
     * @param obj The read s-expression representation of the module.
     * @return The parsed {@link ModuleNode}.
     * @throws ParseException If obj is not a syntactically valid module.
     */
    public static ModuleNode parseModule(Object obj) throws ParseException {
        return mark(obj, () -> {
            List<?> list = expectList(obj);
            ListParser lp = new ListParser(list);
            expectEq("module", lp.expect());
            lp.maybeParseId();

            List<ModuleField> fields = new ArrayList<>();

            while (lp.iter.hasNext()) {
                parseModuleField(fields, lp.iter.next());
            }

            fields.sort(Comparator.comparing(ModuleField::sortIndex));

            IdCtx idcx = new IdCtx();
            for (ModuleField field : fields) {
                field.idc(idcx);
            }

            ModuleNode module = new ModuleNode();
            for (ModuleField field : fields) {
                field.mod(idcx, module);
            }

            if (module.datas != null && module.datas.datas != null) {
                module.dataCount = module.datas.datas.size();
            }

            return module;
        });
    }

    /**
     * Parse a {@link ModuleNode} from a module written as (module id? binary ...), which may appear in WAST scripts.
     * <p>
     * Much like {@link #parseModule(Object)}, this does <i>not</i> validate the modules.
     *
     * @param obj The read s-expression representation of the module.
     * @return The parsed {@link ModuleNode}.
     * @throws ParseException If obj is not a syntactically valid module.
     */
    public static ModuleNode parseBinaryModule(Object obj) throws ParseException {
        return mark(obj, () -> {
            ListParser lp = new ListParser(expectList(obj));
            expectEq("module", lp.expect());
            lp.maybeParseId();

            expectEq("binary", lp.expect());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while (lp.iter.hasNext()) {
                byte[] bytes = expectClass(byte[].class, lp.iter.next());
                baos.write(bytes, 0, bytes.length);
            }

            ModuleNode mn = new ModuleNode();
            try {
                ModuleReader.fromBytes(baos.toByteArray()).accept(mn);
            } catch (RuntimeException e) {
                throw new ParseException("failed to read binary module", obj, e);
            }
            return mn;
        });
    }

    private static Integer truncToU32(BigInteger it) {
        try {
            long lv = it.longValueExact();
            if (lv > Integer.toUnsignedLong(-1)) {
                throw new ArithmeticException();
            }
            return (int) lv;
        } catch (ArithmeticException e) {
            throw new ParseException("value out of range for u32", it);
        }
    }

    /**
     * An exception thrown if the module being parsed was not syntactically valid.
     */
    public static class ParseException extends RuntimeException {
        /**
         * The s-expr object being parsed when parsing failed.
         */
        public final Object in;

        public ParseException(String message, Object in, Throwable cause) {
            super(message + " in: " + Writer.writeToString(in), cause);
            this.in = in;
        }

        public ParseException(ParseException cause, Object in) {
            this("parse error", in, cause);
        }

        public ParseException(String message, Object in) {
            this(message, in, null);
        }
    }

    enum SortIndex implements Comparable<SortIndex> {
        TYPES,
        INDEX_TYPES,
        DEFAULT,
    }

    static class ListParser {
        final List<?> list;
        final ListIterator<?> iter;

        ListParser(List<?> list) {
            this(list, list.listIterator());
        }

        ListParser(List<?> list, int i) {
            this(list, list.listIterator(i));
        }

        ListParser(List<?> list, ListIterator<?> iter) {
            this.list = list;
            this.iter = iter;
        }

        public Object expect() {
            if (!iter.hasNext()) throw new ParseException("expected more terms", list);
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
            return maybeParse(Parser::isId)
                    .map(String.class::cast);
        }

        public void expectEnd() {
            if (iter.hasNext()) {
                throw new ParseException("too many terms", list);
            }
        }

        public Optional<Object> peek() {
            if (!iter.hasNext()) return Optional.empty();
            Object val = iter.next();
            iter.previous();
            return Optional.of(val);
        }
    }

    private static boolean isId(Object it) {
        return it instanceof String && ((String) it).startsWith("$");
    }

    private static boolean couldBeId(Object it) {
        return it instanceof BigInteger || isId(it);
    }

    private static <T> T mark(Object obj, Supplier<T> f) {
        try {
            return f.get();
        } catch (ParseException e) {
            if (e.in != obj) {
                throw new ParseException(e, obj);
            } else {
                throw e;
            }
        } catch (RuntimeException e) {
            throw new ParseException("an error occurred", obj, e);
        }
    }

    private static List<?> expectList(Object obj) {
        if (!(obj instanceof List)) {
            throw new ParseException("expected list", obj);
        }
        return (List<?>) obj;
    }

    private static <T> T expectClass(Class<T> clazz, Object obj) {
        if (!clazz.isInstance(obj)) {
            throw new ParseException("expected " + clazz.getSimpleName(), obj);
        }
        return clazz.cast(obj);
    }

    private static void expectEq(Object expected, Object val) {
        if (!Objects.equals(expected, val)) {
            throw new ParseException("expected " + expected, val);
        }
    }

    static class IdCtx {
        public enum Field {
            DATA,
            ELEM,
            FUNC,
            GLOBAL,
            MEM,
            TABLE,
            TYPE,

            LABEL,
            LOCAL;

            @Override
            public String toString() {
                return name().toLowerCase(Locale.ROOT);
            }
        }

        final EnumMap<Field, IndexedList<@Nullable String>> fields;
        final IndexedList<TypeNode> typedefs;

        IdCtx() {
            this(new EnumMap<>(Field.class), new IndexedList<>());
            for (Field field : Field.values()) {
                fields.put(field, new IndexedList<>());
            }
        }

        IdCtx(EnumMap<Field, IndexedList<@Nullable String>> fields, IndexedList<TypeNode> typedefs) {
            this.fields = fields;
            this.typedefs = typedefs;
        }

        public IndexedList<@Nullable String> f(Field f) {
            return fields.get(f);
        }

        public void addIdx(Field field, @Nullable String name) {
            f(field).addOrThrow(name, () -> new ParseException("duplicate " + field + " id", name));
        }

        public IdCtx deriveLocals() {
            EnumMap<Field, IndexedList<@Nullable String>> newFields = new EnumMap<>(fields);
            newFields.put(Field.LOCAL, new IndexedList<>());
            newFields.put(Field.LABEL, new IndexedList<>());
            return new IdCtx(newFields, typedefs);
        }
    }

    private static class IndexedList<T> extends AbstractList<T> {
        private final List<T> list = new ArrayList<>();
        private final Map<T, Integer> index = new HashMap<>();

        public int getIndex(T value) {
            return index.computeIfAbsent(value, $ -> {
                throw new NoSuchElementException();
            });
        }

        public boolean hasIndex(T value) {
            return index.containsKey(value);
        }

        @Override
        public T get(int index) {
            return list.get(index);
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public boolean add(T o) {
            return addOrDoIfPresent(o, () -> {
            });
        }

        public void popLast() {
            T removed = list.remove(list.size() - 1);
            if (removed != null) {
                index.remove(removed);
            }
        }

        private boolean addOrDoIfPresent(T o, Runnable r) {
            if (o != null && index.putIfAbsent(o, list.size()) != null) {
                r.run();
            }
            return list.add(o);
        }

        public void addOrThrow(T o, Supplier<RuntimeException> supp) {
            addOrDoIfPresent(o, () -> {
                throw supp.get();
            });
        }
    }

    private interface ModuleField {
        void idc(IdCtx idcx);

        void mod(IdCtx idcx, ModuleNode module);

        default SortIndex sortIndex() {
            return SortIndex.DEFAULT;
        }
    }

    private static void parseModuleField(List<ModuleField> fields, Object field) {
        List<?> list = expectList(field);
        String sym = expectClass(String.class, new ListParser(list).expect());
        mark(field, () -> {
            switch (sym) {
                // @formatter:off
                case "type": parseTypeField(fields, list); return null;
                case "import": parseImportField(fields, list); return null;
                case "func": parseFuncField(fields, list); return null;
                case "table": parseTableField(fields, list); return null;
                case "memory": parseMemField(fields, list); return null;
                case "global": parseGlobalField(fields, list); return null;
                case "export": parseExportField(fields, list); return null;
                case "start": parseStartField(fields, list); return null;
                case "elem": parseElemField(fields, list); return null;
                case "data": parseDataField(fields, list); return null;
                // @formatter:on
            }
            throw new ParseException("unrecognised field " + sym, field);
        });
    }

    private static byte parseRefType(Object obj) {
        String str = expectClass(String.class, obj);
        switch (str) {
            // @formatter:off
            case "funcref": return FUNCREF;
            case "externref": return EXTERNREF;
            // @formatter:on
        }
        throw new ParseException("expected reftype", obj);
    }

    private static byte parseValType(Object obj) {
        String str = expectClass(String.class, obj);
        switch (str) {
            // @formatter:off
            case "i32": return I32;
            case "i64": return I64;
            case "f32": return F32;
            case "f64": return F64;
            case "funcref": return FUNCREF;
            case "externref": return EXTERNREF;
            // @formatter:on
        }
        throw new ParseException("expected valtype", obj);
    }

    interface IdVal<T> {
        T resolve(ModuleNode mod, IdCtx idcx);

        static <T> IdVal<T> pure(T x) {
            return (mod, idcx) -> x;
        }

        default <O, R> IdVal<R> ap(IdVal<O> o, BiFunction<T, O, R> f) {
            return (mod, idcx) -> f.apply(resolve(mod, idcx), o.resolve(mod, idcx));
        }

        default <R> IdVal<R> fmap(Function<T, R> f) {
            return (mod, idcx) -> f.apply(resolve(mod, idcx));
        }

        default <R> IdVal<R> bind(Function<T, IdVal<R>> f) {
            return (mod, idcx) -> f.apply(resolve(mod, idcx)).resolve(mod, idcx);
        }
    }

    private static String parseId(Object obj) {
        String str = expectClass(String.class, obj);
        if (!str.startsWith("$")) {
            throw new ParseException("expected identifier", obj);
        }
        return str;
    }

    private static TypeNode parseParamsAndResults(
            List<String> paramIds,
            ListParser lp
    ) {
        List<Byte> params = new ArrayList<>(), results = new ArrayList<>();
        boolean isParams = true;
        while (lp.iter.hasNext()) {
            Optional<Object> maybeParamOrResult = lp.maybeParse(it -> isMacroList(it, "param", "result").isPresent());
            if (!maybeParamOrResult.isPresent()) break;
            List<?> list = (List<?>) maybeParamOrResult.get();
            if ("param".equals(list.get(0))) {
                if (!isParams) throw new ParseException("param following results", list);
                ListParser pLp = new ListParser(list, 1);
                boolean hasId = false;
                if (list.size() == 3) {
                    Optional<String> maybeId = pLp.maybeParseId();
                    if (maybeId.isPresent()) {
                        hasId = true;
                        paramIds.add(maybeId.get());
                    }
                }
                while (pLp.iter.hasNext()) {
                    byte ty = parseValType(pLp.iter.next());
                    if (!hasId) {
                        paramIds.add(null);
                    }
                    params.add(ty);
                }
            } else {
                isParams = false;

                ListIterator<?> li = list.listIterator(1);
                while (li.hasNext()) {
                    results.add(parseValType(li.next()));
                }

            }
        }
        return new TypeNode(params, results);
    }

    private static TypeNode parseFuncType(Object funcType) {
        List<?> list = expectList(funcType);
        ListParser lp = new ListParser(list);
        expectEq("func", lp.expect());
        TypeNode ft = parseParamsAndResults(new ArrayList<>(), lp);
        lp.expectEnd();
        return ft;
    }

    private static void parseTypeField(List<ModuleField> fields, List<?> list) {
        class TypeField implements ModuleField {
            private final String id;
            private final TypeNode ft;

            public TypeField(String id, TypeNode ft) {
                this.id = id;
                this.ft = ft;
            }

            @Override
            public void idc(IdCtx idcx) {
                idcx.addIdx(TYPE, id);
                idcx.typedefs.add(ft);
            }

            @Override
            public void mod(IdCtx idcx, ModuleNode module) {
                if (module.types == null) module.types = new TypesNode();
                if (module.types.types == null) module.types.types = new ArrayList<>();
                module.types.types.add(ft);
            }

            @Override
            public SortIndex sortIndex() {
                return SortIndex.TYPES;
            }
        }
        switch (list.size()) {
            case 2: {
                fields.add(new TypeField(null, parseFuncType(list.get(1))));
                return;
            }
            case 3: {
                fields.add(new TypeField(parseId(list.get(1)), parseFuncType(list.get(2))));
                return;
            }
        }
        throw new ParseException("expected 2 or 3 terms, got " + list.size(), list);
    }

    private static void parseImportField(List<ModuleField> fields, List<?> list) {
        ListParser lp = new ListParser(list, 1);
        String modNm = parseName(lp.expect());
        String nm = parseName(lp.expect());
        List<?> desc = expectList(lp.expect());
        lp.expectEnd();

        ListParser dLp = new ListParser(desc);
        String type = expectClass(String.class, dLp.expect());
        Optional<String> maybeId = dLp.maybeParseId();
        switch (type) {
            case "func": {
                TypeUse tu = parseTypeUse(dLp);
                dLp.expectEnd();
                fields.add(new ImportField(
                        FUNC,
                        maybeId.orElse(null),
                        module -> module.funcs,
                        desc,
                        tu.fmap(ty -> new FuncImportNode(modNm, nm, ty))
                ));
                break;
            }
            case "table": {
                TableNode tn = parseTableType(dLp);
                dLp.expectEnd();
                fields.add(new ImportField(
                        TABLE,
                        maybeId.orElse(null),
                        module -> module.tables,
                        desc,
                        pure(new TableImportNode(modNm, nm, tn.limits, tn.type))
                ));
                break;
            }
            case "memory": {
                MemoryNode memTy = parseMemType(dLp);
                dLp.expectEnd();
                fields.add(new ImportField(
                        MEM,
                        maybeId.orElse(null),
                        module -> module.mems,
                        desc,
                        pure(new MemImportNode(modNm, nm, memTy.limits))
                ));
                break;
            }
            case "global": {
                GlobalTypeNode ty = parseGlobalType(dLp.expect());
                dLp.expectEnd();
                fields.add(new ImportField(
                        GLOBAL,
                        maybeId.orElse(null),
                        module -> module.globals,
                        desc,
                        pure(new GlobalImportNode(modNm, nm, ty))
                ));
                break;
            }
            default:
                throw new ParseException("unrecognised import descriptor", type);
        }
    }

    private static String parseName(Object obj) {
        byte[] bytes = expectClass(byte[].class, obj);
        CharBuffer buf;
        try {
            buf = StandardCharsets.UTF_8
                    .newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes));
        } catch (CharacterCodingException e) {
            throw new ParseException("error decoding string", e);
        }
        return buf.toString();
    }

    private static class TypeUse implements IdVal<Integer> {
        final IdVal<Integer> underlying;
        @Nullable
        final IndexedList<String> locals;

        private TypeUse(IdVal<Integer> underlying, @Nullable IndexedList<String> params) {
            this.underlying = underlying;
            this.locals = params;
        }

        @Override
        public Integer resolve(ModuleNode module, IdCtx idcx) {
            if (locals != null) {
                for (String uniqueLocal : locals.index.keySet()) {
                    if (idcx.f(LOCAL).hasIndex(uniqueLocal)) {
                        throw new ParseException("local variable is defined twice", uniqueLocal);
                    }
                }
            }

            return underlying.resolve(module, idcx);
        }
    }

    private static Optional<String> isMacroList(Object obj, String... macros) {
        if (!(obj instanceof List<?>)) return Optional.empty();
        List<?> list = (List<?>) obj;
        if (list.size() < 1) return Optional.empty();
        Object f = list.get(0);
        if (!(f instanceof String)) return Optional.empty();
        for (String macro : macros) {
            if (macro.equals(f)) return Optional.of(macro);
        }
        return Optional.empty();
    }

    private static TypeUse parseTypeUse(ListParser lp) {
        Optional<Object> maybeType = lp.maybeParse(it -> isMacroList(it, "type").isPresent());
        if (maybeType.isPresent()) {
            List<?> type = (List<?>) maybeType.get();
            if (type.size() != 2) throw new ParseException("expected two terms (type x)", type);
            expectEq("type", type.get(0));
            IdVal<Integer> id = parseIdx(TYPE, type.get(1));

            if (lp.iter.hasNext()) hasInline:{
                IndexedList<String> locals = new IndexedList<>();
                TypeNode inline = parseParamsAndResults(locals, lp);
                if (inline.params.length == 0 || inline.returns.length == 0) {
                    // there were more terms, but there wasn't actually an inline typeuse, so skip checking it
                    break hasInline;
                }
                IdVal<Integer> oldId = id;
                id = (m, idcx) -> {
                    int resolved = oldId.resolve(m, idcx);
                    if (m.types == null || m.types.types == null || m.types.types.size() < resolved) {
                        throw new ParseException("type index does not exist", lp.list);
                    }
                    TypeNode referenced = m.types.types.get(resolved);
                    if (!inline.equals(referenced)) {
                        throw new ParseException("type use did not match index type", lp.list);
                    }
                    return resolved;
                };
                return new TypeUse(id, locals);
            }
            return new TypeUse(id, null);
        } else {
            IndexedList<String> locals = new IndexedList<>();
            TypeNode type = parseParamsAndResults(locals, lp);
            IdVal<Integer> id = (module, idcx) -> {
                if (idcx.typedefs.hasIndex(type)) {
                    return idcx.typedefs.getIndex(type);
                } else {
                    if (module.types == null) module.types = new TypesNode();
                    if (module.types.types == null) module.types.types = new ArrayList<>();
                    int idx = idcx.f(TYPE).size();
                    idcx.addIdx(TYPE, null);
                    idcx.typedefs.add(type);
                    module.types.types.add(type);
                    return idx;
                }
            };
            return new TypeUse(id, locals);
        }
    }

    private static IdVal<Integer> parseIdx(IdCtx.Field field, Object o) {
        if (o instanceof BigInteger) {
            int i = ((BigInteger) o).intValueExact();
            return (m, idcx) -> i;
        } else if (o instanceof String) {
            String id = parseId(o);
            return (m, idcx) -> {
                IndexedList<@Nullable String> il = idcx.fields.get(field);
                if (!il.hasIndex(id)) {
                    throw new ParseException(field + " id does not exist", o);
                }
                return il.getIndex(id);
            };
        } else {
            throw new ParseException("expected idx (list or identifier)", o);
        }
    }

    private static void wrapExportImportAbbrevs(
            IdCtx.Field field,
            byte exportType,

            Function<ModuleNode, Object> getLocalObjects,

            @Nullable String maybeId,
            List<ModuleField> fields,
            ListParser lp,

            Supplier<IdVal<BiFunction<String, String, AbstractImportNode>>> parseImport,
            Runnable parseLocal
    ) {
        List<String> exportNames = new ArrayList<>();

        Optional<Object> maybeExportOrImport;
        while ((maybeExportOrImport = lp.maybeParse(it -> isMacroList(it, "import", "export").isPresent()))
                .isPresent()) {
            ListParser eiLp = new ListParser((List<?>) maybeExportOrImport.get());
            switch (expectClass(String.class, eiLp.expect())) {
                case "import": {
                    String moduleName = parseName(eiLp.expect());
                    String name = parseName(eiLp.expect());

                    IdVal<AbstractImportNode> theImport = parseImport.get()
                            .fmap(f -> f.apply(moduleName, name));

                    eiLp.expectEnd();
                    fields.add(new ImportField(field, maybeId, getLocalObjects, lp.list, theImport));
                    return;
                }
                case "export":
                    exportNames.add(parseName(eiLp.expect()));
                    eiLp.expectEnd();
                    break;
                default:
                    break;
            }
        }

        parseLocal.run();

        for (String exportName : exportNames) {
            fields.add(new ModuleField() {
                @Override
                public void idc(IdCtx idcx) {
                }

                @Override
                public void mod(IdCtx idcx, ModuleNode module) {
                    int idx = idcx.f(field).size() - 1;
                    if (module.exports == null) module.exports = new ExportsNode();
                    module.exports.visitExport(exportName, exportType, idx);
                }
            });
        }
    }

    private static void parseFuncField(List<ModuleField> fields, List<?> list) {
        ListParser lp = new ListParser(list, 1);
        Optional<String> maybeId = lp.maybeParseId();

        wrapExportImportAbbrevs(
                FUNC,
                EXPORTS_FUNC,
                module -> module.funcs,
                maybeId.orElse(null),
                fields,
                lp,
                () -> parseTypeUse(lp).fmap(ty -> (moduleName, name) -> new FuncImportNode(moduleName, name, ty)),
                () -> {
                    TypeUse tu = parseTypeUse(lp);

                    List<Byte> localTys = new ArrayList<>();
                    List<@Nullable String> localNames = new ArrayList<>();

                    Optional<Object> maybeLocal;
                    while ((maybeLocal = lp.maybeParse(it -> isMacroList(it, "local").isPresent()))
                            .isPresent()) {
                        List<?> local = (List<?>) maybeLocal.get();

                        ListParser llp = new ListParser(local, 1);
                        Optional<String> maybeLocalId = llp.maybeParseId();
                        if (maybeLocalId.isPresent()) {
                            if (local.size() != 3) {
                                throw new ParseException("wrong number of terms in named local", local);
                            }
                            localNames.add(maybeLocalId.get());
                            localTys.add(parseValType(local.get(2)));
                        } else {
                            while (llp.iter.hasNext()) {
                                localNames.add(null);
                                localTys.add(parseValType(llp.iter.next()));
                            }
                        }
                    }

                    IdVal<List<AbstractInsnNode>> instrs = parseInstrs(lp);

                    fields.add(new ModuleField() {
                        @Override
                        public void idc(IdCtx idcx) {
                            idcx.addIdx(FUNC, maybeId.orElse(null));
                        }

                        @Override
                        public void mod(IdCtx idcx, ModuleNode module) {
                            if (module.funcs == null) module.funcs = new FunctionsNode();
                            if (module.funcs.funcs == null) module.funcs.funcs = new ArrayList<>();

                            if (module.codes == null) module.codes = new CodesNode();
                            if (module.codes.codes == null) module.codes.codes = new ArrayList<>();

                            int type = tu.resolve(module, idcx);

                            if (module.types == null || module.types.types == null) throw new IllegalStateException();

                            TypeNode funcType = module.types.types.get(type);

                            if (!(tu.locals == null || tu.locals.size() == funcType.params.length)) {
                                throw new ParseException(
                                        "local count does not match function type "
                                                + "(locals: " + tu.locals.size()
                                                + ", params: " + funcType.params.length + ")",
                                        list
                                );
                            }

                            byte[] locals = new byte[funcType.params.length + localTys.size()];
                            int i = 0;
                            for (byte param : funcType.params) {
                                locals[i++] = param;
                            }
                            for (byte localTy : localTys) {
                                locals[i++] = localTy;
                            }

                            IdCtx localIdcx = idcx.deriveLocals();
                            if (tu.locals == null) {
                                for (int j = 0; j < funcType.params.length; j++) {
                                    localIdcx.f(LOCAL).add(null);
                                }
                            } else {
                                localIdcx.f(LOCAL).addAll(tu.locals);
                            }

                            localIdcx.f(LOCAL).addAll(localNames);

                            module.funcs.funcs.add(new FuncNode(type));

                            List<AbstractInsnNode> realInstrs = instrs.resolve(module, localIdcx);
                            ExprNode expr = new ExprNode();
                            expr.instructions = new LinkedList<>(realInstrs);
                            expr.instructions.add(new EndInsnNode());
                            module.codes.codes.add(new CodeNode(locals, expr));
                        }
                    });
                });
    }

    private interface InstrParser {
        IdVal<AbstractInsnNode> parse(ListParser lp);
    }

    private static final Map<String, InstrParser> OPCODES = new HashMap<>();

    static {
        OPCODES.put("unreachable", lp -> pure(new InsnNode(UNREACHABLE)));
        OPCODES.put("nop", lp -> pure(new InsnNode(NOP)));
        OPCODES.put("br", lp -> parseLabelIdx(lp.expect()).fmap(lb -> new BreakInsnNode(BR, lb)));
        OPCODES.put("br_if", lp -> parseLabelIdx(lp.expect()).fmap(lb -> new BreakInsnNode(BR, lb)));
        OPCODES.put("br_table", lp -> {
            Optional<Object> maybeLabel;
            List<IdVal<Integer>> labels = new ArrayList<>();
            while ((maybeLabel = lp.maybeParse(Parser::couldBeId)).isPresent()) {
                labels.add(parseLabelIdx(maybeLabel.get()));
            }
            if (labels.size() < 1) throw new ParseException("expected at least one lable for br_table", lp.list);
            return (mod, idcx) -> {
                int[] iLabels = new int[labels.size() - 1];
                int i = 0;
                for (IdVal<Integer> label : labels.subList(0, iLabels.length)) {
                    iLabels[i++] = label.resolve(mod, idcx);
                }
                int dfltLabel = labels.get(labels.size() - 1).resolve(mod, idcx);
                return new TableBreakInsnNode(iLabels, dfltLabel);
            };
        });
        OPCODES.put("return", lp -> pure(new InsnNode(RETURN)));
        OPCODES.put("call", lp -> parseIdx(FUNC, lp.expect()).fmap(CallInsnNode::new));
        OPCODES.put("call_indirect", lp -> maybeParseIdx(TABLE, lp)
                .ap(parseTypeUse(lp), CallIndirectInsnNode::new));

        OPCODES.put("ref.null", lp -> pure(new NullInsnNode(parseHeapType(lp))));
        OPCODES.put("ref.is_null", lp -> pure(new InsnNode(REF_IS_NULL)));
        OPCODES.put("ref.func", lp -> parseIdx(FUNC, lp.expect()).fmap(FuncRefInsnNode::new));

        OPCODES.put("drop", lp -> pure(new InsnNode(DROP)));
        OPCODES.put("select", lp -> pure(new SelectInsnNode(parseResults(lp))));

        OPCODES.put("local.get", lp -> parseIdx(LOCAL, lp.expect()).fmap(idx -> new VariableInsnNode(LOCAL_GET, idx)));
        OPCODES.put("local.set", lp -> parseIdx(LOCAL, lp.expect()).fmap(idx -> new VariableInsnNode(LOCAL_SET, idx)));
        OPCODES.put("local.tee", lp -> parseIdx(LOCAL, lp.expect()).fmap(idx -> new VariableInsnNode(LOCAL_TEE, idx)));
        OPCODES.put("global.get", lp -> parseIdx(GLOBAL, lp.expect()).fmap(idx -> new VariableInsnNode(GLOBAL_GET, idx)));
        OPCODES.put("global.set", lp -> parseIdx(GLOBAL, lp.expect()).fmap(idx -> new VariableInsnNode(GLOBAL_SET, idx)));

        OPCODES.put("table.get", lp -> maybeParseIdx(TABLE, lp).fmap(idx -> new PrefixTableInsnNode(TABLE_GET, idx)));
        OPCODES.put("table.set", lp -> maybeParseIdx(TABLE, lp).fmap(idx -> new PrefixTableInsnNode(TABLE_SET, idx)));
        OPCODES.put("table.size", lp -> maybeParseIdx(TABLE, lp).fmap(idx -> new PrefixTableInsnNode(TABLE_SIZE, idx)));
        OPCODES.put("table.grow", lp -> maybeParseIdx(TABLE, lp).fmap(idx -> new PrefixTableInsnNode(TABLE_GROW, idx)));
        OPCODES.put("table.fill", lp -> maybeParseIdx(TABLE, lp).fmap(idx -> new PrefixTableInsnNode(TABLE_FILL, idx)));
        OPCODES.put("table.copy", lp -> maybeParseIdx(TABLE, lp).ap(maybeParseIdx(TABLE, lp), (x, y) -> new PrefixBinaryTableInsnNode(TABLE_COPY, x, y)));
        OPCODES.put("table.init", lp -> {
            Optional<Object> firstId = lp.maybeParse(Parser::couldBeId);
            Optional<Object> secondId = lp.maybeParse(Parser::couldBeId);

            IdVal<Integer> table, elem;
            if (secondId.isPresent()) {
                table = parseIdx(TABLE, firstId.orElseThrow(AssertionError::new));
                elem = parseIdx(ELEM, secondId.get());
            } else {
                table = pure(0);
                elem = parseIdx(ELEM, firstId.orElseThrow(() -> new ParseException("expected id", lp.expect())));
            }
            return table.ap(elem, (x, y) -> new PrefixBinaryTableInsnNode(TABLE_INIT, x, y));
        });
        OPCODES.put("elem.drop", lp -> parseIdx(ELEM, lp.expect()).fmap((x) -> new PrefixTableInsnNode(ELEM_DROP, x)));

        OPCODES.put("i32.load", lp -> pure(parseMemArg(I32_LOAD, 4, lp)));
        OPCODES.put("i64.load", lp -> pure(parseMemArg(I64_LOAD, 8, lp)));
        OPCODES.put("f32.load", lp -> pure(parseMemArg(F32_LOAD, 4, lp)));
        OPCODES.put("f64.load", lp -> pure(parseMemArg(F64_LOAD, 8, lp)));
        OPCODES.put("i32.load8_s", lp -> pure(parseMemArg(I32_LOAD8_S, 1, lp)));
        OPCODES.put("i32.load8_u", lp -> pure(parseMemArg(I32_LOAD8_U, 1, lp)));
        OPCODES.put("i32.load16_s", lp -> pure(parseMemArg(I32_LOAD16_S, 2, lp)));
        OPCODES.put("i32.load16_u", lp -> pure(parseMemArg(I32_LOAD16_U, 2, lp)));
        OPCODES.put("i64.load8_s", lp -> pure(parseMemArg(I64_LOAD8_S, 1, lp)));
        OPCODES.put("i64.load8_u", lp -> pure(parseMemArg(I64_LOAD8_U, 1, lp)));
        OPCODES.put("i64.load16_s", lp -> pure(parseMemArg(I64_LOAD16_S, 2, lp)));
        OPCODES.put("i64.load16_u", lp -> pure(parseMemArg(I64_LOAD16_U, 2, lp)));
        OPCODES.put("i64.load32_s", lp -> pure(parseMemArg(I64_LOAD32_S, 4, lp)));
        OPCODES.put("i64.load32_u", lp -> pure(parseMemArg(I64_LOAD32_U, 4, lp)));
        OPCODES.put("i32.store", lp -> pure(parseMemArg(I32_STORE, 4, lp)));
        OPCODES.put("i64.store", lp -> pure(parseMemArg(I64_STORE, 8, lp)));
        OPCODES.put("f32.store", lp -> pure(parseMemArg(F32_STORE, 4, lp)));
        OPCODES.put("f64.store", lp -> pure(parseMemArg(F64_STORE, 8, lp)));
        OPCODES.put("i32.store8", lp -> pure(parseMemArg(I32_STORE8, 1, lp)));
        OPCODES.put("i32.store16", lp -> pure(parseMemArg(I32_STORE16, 2, lp)));
        OPCODES.put("i64.store8", lp -> pure(parseMemArg(I64_STORE8, 1, lp)));
        OPCODES.put("i64.store16", lp -> pure(parseMemArg(I64_STORE16, 2, lp)));
        OPCODES.put("i64.store32", lp -> pure(parseMemArg(I64_STORE32, 4, lp)));

        OPCODES.put("memory.size", lp -> pure(new InsnNode(MEMORY_SIZE)));
        OPCODES.put("memory.grow", lp -> pure(new InsnNode(MEMORY_GROW)));
        OPCODES.put("memory.fill", lp -> pure(new PrefixInsnNode(MEMORY_FILL)));
        OPCODES.put("memory.copy", lp -> pure(new PrefixInsnNode(MEMORY_COPY)));
        OPCODES.put("memory.init", lp -> parseIdx(DATA, lp.expect()).fmap(x -> new IndexedMemInsnNode(MEMORY_INIT, x)));
        OPCODES.put("data.drop", lp -> parseIdx(DATA, lp.expect()).fmap(x -> new IndexedMemInsnNode(DATA_DROP, x)));

        // TODO check ranges?
        OPCODES.put("i32.const", lp -> pure(new ConstInsnNode(expectClass(BigInteger.class, lp.expect()).intValue())));
        OPCODES.put("i64.const", lp -> pure(new ConstInsnNode(expectClass(BigInteger.class, lp.expect()).longValue())));
        OPCODES.put("f32.const", lp -> pure(new ConstInsnNode(expectClass(Number.class, lp.expect()).floatValue())));
        OPCODES.put("f64.const", lp -> pure(new ConstInsnNode(expectClass(Number.class, lp.expect()).doubleValue())));
    }

    static class Entry {
        final String name;
        final byte opcode;

        private Entry(String name, byte opcode) {
            this.name = name;
            this.opcode = opcode;
        }

        static Entry e(String name, byte opcode) {
            return new Entry(name, opcode);
        }
    }

    static {
        Entry[] entries = {
                e("i32.eqz", I32_EQZ),
                e("i32.eq", I32_EQ),
                e("i32.ne", I32_NE),
                e("i32.lt_s", I32_LT_S),
                e("i32.lt_u", I32_LT_U),
                e("i32.gt_s", I32_GT_S),
                e("i32.gt_u", I32_GT_U),
                e("i32.le_s", I32_LE_S),
                e("i32.le_u", I32_LE_U),
                e("i32.ge_s", I32_GE_S),
                e("i32.ge_u", I32_GE_U),
                e("i64.eqz", I64_EQZ),
                e("i64.eq", I64_EQ),
                e("i64.ne", I64_NE),
                e("i64.lt_s", I64_LT_S),
                e("i64.lt_u", I64_LT_U),
                e("i64.gt_s", I64_GT_S),
                e("i64.gt_u", I64_GT_U),
                e("i64.le_s", I64_LE_S),
                e("i64.le_u", I64_LE_U),
                e("i64.ge_s", I64_GE_S),
                e("i64.ge_u", I64_GE_U),
                e("f32.eq", F32_EQ),
                e("f32.ne", F32_NE),
                e("f32.lt", F32_LT),
                e("f32.gt", F32_GT),
                e("f32.le", F32_LE),
                e("f32.ge", F32_GE),
                e("f64.eq", F64_EQ),
                e("f64.ne", F64_NE),
                e("f64.lt", F64_LT),
                e("f64.gt", F64_GT),
                e("f64.le", F64_LE),
                e("f64.ge", F64_GE),
                e("i32.clz", I32_CLZ),
                e("i32.ctz", I32_CTZ),
                e("i32.popcnt", I32_POPCNT),
                e("i32.add", I32_ADD),
                e("i32.sub", I32_SUB),
                e("i32.mul", I32_MUL),
                e("i32.div_s", I32_DIV_S),
                e("i32.div_u", I32_DIV_U),
                e("i32.rem_s", I32_REM_S),
                e("i32.rem_u", I32_REM_U),
                e("i32.and", I32_AND),
                e("i32.or", I32_OR),
                e("i32.xor", I32_XOR),
                e("i32.shl", I32_SHL),
                e("i32.shr_s", I32_SHR_S),
                e("i32.shr_u", I32_SHR_U),
                e("i32.rotl", I32_ROTL),
                e("i32.rotr", I32_ROTR),
                e("i64.clz", I64_CLZ),
                e("i64.ctz", I64_CTZ),
                e("i64.popcnt", I64_POPCNT),
                e("i64.add", I64_ADD),
                e("i64.sub", I64_SUB),
                e("i64.mul", I64_MUL),
                e("i64.div_s", I64_DIV_S),
                e("i64.div_u", I64_DIV_U),
                e("i64.rem_s", I64_REM_S),
                e("i64.rem_u", I64_REM_U),
                e("i64.and", I64_AND),
                e("i64.or", I64_OR),
                e("i64.xor", I64_XOR),
                e("i64.shl", I64_SHL),
                e("i64.shr_s", I64_SHR_S),
                e("i64.shr_u", I64_SHR_U),
                e("i64.rotl", I64_ROTL),
                e("i64.rotr", I64_ROTR),
                e("f32.abs", F32_ABS),
                e("f32.neg", F32_NEG),
                e("f32.ceil", F32_CEIL),
                e("f32.floor", F32_FLOOR),
                e("f32.trunc", F32_TRUNC),
                e("f32.nearest", F32_NEAREST),
                e("f32.sqrt", F32_SQRT),
                e("f32.add", F32_ADD),
                e("f32.sub", F32_SUB),
                e("f32.mul", F32_MUL),
                e("f32.div", F32_DIV),
                e("f32.min", F32_MIN),
                e("f32.max", F32_MAX),
                e("f32.copysign", F32_COPYSIGN),
                e("f64.abs", F64_ABS),
                e("f64.neg", F64_NEG),
                e("f64.ceil", F64_CEIL),
                e("f64.floor", F64_FLOOR),
                e("f64.trunc", F64_TRUNC),
                e("f64.nearest", F64_NEAREST),
                e("f64.sqrt", F64_SQRT),
                e("f64.add", F64_ADD),
                e("f64.sub", F64_SUB),
                e("f64.mul", F64_MUL),
                e("f64.div", F64_DIV),
                e("f64.min", F64_MIN),
                e("f64.max", F64_MAX),
                e("f64.copysign", F64_COPYSIGN),
                e("i32.wrap_i64", I32_WRAP_I64),
                e("i32.trunc_f32_s", I32_TRUNC_F32_S),
                e("i32.trunc_f32_u", I32_TRUNC_F32_U),
                e("i32.trunc_f64_s", I32_TRUNC_F64_S),
                e("i32.trunc_f64_u", I32_TRUNC_F64_U),
                e("i64.extend_i32_s", I64_EXTEND_I32_S),
                e("i64.extend_i32_u", I64_EXTEND_I32_U),
                e("i64.trunc_f32_s", I64_TRUNC_F32_S),
                e("i64.trunc_f32_u", I64_TRUNC_F32_U),
                e("i64.trunc_f64_s", I64_TRUNC_F64_S),
                e("i64.trunc_f64_u", I64_TRUNC_F64_U),
                e("f32.convert_i32_s", F32_CONVERT_I32_S),
                e("f32.convert_i32_u", F32_CONVERT_I32_U),
                e("f32.convert_i64_s", F32_CONVERT_I64_S),
                e("f32.convert_i64_u", F32_CONVERT_I64_U),
                e("f32.demote_f64", F32_DEMOTE_F64),
                e("f64.convert_i32_s", F64_CONVERT_I32_S),
                e("f64.convert_i32_u", F64_CONVERT_I32_U),
                e("f64.convert_i64_s", F64_CONVERT_I64_S),
                e("f64.convert_i64_u", F64_CONVERT_I64_U),
                e("f64.promote_f32", F64_PROMOTE_F32),
                e("i32.reinterpret_f32", I32_REINTERPRET_F32),
                e("i64.reinterpret_f64", I64_REINTERPRET_F64),
                e("f32.reinterpret_i32", F32_REINTERPRET_I32),
                e("f64.reinterpret_i64", F64_REINTERPRET_I64),
                e("i32.extend8_s", I32_EXTEND8_S),
                e("i32.extend16_s", I32_EXTEND16_S),
                e("i64.extend8_s", I64_EXTEND8_S),
                e("i64.extend16_s", I64_EXTEND16_S),
                e("i64.extend32_s", I64_EXTEND32_S),
        };
        for (Entry entry : entries) {
            byte opc = entry.opcode;
            OPCODES.put(entry.name, lp -> pure(new InsnNode(opc)));
        }

        String[] names = {
                "i32.trunc_sat_f32_s",
                "i32.trunc_sat_f32_u",
                "i32.trunc_sat_f64_s",
                "i32.trunc_sat_f64_u",
                "i64.trunc_sat_f32_s",
                "i64.trunc_sat_f32_u",
                "i64.trunc_sat_f64_s",
                "i64.trunc_sat_f64_u",
        };
        int[] iOps = {
                I32_TRUNC_SAT_F32_S,
                I32_TRUNC_SAT_F32_U,
                I32_TRUNC_SAT_F64_S,
                I32_TRUNC_SAT_F64_U,
                I64_TRUNC_SAT_F32_S,
                I64_TRUNC_SAT_F32_U,
                I64_TRUNC_SAT_F64_S,
                I64_TRUNC_SAT_F64_U,
        };
        for (int i = 0; i < names.length; i++) {
            int opc = iOps[i];
            OPCODES.put(names[i], lp -> pure(new PrefixInsnNode(opc)));
        }
    }

    private static AbstractInsnNode parseMemArg(byte opcode, int n, ListParser lp) {
        Function<Object, BigInteger> extractValue = it -> ((MemArgPart) it).value;
        int offset = lp.maybeParse(it -> it instanceof MemArgPart && ((MemArgPart) it).type == MemArgPart.Type.OFFSET)
                .map(extractValue.andThen(Parser::truncToU32))
                .orElse(0);
        int align = lp.maybeParse(it -> it instanceof MemArgPart && ((MemArgPart) it).type == MemArgPart.Type.ALIGN)
                .map(extractValue.andThen(Parser::truncToU32))
                .orElse(n);
        if ((n & (n - 1)) != 0) {
            throw new ParseException("align not a power of two", lp.iter.previous());
        }
        return new MemInsnNode(opcode, align, offset);
    }

    private static byte parseHeapType(ListParser lp) {
        Object next = lp.expect();
        if ("func".equals(next)) return FUNCREF;
        if ("extern".equals(next)) return EXTERNREF;
        throw new ParseException("expected heaptype", next);
    }

    @NotNull
    private static IdVal<Integer> maybeParseIdx(IdCtx.Field field, ListParser lp) {
        return lp.maybeParse(Parser::couldBeId)
                .map(it -> parseIdx(field, it))
                .orElse((mod, idcx) -> 0);
    }

    private static IdVal<Integer> parseLabelIdx(Object obj) {
        // The IndexedList indexes labels with *absolute* depth, but instructions use relative depths,
        // so invert if it is referenced by name.
        return parseIdx(LABEL, obj)
                .bind(raw -> (mod, idcx) -> obj instanceof BigInteger ? raw : idcx.f(LABEL).size() - raw);
    }

    private interface InstrSeq {
        void resolveInsns(List<AbstractInsnNode> insns, ModuleNode mod, IdCtx idcx);
    }

    private static boolean isBlockOpcode(String opcode) {
        switch (opcode) {
            case "block":
            case "loop":
            case "if":
                return true;
            default:
                return false;
        }
    }

    private static byte[] parseResults(ListParser lp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Optional<Object> maybeResult;
        while ((maybeResult = lp.maybeParse(it -> isMacroList(it, "result").isPresent())).isPresent()) {
            ListIterator<?> li = ((List<?>) maybeResult.get()).listIterator(1);
            while (li.hasNext()) {
                baos.write(parseValType(li.next()));
            }
        }
        return baos.toByteArray();
    }

    private static IdVal<BlockType> parseBlockType(ListParser lp) {
        IdVal<BlockType> blockTy;
        Optional<Object> maybeTypeUse = lp.maybeParse(it -> isMacroList(it, "type", "param", "result").isPresent());
        if (!maybeTypeUse.isPresent()) {
            // empty blocktype (yay!)
            blockTy = (mod, idcx) -> new BlockType(BlockType.Kind.VALTYPE, EMPTY_TYPE);
        } else breakIfNiceEasyValtype:{
            // check if it's just a single result first...
            List<?> typeUseStart = (List<?>) maybeTypeUse.get();
            if (isMacroList(typeUseStart, "result").isPresent()
                    && typeUseStart.size() == 2) {
                // at least one result
                if (!lp.maybeParse(it -> isMacroList(it, "result").isPresent()).isPresent()) {
                    byte resTy = parseValType(typeUseStart.get(1));
                    blockTy = (mod, idcx) -> new BlockType(BlockType.Kind.VALTYPE, resTy);
                    break breakIfNiceEasyValtype;
                }
                lp.iter.previous(); // more than one result, it's a typeuse abbrev :(
            }
            lp.iter.previous();

            TypeUse tu = parseTypeUse(lp);
            if (tu.locals != null && !tu.locals.index.isEmpty()) {
                throw new ParseException("binding locals in a blocktype is illegal", lp.list);
            }
            blockTy = tu.fmap(idx -> new BlockType(BlockType.Kind.FUNCTYPE, idx));
        }
        return blockTy;
    }

    private static IdVal<AbstractInsnNode> parsePlainInstr(ListParser lp) {
        String op = expectClass(String.class, lp.expect());
        if (!OPCODES.containsKey(op)) throw new ParseException("unrecognised instruction", op);
        return OPCODES.get(op).parse(lp);
    }

    private static InstrSeq parseFlatInstr(ListParser lp) {
        Optional<Object> maybeBlockOpcode = lp.maybeParse(it -> it instanceof String && isBlockOpcode((String) it));
        if (!maybeBlockOpcode.isPresent()) {
            IdVal<AbstractInsnNode> idv = parsePlainInstr(lp);
            return (insns, mod, idcx) -> insns.add(idv.resolve(mod, idcx));
        } else {
            String op = (String) maybeBlockOpcode.get();
            byte opcode;
            switch (op) {
                // @formatter:off
                case "block": opcode = BLOCK; break;
                case "loop": opcode = LOOP; break;
                case "if": opcode = IF; break;
                // @formatter:on
                default:
                    throw new AssertionError();
            }

            Optional<String> maybeLabelId = lp.maybeParseId();
            IdVal<BlockType> blockTy = parseBlockType(lp);
            InstrSeq ins = parseInstrs0(lp);

            Consumer<Optional<String>> checkRepeatLabel = (otherLabel) -> {
                if (otherLabel.isPresent()) {
                    if (!maybeLabelId.isPresent()) {
                        throw new ParseException("repeated label with no start label", otherLabel.get());
                    }
                    if (!maybeLabelId.get().equals(otherLabel.get())) {
                        throw new ParseException("repeated label does not match start label", otherLabel.get());
                    }
                }
            };

            if ("if".equals(op)) {
                if (lp.maybeParse("else"::equals).isPresent()) {
                    checkRepeatLabel.accept(lp.maybeParseId());
                    InstrSeq thens = ins;
                    InstrSeq elses = parseInstrs0(lp);
                    ins = (insns, mod, idcx) -> {
                        thens.resolveInsns(insns, mod, idcx);
                        insns.add(new ElseInsnNode());
                        elses.resolveInsns(insns, mod, idcx);
                    };
                }
            }
            expectEq("end", lp.expect());
            checkRepeatLabel.accept(lp.maybeParseId());

            InstrSeq finalIns = ins;
            return (insns, mod, idcx) -> {
                idcx.f(LABEL).add(maybeLabelId.orElse(null));

                insns.add(new BlockInsnNode(opcode, blockTy.resolve(mod, idcx)));
                finalIns.resolveInsns(insns, mod, idcx);
                insns.add(new EndInsnNode());

                idcx.f(LABEL).popLast();
            };
        }
    }

    private static InstrSeq parseFoldedInstr(List<?> sexpList) {
        return mark(sexpList, () -> {
            if (isMacroList(sexpList, "block", "loop").isPresent()) {
                List<Object> flatList = new ArrayList<>(sexpList);
                flatList.add("end");
                return parseFlatInstr(new ListParser(flatList));
            }

            ListParser sLp = new ListParser(sexpList);
            if (sLp.maybeParse("if"::equals).isPresent()) {
                Optional<String> maybeLabelId = sLp.maybeParseId();
                IdVal<BlockType> blockTy = parseBlockType(sLp);

                List<InstrSeq> condInstrs = new ArrayList<>();

                Optional<Object> maybeThen;
                while (!(maybeThen = sLp.maybeParse(it -> isMacroList(it, "then").isPresent())).isPresent()) {
                    condInstrs.add(parseFoldedInstr(expectList(sLp.expect())));
                }
                List<?> thenBlock = (List<?>) maybeThen.get();
                ListParser thenLp = new ListParser(thenBlock, 1);

                InstrSeq thenInstrs = parseInstrs0(thenLp);

                InstrSeq elseInstrs;
                if (sLp.iter.hasNext()) {
                    List<?> elseBlock = expectList(sLp.iter.next());
                    ListParser elseLp = new ListParser(elseBlock);
                    expectEq("else", elseLp.expect());
                    elseInstrs = parseInstrs0(elseLp);
                } else {
                    elseInstrs = (insns, mod, idcx) -> {
                    };
                }

                return (insns, mod, idcx) -> {
                    for (InstrSeq condInstr : condInstrs) {
                        condInstr.resolveInsns(insns, mod, idcx);
                    }

                    idcx.f(LABEL).add(maybeLabelId.orElse(null));

                    insns.add(new BlockInsnNode(IF, blockTy.resolve(mod, idcx)));
                    thenInstrs.resolveInsns(insns, mod, idcx);
                    insns.add(new ElseInsnNode());
                    elseInstrs.resolveInsns(insns, mod, idcx);
                    insns.add(new EndInsnNode());

                    idcx.f(LABEL).popLast();
                };
            }

            IdVal<AbstractInsnNode> f = parsePlainInstr(sLp);
            List<InstrSeq> args = new ArrayList<>();
            while (sLp.iter.hasNext()) {
                args.add(parseFoldedInstr(expectList(sLp.expect())));
            }
            return (insns, mod, idcx) -> {
                for (InstrSeq arg : args) {
                    arg.resolveInsns(insns, mod, idcx);
                }
                insns.add(f.resolve(mod, idcx));
            };
        });
    }

    private static InstrSeq parseSingleInsn(ListParser lp) {
        Optional<Object> maybeFolded = lp.maybeParse(List.class::isInstance);
        if (maybeFolded.isPresent()) {
            return parseFoldedInstr((List<?>) maybeFolded.get());
        } else {
            return parseFlatInstr(lp);
        }
    }

    private static InstrSeq parseInstrs0(ListParser lp) {
        List<InstrSeq> recInsns = new ArrayList<>();
        while (lp.iter.hasNext()) {
            if (lp.maybeParse(it -> "end".equals(it) || "else".equals(it)).isPresent()) {
                lp.iter.previous();
                break;
            }
            recInsns.add(parseSingleInsn(lp));
        }
        return (insns, mod, idcx) -> {
            for (InstrSeq recInsn : recInsns) {
                recInsn.resolveInsns(insns, mod, idcx);
            }
        };
    }

    private static IdVal<List<AbstractInsnNode>> parseInstrs(ListParser lp) {
        InstrSeq iseq = parseInstrs0(lp);
        return (mod, idcx) -> {
            List<AbstractInsnNode> realInstrs = new ArrayList<>();
            iseq.resolveInsns(realInstrs, mod, idcx);
            return realInstrs;
        };
    }

    private static IdVal<ExprNode> parseExpr(ListParser lp) {
        IdVal<List<AbstractInsnNode>> instrs = parseInstrs(lp);
        return (mod, idcx) -> {
            ExprNode en = new ExprNode();
            en.instructions = new LinkedList<>(instrs.resolve(mod, idcx));
            en.instructions.add(new EndInsnNode());
            return en;
        };
    }

    private static void parseTableField(List<ModuleField> fields, List<?> list) {
        ListParser lp = new ListParser(list, 1);
        Optional<String> maybeId = lp.maybeParseId();

        wrapExportImportAbbrevs(
                TABLE,
                EXPORTS_TABLE,
                module -> module.tables,
                maybeId.orElse(null),
                fields,
                lp,
                () -> {
                    TableNode tableTy = parseTableType(lp);
                    return pure((moduleName, name) -> new TableImportNode(moduleName, name, tableTy.limits, tableTy.type));
                },
                () -> {
                    // tabletype (= limits reftype) starts with a BigInteger, so if it's not that we have a valtype,
                    // thus it's an elem abbrev
                    Optional<Object> maybeValType = lp.maybeParse(it -> !(it instanceof BigInteger));
                    @Nullable ModuleField maybeElem = null;
                    TableNode table;
                    if (maybeValType.isPresent()) {
                        byte refTy = parseRefType(maybeValType.get());
                        List<?> elemAbbrev = expectList(lp.expect());
                        int n;
                        ListParser eLp = new ListParser(elemAbbrev);
                        expectEq("elem", eLp.expect());

                        IdVal<ElementNode> en;
                        if (eLp.peek().map(Parser::couldBeId).orElse(true)) {
                            List<IdVal<Integer>> inits = new ArrayList<>();
                            while (eLp.iter.hasNext()) {
                                inits.add(parseIdx(FUNC, eLp.iter.next()));
                            }
                            n = inits.size();
                            en = (mod, idcx) -> {
                                ElementNode node = new ElementNode();
                                node.indeces = new int[inits.size()];
                                int i = 0;
                                for (IdVal<Integer> init : inits) {
                                    node.indeces[i++] = init.resolve(mod, idcx);
                                }
                                return node;
                            };
                        } else {
                            List<IdVal<ExprNode>> inits = new ArrayList<>();
                            while (eLp.iter.hasNext()) {
                                inits.add(parseSingleInsnAbbrevExpr(new ListParser(expectList(eLp.expect())), "item"));
                            }
                            n = inits.size();
                            en = (mod, idcx) -> {
                                ElementNode node = new ElementNode();
                                node.init = new ArrayList<>(inits.size());
                                for (IdVal<ExprNode> init : inits) {
                                    node.init.add(init.resolve(mod, idcx));
                                }
                                return node;
                            };
                        }

                        table = new TableNode(new Limits(n, n), refTy);
                        maybeElem = new ModuleField() {
                            @Override
                            public void idc(IdCtx idcx) {
                                idcx.addIdx(ELEM, null);
                            }

                            @Override
                            public void mod(IdCtx idcx, ModuleNode module) {
                                if (module.elems == null) module.elems = new ElementSegmentsNode();
                                if (module.elems.elems == null) module.elems.elems = new ArrayList<>();

                                ElementNode node = en.resolve(module, idcx);

                                node.table = idcx.f(TABLE).size() - 1;

                                node.offset = new ExprNode();
                                node.offset.instructions = new LinkedList<>(Arrays.asList(
                                        new ConstInsnNode(0),
                                        new EndInsnNode()
                                ));

                                module.elems.elems.add(node);
                            }
                        };
                    } else {
                        table = parseTableType(lp);
                    }
                    lp.expectEnd();

                    fields.add(new ModuleField() {
                        @Override
                        public void idc(IdCtx idcx) {
                            idcx.addIdx(TABLE, maybeId.orElse(null));
                        }

                        @Override
                        public void mod(IdCtx idcx, ModuleNode module) {
                            if (module.tables == null) module.tables = new TablesNode();
                            if (module.tables.tables == null) module.tables.tables = new ArrayList<>();
                            module.tables.tables.add(table);
                        }
                    });
                    if (maybeElem != null) {
                        fields.add(maybeElem);
                    }
                });
    }

    private static TableNode parseTableType(ListParser lp) {
        return new TableNode(parseLimits(lp), parseRefType(lp.expect()));
    }

    private static Limits parseLimits(ListParser lp) {
        BigInteger min = expectClass(BigInteger.class, lp.expect());
        BigInteger max = lp.maybeParse(BigInteger.class::isInstance).map(BigInteger.class::cast).orElse(null);
        return new Limits(truncToU32(min), max == null ? null : truncToU32(max));
    }

    private static void parseMemField(List<ModuleField> fields, List<?> list) {
        ListParser lp = new ListParser(list, 1);
        Optional<String> maybeId = lp.maybeParseId();

        wrapExportImportAbbrevs(
                MEM,
                EXPORTS_MEM,
                module -> module.mems,
                maybeId.orElse(null),
                fields,
                lp,
                () -> {
                    MemoryNode memTy = parseMemType(lp);
                    return pure((moduleName, name) -> new MemImportNode(moduleName, name, memTy.limits));
                },
                () -> {
                    Optional<Object> maybeInlineData = lp.maybeParse(it -> isMacroList(it, "data").isPresent());
                    MemoryNode mem;
                    @Nullable ModuleField maybeDataField = null;
                    if (maybeInlineData.isPresent()) {
                        byte[] init = parseDataString(lp);
                        int m = init.length == 0 ? 0
                                : (init.length - 1) / PAGE_SIZE + 1; // ceil division
                        mem = new MemoryNode(new Limits(m, m));
                        maybeDataField = new ModuleField() {
                            @Override
                            public void idc(IdCtx idcx) {
                                idcx.addIdx(DATA, null);
                            }

                            @Override
                            public void mod(IdCtx idcx, ModuleNode module) {
                                if (module.datas == null) module.datas = new DataSegmentsNode();
                                if (module.datas.datas == null) module.datas.datas = new ArrayList<>();
                                int idx = idcx.f(MEM).size() - 1;
                                ExprNode offset = new ExprNode();
                                offset.instructions = new LinkedList<>(Arrays.asList(
                                        new ConstInsnNode(0),
                                        new EndInsnNode()
                                ));
                                module.datas.datas.add(new DataNode(init, idx, offset));
                            }
                        };
                    } else {
                        mem = parseMemType(lp);
                    }
                    fields.add(new ModuleField() {
                        @Override
                        public void idc(IdCtx idcx) {
                            idcx.addIdx(MEM, maybeId.orElse(null));
                        }

                        @Override
                        public void mod(IdCtx idcx, ModuleNode module) {
                            if (module.mems == null) module.mems = new MemoriesNode();
                            if (module.mems.memories == null) module.mems.memories = new ArrayList<>();
                            module.mems.memories.add(mem);
                        }
                    });
                    if (maybeDataField != null) {
                        fields.add(maybeDataField);
                    }
                });
    }

    private static MemoryNode parseMemType(ListParser lp) {
        return new MemoryNode(parseLimits(lp));
    }

    private static void parseGlobalField(List<ModuleField> fields, List<?> list) {
        ListParser lp = new ListParser(list, 1);
        Optional<String> maybeId = lp.maybeParseId();

        wrapExportImportAbbrevs(
                GLOBAL,
                EXPORTS_GLOBAL,
                module -> module.globals,
                maybeId.orElse(null),
                fields,
                lp,
                () -> {
                    GlobalTypeNode gTy = parseGlobalType(lp.expect());
                    return pure((moduleName, name) -> new GlobalImportNode(moduleName, name, gTy));
                },
                () -> {
                    GlobalTypeNode type = parseGlobalType(lp.expect());
                    IdVal<ExprNode> expr = parseExpr(lp);
                    fields.add(new ModuleField() {
                        @Override
                        public void idc(IdCtx idcx) {
                            idcx.addIdx(GLOBAL, maybeId.orElse(null));
                        }

                        @Override
                        public void mod(IdCtx idcx, ModuleNode module) {
                            if (module.globals == null) module.globals = new GlobalsNode();
                            if (module.globals.globals == null) module.globals.globals = new ArrayList<>();
                            module.globals.globals.add(new GlobalNode(type, expr.resolve(module, idcx)));
                        }
                    });
                });
    }

    private static GlobalTypeNode parseGlobalType(Object value) {
        byte mut, ty;
        if (value instanceof List) {
            mut = MUT_VAR;
            ListParser lp = new ListParser((List<?>) value);
            expectEq("mut", lp.expect());
            ty = parseValType(lp.expect());
            lp.expectEnd();
        } else {
            mut = MUT_CONST;
            ty = parseValType(value);
        }
        return new GlobalTypeNode(mut, ty);
    }

    private static void parseExportField(List<ModuleField> fields, List<?> list) {
        ListParser lp = new ListParser(list, 1);
        String nm = parseName(lp.expect());
        List<?> desc = expectList(lp.expect());
        lp.expectEnd();

        ListParser dLp = new ListParser(desc);
        IdCtx.Field field;
        byte type;
        switch (expectClass(String.class, dLp.expect())) {
            // @formatter:off
            case "func": field = FUNC; type = EXPORTS_FUNC; break;
            case "table": field = TABLE; type = EXPORTS_TABLE; break;
            case "memory": field = MEM; type = EXPORTS_MEM; break;
            case "global": field = GLOBAL; type = EXPORTS_GLOBAL; break;
            // @formatter:on
            default:
                throw new ParseException("unrecognised export descriptor", desc);
        }
        IdVal<Integer> idx = parseIdx(field, dLp.expect());
        dLp.expectEnd();
        fields.add(new ModuleField() {
            @Override
            public void idc(IdCtx idcx) {
            }

            @Override
            public void mod(IdCtx idcx, ModuleNode module) {
                if (module.exports == null) module.exports = new ExportsNode();
                if (module.exports.exports == null) module.exports.exports = new ArrayList<>();
                module.exports.exports.add(new ExportNode(nm, type, idx.resolve(module, idcx)));
            }
        });
    }

    private static void parseStartField(List<ModuleField> fields, List<?> list) {
        ListParser lp = new ListParser(list, 1);
        IdVal<Integer> x = parseIdx(FUNC, lp.expect());
        lp.expectEnd();
        fields.add(new ModuleField() {
            @Override
            public void idc(IdCtx idcx) {
            }

            @Override
            public void mod(IdCtx idcx, ModuleNode module) {
                if (module.start != null) {
                    throw new ParseException("duplicate start field", list);
                }
                module.start = x.resolve(module, idcx);
            }
        });
    }

    private static void parseElemField(List<ModuleField> fields, List<?> list) {
        ListParser lp = new ListParser(list, 1);
        Optional<String> maybeId = lp.maybeParseId();

        IdVal<ElementNode> en;

        boolean tableOmitted = false;
        if (lp.maybeParse("declare"::equals).isPresent()) {
            ElementNode node = new ElementNode();
            node.passive = false;
            en = pure(node);
        } else if (lp.maybeParse(List.class::isInstance).isPresent()) {
            lp.iter.previous();
            Optional<Object> maybeTableUse;
            IdVal<Integer> tableIdx;
            if ((maybeTableUse = lp.maybeParse(it -> isMacroList(it, "table").isPresent()))
                    .isPresent()) {
                ListParser tLp = new ListParser((List<?>) maybeTableUse.get(), 1);
                tableIdx = parseIdx(TABLE, tLp.expect());
                tLp.expectEnd();
            } else {
                tableOmitted = true;
                tableIdx = pure(0);
            }
            IdVal<ExprNode> offsetV = parseSingleInsnAbbrevExpr(new ListParser(expectList(lp.expect())), "offset");
            en = tableIdx.ap(offsetV, (table, offset) -> {
                ElementNode node = new ElementNode();
                node.table = table;
                node.offset = offset;
                return node;
            });
        } else {
            ElementNode node = new ElementNode();
            node.passive = true;
            en = pure(node);
        }

        if (lp.maybeParse("func"::equals).isPresent()
                || (tableOmitted &&
                lp.peek().map(Parser::couldBeId).orElse(true))) {
            List<IdVal<Integer>> funcs = new ArrayList<>();
            while (lp.iter.hasNext()) {
                funcs.add(parseIdx(FUNC, lp.iter.next()));
            }
            en = en.bind(node -> (mod, idcx) -> {
                node.type = FUNCREF;
                node.indeces = new int[funcs.size()];
                int i = 0;
                for (IdVal<Integer> func : funcs) {
                    node.indeces[i++] = func.resolve(mod, idcx);
                }
                return node;
            });
        } else {
            byte refTy = parseRefType(lp.expect());
            List<IdVal<ExprNode>> inits = new ArrayList<>();
            while (lp.iter.hasNext()) {
                inits.add(parseSingleInsnAbbrevExpr(new ListParser(expectList(lp.iter.next())), "item"));
            }
            en = en.bind(node -> (mod, idcx) -> {
                node.type = refTy;
                node.init = new ArrayList<>(inits.size());
                for (IdVal<ExprNode> init : inits) {
                    node.init.add(init.resolve(mod, idcx));
                }
                return node;
            });
        }

        IdVal<ElementNode> finalEn = en;
        fields.add(new ModuleField() {
            @Override
            public void idc(IdCtx idcx) {
                idcx.addIdx(ELEM, maybeId.orElse(null));
            }

            @Override
            public void mod(IdCtx idcx, ModuleNode module) {
                if (module.elems == null) module.elems = new ElementSegmentsNode();
                if (module.elems.elems == null) module.elems.elems = new ArrayList<>();
                module.elems.elems.add(finalEn.resolve(module, idcx));
            }
        });
    }

    private static void parseDataField(List<ModuleField> fields, List<?> list) {
        ListParser lp = new ListParser(list, 1);
        Optional<String> maybeId = lp.maybeParseId();

        Object next = lp.expect();
        IdVal<DataNode> initlessData;
        if (!(next instanceof byte[])) {
            List<?> memUseOrOffset = expectList(next);
            ListParser muLp = new ListParser(memUseOrOffset);

            IdVal<Integer> memory;
            if (muLp.maybeParse("memory"::equals).isPresent()) {
                memory = parseIdx(MEM, muLp.expect());
                muLp.expectEnd();
                muLp = new ListParser(expectList(lp.expect()));
            } else {
                memory = (mod, idcx) -> 0;
            }

            IdVal<ExprNode> expr = parseSingleInsnAbbrevExpr(muLp, "offset");

            initlessData = (mod, idcx) -> new DataNode(null, memory.resolve(mod, idcx), expr.resolve(mod, idcx));
        } else {
            initlessData = (mod, idcx) -> new DataNode();
            lp.iter.previous();
        }

        byte[] init = parseDataString(lp);

        IdVal<DataNode> data = (mod, idcx) -> {
            DataNode node = initlessData.resolve(mod, idcx);
            node.init = init;
            return node;
        };
        fields.add(new ModuleField() {
            @Override
            public void idc(IdCtx idcx) {
                idcx.addIdx(DATA, maybeId.orElse(null));
            }

            @Override
            public void mod(IdCtx idcx, ModuleNode module) {
                if (module.datas == null) module.datas = new DataSegmentsNode();
                if (module.datas.datas == null) module.datas.datas = new ArrayList<>();
                module.datas.datas.add(data.resolve(module, idcx));
            }
        });
    }

    private static byte[] parseDataString(ListParser lp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (lp.iter.hasNext()) {
            byte[] bytes = expectClass(byte[].class, lp.iter.next());
            baos.write(bytes, 0, bytes.length);
        }
        return baos.toByteArray();
    }

    @NotNull
    private static IdVal<ExprNode> parseSingleInsnAbbrevExpr(ListParser lp, String s) {
        IdVal<ExprNode> expr;
        if (lp.maybeParse(s::equals).isPresent()) {
            expr = parseExpr(lp);
        } else {
            InstrSeq singleInsn = parseSingleInsn(lp);
            lp.expectEnd();
            expr = (mod, idcx) -> {
                ExprNode node = new ExprNode();
                singleInsn.resolveInsns(node.instructions = new LinkedList<>(), mod, idcx);
                node.instructions.add(new EndInsnNode());
                return node;
            };
        }
        return expr;
    }

    private static class ImportField implements ModuleField {
        private final IdCtx.Field field;
        private final @Nullable String maybeId;
        private final Function<ModuleNode, Object> getLocalObjects;
        private final Object in;
        private final IdVal<AbstractImportNode> theImport;

        public ImportField(
                IdCtx.Field field,
                @Nullable String maybeId,
                Function<ModuleNode, Object> getLocalObjects,
                Object in,
                IdVal<AbstractImportNode> theImport
        ) {
            this.field = field;
            this.maybeId = maybeId;
            this.getLocalObjects = getLocalObjects;
            this.in = in;
            this.theImport = theImport;
        }

        @Override
        public void idc(IdCtx idcx) {
            idcx.addIdx(field, maybeId);
        }

        @Override
        public void mod(IdCtx idcx, ModuleNode module) {
            if (getLocalObjects.apply(module) != null) {
                throw new ParseException(
                        "imports must precede local " + field + "s",
                        in
                );
            }
            if (module.imports == null) module.imports = new ImportsNode();
            if (module.imports.imports == null) module.imports.imports = new ArrayList<>();
            module.imports.imports.add(theImport.resolve(module, idcx));
        }
    }
}
