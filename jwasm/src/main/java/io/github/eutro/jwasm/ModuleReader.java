package io.github.eutro.jwasm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * A parser that makes a {@link ModuleVisitor} visit a WebAssembly module structure.
 * <p>
 * This class parses a binary module's contents, calling the appropriate {@code visit} methods
 * of the {@link ModuleVisitor} and any other relevant {@code *Visitor}s it returns.
 *
 * @param <E> The exception that may be thrown on read errors by {@link #accept(ModuleVisitor)}.
 */
public class ModuleReader<E extends Exception> {
    /**
     * The supplier of {@link ByteInputStream}s to read the module from.
     */
    private final Supplier<ByteInputStream<E>> source;

    /**
     * Construct a {@link ModuleReader} with the given source of bytes.
     *
     * @param source The supplier of {@link ByteInputStream}s to read the module from.
     */
    public ModuleReader(Supplier<ByteInputStream<E>> source) {
        this.source = source;
    }

    /**
     * Construct a {@link ModuleReader} that reads from the given byte array.
     *
     * @param b The byte array to read from.
     * @return The new {@link ModuleReader}.
     */
    public static ModuleReader<RuntimeException> fromBytes(byte[] b) {
        return fromBytes(b, 0, b.length);
    }

    /**
     * Construct a {@link ModuleReader} that reads from a subarray of the given byte array,
     * starting at {@code offset} with a length of {@code len} bytes.
     *
     * @param b The byte array to read from.
     * @param offset The offset of the subarray to use.
     * @param len The length of the subarray to use.
     * @return The new {@link ModuleReader}.
     */
    public static ModuleReader<RuntimeException> fromBytes(byte[] b, int offset, int len) {
        return new ModuleReader<>(() -> new ByteInputStream.ByteBufferByteInputStream(ByteBuffer.wrap(b, offset, len).order(ByteOrder.LITTLE_ENDIAN)));
    }

    /**
     * Construct a {@link ModuleReader} that reads from the given input stream.
     * <p>
     * The {@link #accept(ModuleVisitor)} method of the returned {@link ModuleReader} can only be called once,
     * or an {@link IllegalStateException} will be thrown.
     *
     * @param is The input stream to read from.
     * @return The new {@link ModuleReader}.
     */
    public static ModuleReader<IOException> fromInputStream(InputStream is) {
        AtomicBoolean gotten = new AtomicBoolean(false);
        return new ModuleReader<>(() -> {
            if (gotten.getAndSet(true)) throw new IllegalStateException("Cannot get byte stream more than once");
            return new ByteInputStream.InputStreamByteInputStream(is);
        });
    }

    /**
     * Makes the given {@link ModuleVisitor} visit the structure of the module.
     *
     * @param mv The {@link ModuleVisitor}.
     * @throws E                   If the {@link ByteInputStream} throws an error while reading.
     * @throws ValidationException If the bytes represent an invalid module file that inhibits the parsing of the file.
     *                             Not all invalid modules will cause this method to throw this exception,
     *                             only those errors that prevent the parsing.
     */
    public void accept(ModuleVisitor mv) throws E {
        ByteInputStream<E> bb = source.get();
        if (bb.getUInt32() != Opcodes.MAGIC) throw new ValidationException("Wrong magic");
        if (bb.getUInt32() != Opcodes.VERSION) throw new ValidationException("Wrong version");
        mv.visitHeader(Opcodes.VERSION);

        int section = bb.get();
        ByteInputStream<E> sbb;

        section = acceptCustoms(mv, bb, section);

        if (section == Opcodes.SECTION_TYPE) {
            sbb = bb.sectionStream();

            TypesVisitor tv = mv.visitTypes();
            if (tv == null) {
                sbb.skipAll();
            } else {
                int typeCount = sbb.getVarUInt32();
                for (int i = 0; i < typeCount; i++) {
                    if (sbb.expect() != Opcodes.TYPES_FUNCTION) {
                        throw new ValidationException(String.format("functype didn't begin with 0x%02x", Opcodes.TYPES_FUNCTION));
                    }
                    byte[] params = sbb.getByteArray();
                    byte[] returns = sbb.getByteArray();
                    tv.visitFuncType(params, returns);
                }

                tv.visitEnd();
                sbb.expectEmpty();
            }
            section = bb.get();
        }

        section = acceptCustoms(mv, bb, section);

        if (section == Opcodes.SECTION_IMPORT) {
            sbb = bb.sectionStream();

            ImportsVisitor iv = mv.visitImports();
            if (iv == null) {
                sbb.skipAll();
            } else {
                int importCount = sbb.getVarUInt32();
                for (int i = 0; i < importCount; i++) {
                    String module = sbb.getName();
                    String name = sbb.getName();
                    byte importType = sbb.expect();
                    switch (importType) {
                        case Opcodes.IMPORTS_FUNC: {
                            int index = sbb.getVarUInt32();
                            iv.visitFuncImport(module, name, index);
                            break;
                        }
                        case Opcodes.IMPORTS_TABLE: {
                            byte type = sbb.expect();
                            Integer[] limit = sbb.getLimit();
                            iv.visitTableImport(module, name, limit[0], limit[1], type);
                            break;
                        }
                        case Opcodes.IMPORTS_MEM: {
                            Integer[] limit = sbb.getLimit();
                            iv.visitMemImport(module, name, limit[0], limit[1]);
                            break;
                        }
                        case Opcodes.IMPORTS_GLOBAL: {
                            byte type = sbb.expect();
                            byte mut = sbb.expect();
                            iv.visitGlobalImport(module, name, mut, type);
                            break;
                        }
                        default:
                            throw new ValidationException(String.format("Unrecognised import type 0x%02x", importType));
                    }
                }

                iv.visitEnd();
                sbb.expectEmpty();
            }
            section = bb.get();
        }

        section = acceptCustoms(mv, bb, section);

        if (section == Opcodes.SECTION_FUNCTION) {
            sbb = bb.sectionStream();

            FunctionsVisitor fv = mv.visitFuncs();
            if (fv == null) {
                sbb.skipAll();
            } else {
                int typeCount = sbb.getVarUInt32();
                for (int i = 0; i < typeCount; i++) {
                    fv.visitFunc(sbb.getVarUInt32());
                }

                fv.visitEnd();
                sbb.expectEmpty();
            }
            section = bb.get();
        }

        section = acceptCustoms(mv, bb, section);

        if (section == Opcodes.SECTION_TABLE) {
            sbb = bb.sectionStream();

            TablesVisitor tv = mv.visitTables();
            if (tv == null) {
                sbb.skipAll();
            } else {
                int tableCount = sbb.getVarUInt32();
                for (int i = 0; i < tableCount; i++) {
                    byte type = sbb.expect();
                    Integer[] limit = sbb.getLimit();
                    tv.visitTable(limit[0], limit[1], type);
                }

                tv.visitEnd();
                sbb.expectEmpty();
            }
            section = bb.get();
        }

        section = acceptCustoms(mv, bb, section);

        if (section == Opcodes.SECTION_MEMORY) {
            sbb = bb.sectionStream();

            MemoriesVisitor mmv = mv.visitMems();
            if (mmv == null) {
                sbb.skipAll();
            } else {
                int memCount = sbb.getVarUInt32();
                for (int i = 0; i < memCount; i++) {
                    Integer[] limit = sbb.getLimit();
                    mmv.visitMemory(limit[0], limit[1]);
                }

                mmv.visitEnd();
                sbb.expectEmpty();
            }
            section = bb.get();
        }

        section = acceptCustoms(mv, bb, section);

        if (section == Opcodes.SECTION_GLOBAL) {
            sbb = bb.sectionStream();

            GlobalsVisitor gv = mv.visitGlobals();
            if (gv == null) {
                sbb.skipAll();
            } else {
                int globCount = sbb.getVarUInt32();
                for (int i = 0; i < globCount; i++) {
                    byte type = sbb.expect();
                    byte mut = sbb.expect();
                    acceptExpr(sbb, gv.visitGlobal(mut, type));
                }

                gv.visitEnd();
                sbb.expectEmpty();
            }
            section = bb.get();
        }

        section = acceptCustoms(mv, bb, section);

        if (section == Opcodes.SECTION_EXPORT) {
            sbb = bb.sectionStream();

            ExportsVisitor ev = mv.visitExports();
            if (ev == null) {
                sbb.skipAll();
            } else {
                int expCount = sbb.getVarUInt32();
                for (int i = 0; i < expCount; i++) {
                    String name = sbb.getName();
                    byte type = sbb.expect();
                    int index = sbb.getVarUInt32();
                    ev.visitExport(name, type, index);
                }

                ev.visitEnd();
                sbb.expectEmpty();
            }
            section = bb.get();
        }

        section = acceptCustoms(mv, bb, section);

        if (section == Opcodes.SECTION_START) {
            sbb = bb.sectionStream();

            int index = sbb.getVarUInt32();
            mv.visitStart(index);

            sbb.expectEmpty();
            section = bb.get();
        }

        section = acceptCustoms(mv, bb, section);

        if (section == Opcodes.SECTION_ELEMENT) {
            sbb = bb.sectionStream();

            ElementSegmentsVisitor ev = mv.visitElems();
            if (ev == null) {
                sbb.skipAll();
            } else {
                int elemCount = sbb.getVarUInt32();
                for (int i = 0; i < elemCount; i++) {
                    ElementVisitor elv = ev.visitElem();
                    byte elemType = sbb.expect();

                    if (elemType >= 0x08 || elemType < 0) {
                        throw new ValidationException(String.format("Unrecognised element type 0x%02x", elemType));
                    }

                    if ((elemType & Opcodes.ELEM_PASSIVE_OR_DECLARATIVE) != 0) {
                        if (elv != null) elv.visitNonActiveMode((elemType & Opcodes.ELEM_TABLE_INDEX) != 0);
                    } else {
                        int table = (elemType & Opcodes.ELEM_TABLE_INDEX) != 0 ? sbb.getVarUInt32() : 0;
                        acceptExpr(sbb, elv == null ? null : elv.visitActiveMode(table));
                    }

                    boolean implicitFuncref = (elemType & 0b011) == 0;
                    if (implicitFuncref && elv != null) elv.visitType(Opcodes.FUNCREF);
                    if ((elemType & Opcodes.ELEM_EXPRESSIONS) != 0) {
                        if (!implicitFuncref) {
                            byte type = sbb.expect();
                            if (elv != null) elv.visitType(type);
                        }

                        int initLen = sbb.getVarUInt32();
                        for (int j = 0; j < initLen; j++) {
                            acceptExpr(sbb, elv == null ? null : elv.visitInit());
                        }
                    } else {
                        if (!implicitFuncref) {
                            byte b = sbb.expect();
                            if (b != Opcodes.ELEMKIND) {
                                throw new ValidationException(String.format("Unrecognised elemkind 0x%02x", b));
                            }
                            if (elv != null) elv.visitType(Opcodes.FUNCREF);
                        }

                        int funcIndeces = sbb.getVarUInt32();
                        int[] indeces = new int[funcIndeces];
                        for (int j = 0; j < funcIndeces; j++) {
                            indeces[j] = sbb.getVarUInt32();
                        }
                        if (elv != null) elv.visitElemIndeces(indeces);
                    }
                    if (elv != null) elv.visitEnd();
                }

                ev.visitEnd();
                sbb.expectEmpty();
            }
            section = bb.get();
        }

        section = acceptCustoms(mv, bb, section);

        if (section == Opcodes.SECTION_DATA_COUNT) {
            sbb = bb.sectionStream();
            mv.visitDataCount(sbb.getVarUInt32());
            sbb.expectEmpty();
            section = bb.get();
        }

        section = acceptCustoms(mv, bb, section);

        if (section == Opcodes.SECTION_CODE) {
            sbb = bb.sectionStream();

            CodesVisitor cv = mv.visitCode();
            if (cv == null) {
                sbb.skipAll();
            } else {
                int codeCount = sbb.getVarUInt32();
                for (int i = 0; i < codeCount; i++) {
                    ByteInputStream<E> fbb = sbb.sectionStream();

                    byte[] locals;
                    int localsCount = fbb.getVarUInt32();

                    if (localsCount == 0) {
                        locals = new byte[0];
                    } else {
                        int nsum = 0;
                        int[] ns = new int[localsCount];
                        byte[] ts = new byte[localsCount];
                        for (int l = 0; l < localsCount; l++) {
                            int n = fbb.getVarUInt32();
                            ns[l] = n;
                            byte t = fbb.expect();
                            ts[l] = t;
                            nsum += n;
                        }
                        locals = new byte[nsum];
                        int index = 0;
                        for (int l = 0; l < localsCount; l++) {
                            Arrays.fill(locals, index, index + ns[l], ts[l]);
                            index += ns[l];
                        }
                    }

                    ExprVisitor ev = cv.visitCode(locals);
                    if (ev == null) {
                        fbb.skipAll();
                    } else {
                        acceptExpr(fbb, ev);
                        fbb.expectEmpty();
                    }
                }
                cv.visitEnd();

                sbb.expectEmpty();
            }
            section = bb.get();
        }

        section = acceptCustoms(mv, bb, section);

        if (section == Opcodes.SECTION_DATA) {
            sbb = bb.sectionStream();

            DataSegmentsVisitor dv = mv.visitDatas();
            if (dv == null) {
                sbb.skipAll();
            } else {
                int dataCount = sbb.getVarUInt32();
                for (int i = 0; i < dataCount; i++) {
                    DataVisitor ddv = dv.visitData();
                    byte dataType = sbb.expect();
                    if (dataType >= 0x04 || dataType < 0) {
                        throw new ValidationException(String.format("Unrecognised data section type 0x%02x", dataType));
                    }
                    if ((dataType & Opcodes.DATA_PASSIVE) == 0) {
                        int memory = (dataType & Opcodes.DATA_EXPLICIT) == 0 ? 0 : sbb.getVarUInt32();
                        acceptExpr(sbb, ddv.visitActive(memory));
                    }
                    byte[] init = sbb.getByteArray();
                    if (ddv != null) {
                        ddv.visitInit(init);
                        ddv.visitEnd();
                    }
                }

                dv.visitEnd();
                sbb.expectEmpty();
            }
            section = bb.get();
        }

        section = acceptCustoms(mv, bb, section);

        if (section != -1) throw new ValidationException(String.format("Unexpected section: 0x%02x", section));
        mv.visitEnd();
    }

    private void acceptExpr(ByteInputStream<E> bb, ExprVisitor ev) throws E {
        int depth = 0;
        if (ev == null) ev = new ExprVisitor();
        byte opcode;
        while (true) {
            opcode = bb.expect();
            switch (opcode) {
                case Opcodes.BLOCK:
                case Opcodes.LOOP:
                case Opcodes.IF:
                    ++depth;
                    ev.visitBlockInsn(opcode, bb.getBlockType());
                    break;
                case Opcodes.ELSE:
                    ev.visitElseInsn();
                    break;
                case Opcodes.END:
                    ev.visitEndInsn();
                    if (--depth < 0) {
                        ev.visitEnd();
                        return;
                    }
                    break;
                case Opcodes.BR:
                case Opcodes.BR_IF:
                    ev.visitBreakInsn(opcode, bb.getVarUInt32());
                    break;
                case Opcodes.BR_TABLE: {
                    int indexCount = bb.getVarUInt32();
                    int[] indeces = new int[indexCount];
                    for (int i = 0; i < indexCount; i++) {
                        indeces[i] = bb.getVarUInt32();
                    }
                    ev.visitTableBreakInsn(indeces, bb.getVarUInt32());
                    break;
                }
                case Opcodes.CALL:
                    ev.visitCallInsn(bb.getVarUInt32());
                    break;
                case Opcodes.CALL_INDIRECT: {
                    int y = bb.getVarUInt32();
                    int x = bb.getVarUInt32();
                    ev.visitCallIndirectInsn(x, y);
                    break;
                }
                case Opcodes.REF_NULL:
                    ev.visitNullInsn(bb.expect());
                    break;
                case Opcodes.REF_FUNC:
                    ev.visitFuncInsn(bb.getVarUInt32());
                    break;
                case Opcodes.SELECTT:
                    ev.visitSelectInsn(bb.getByteArray());
                    break;
                case Opcodes.LOCAL_GET:
                case Opcodes.LOCAL_SET:
                case Opcodes.LOCAL_TEE:
                case Opcodes.GLOBAL_GET:
                case Opcodes.GLOBAL_SET:
                    ev.visitVariableInsn(opcode, bb.getVarUInt32());
                    break;
                case Opcodes.TABLE_GET:
                case Opcodes.TABLE_SET:
                    ev.visitTableInsn(opcode, bb.getVarUInt32());
                    break;
                case Opcodes.I32_LOAD:
                case Opcodes.I64_LOAD:
                case Opcodes.F32_LOAD:
                case Opcodes.F64_LOAD:
                case Opcodes.I32_LOAD8_S:
                case Opcodes.I32_LOAD8_U:
                case Opcodes.I32_LOAD16_S:
                case Opcodes.I32_LOAD16_U:
                case Opcodes.I64_LOAD8_S:
                case Opcodes.I64_LOAD8_U:
                case Opcodes.I64_LOAD16_S:
                case Opcodes.I64_LOAD16_U:
                case Opcodes.I64_LOAD32_S:
                case Opcodes.I64_LOAD32_U:
                case Opcodes.I32_STORE:
                case Opcodes.I64_STORE:
                case Opcodes.F32_STORE:
                case Opcodes.F64_STORE:
                case Opcodes.I32_STORE8:
                case Opcodes.I32_STORE16:
                case Opcodes.I64_STORE8:
                case Opcodes.I64_STORE16:
                case Opcodes.I64_STORE32:
                    ev.visitMemInsn(opcode, bb.getVarUInt32(), bb.getVarUInt32());
                    break;
                case Opcodes.MEMORY_SIZE:
                case Opcodes.MEMORY_GROW:
                    if (bb.expect() != 0x00) throw new ValidationException("Expected 0x00");
                    ev.visitInsn(opcode);
                    break;
                case Opcodes.I32_CONST:
                    ev.visitConstInsn(bb.getVarSInt32());
                    break;
                case Opcodes.I64_CONST:
                    ev.visitConstInsn(bb.getVarSInt64());
                    break;
                case Opcodes.F32_CONST:
                    ev.visitConstInsn(bb.getFloat32());
                    break;
                case Opcodes.F64_CONST:
                    ev.visitConstInsn(bb.getFloat64());
                    break;
                case Opcodes.INSN_PREFIX:
                    int intOpcode = bb.getVarUInt32();
                    switch (intOpcode) {
                        case Opcodes.TABLE_INIT: {
                            int y = bb.getVarUInt32();
                            int x = bb.getVarUInt32();
                            ev.visitPrefixBinaryTableInsn(intOpcode, x, y);
                            break;
                        }
                        case Opcodes.TABLE_COPY: {
                            int x = bb.getVarUInt32();
                            int y = bb.getVarUInt32();
                            ev.visitPrefixBinaryTableInsn(intOpcode, x, y);
                            break;
                        }
                        case Opcodes.ELEM_DROP:
                        case Opcodes.TABLE_GROW:
                        case Opcodes.TABLE_SIZE:
                        case Opcodes.TABLE_FILL:
                            ev.visitPrefixTableInsn(intOpcode, bb.getVarUInt32());
                            break;
                        case Opcodes.MEMORY_INIT:
                        case Opcodes.DATA_DROP:
                            int index = bb.getVarUInt32();
                            if (intOpcode == Opcodes.MEMORY_INIT) bb.expect();
                            ev.visitIndexedMemInsn(intOpcode, index);
                            break;
                        case Opcodes.MEMORY_COPY:
                            bb.expect();
                            // fall through
                        case Opcodes.MEMORY_FILL:
                            bb.expect();
                            // fall through
                        default:
                            ev.visitPrefixInsn(intOpcode);
                            break;
                    }
                    break;
                default:
                    ev.visitInsn(opcode);
                    break;
            }
        }
    }

    private int acceptCustoms(ModuleVisitor mv, ByteInputStream<E> bb, int section) throws E {
        ByteInputStream<E> sbb;
        for (; section == Opcodes.SECTION_CUSTOM; section = bb.get()) {
            int length = bb.getVarUInt32();
            sbb = bb.sectionStream(length);

            byte[] stringBytes = sbb.getByteArray();
            String name = new String(stringBytes, StandardCharsets.UTF_8);
            int payloadLength = length - stringBytes.length - ByteOutputStream.DUMMY.putVarUInt(stringBytes.length);
            byte[] payload = new byte[payloadLength];
            sbb.get(payload, 0, payload.length);
            mv.visitCustom(name, payload);
        }
        return section;
    }

}
