package io.github.eutro.jwasm;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
     * @param b      The byte array to read from.
     * @param offset The offset of the subarray to use.
     * @param len    The length of the subarray to use.
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
     * Make the given {@link ModuleVisitor} visit the structure of the module.
     *
     * @param mv The {@link ModuleVisitor}.
     * @throws E                   If the {@link ByteInputStream} throws an error while reading.
     * @throws ValidationException If the bytes represent an invalid module file that inhibits the parsing of the file.
     *                             Not all invalid modules will cause this method to throw this exception,
     *                             only those errors that prevent the parsing.
     */
    public void accept(ModuleVisitor mv) throws E {
        ByteInputStream<E> bb = source.get();
        if (bb.getUInt32() != Opcodes.MAGIC) throw new ValidationException("Wrong magic",
                new RuntimeException("magic header not detected"));
        if (bb.getUInt32() != Opcodes.VERSION) throw new ValidationException("Wrong version",
                new RuntimeException("unknown binary version"));
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
                for (int i = 0; Integer.compareUnsigned(i, typeCount) < 0; i++) {
                    if (sbb.expect() != Opcodes.TYPES_FUNCTION) {
                        throw new ValidationException(String.format("Malformed functype 0x%02x", Opcodes.TYPES_FUNCTION),
                                new RuntimeException("malformed functype")
                        );
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
                for (int i = 0; Integer.compareUnsigned(i, importCount) < 0; i++) {
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
                            Limits limit = sbb.getLimits();
                            iv.visitTableImport(module, name, limit.min, limit.max, type);
                            break;
                        }
                        case Opcodes.IMPORTS_MEM: {
                            Limits limit = sbb.getLimits();
                            iv.visitMemImport(module, name, limit.min, limit.max);
                            break;
                        }
                        case Opcodes.IMPORTS_GLOBAL: {
                            byte type = sbb.expect();
                            byte mut = expectMut(sbb.expect());
                            iv.visitGlobalImport(module, name, mut, type);
                            break;
                        }
                        default:
                            throw new ValidationException(String.format("Unrecognised import kind 0x%02x", importType),
                                    new RuntimeException("malformed import kind"));
                    }
                }

                iv.visitEnd();
                sbb.expectEmpty();
            }
            section = bb.get();
        }

        section = acceptCustoms(mv, bb, section);

        long funcCount = 0;
        if (section == Opcodes.SECTION_FUNCTION) {
            sbb = bb.sectionStream();

            FunctionsVisitor fv = mv.visitFuncs();
            funcCount = Integer.toUnsignedLong(sbb.getVarUInt32());
            if (fv == null) {
                sbb.skipAll();
            } else {
                for (long i = 0; i < funcCount; i++) {
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
                for (int i = 0; Integer.compareUnsigned(i, tableCount) < 0; i++) {
                    byte type = expectRefType(sbb.expect());
                    Limits limit = sbb.getLimits();
                    tv.visitTable(limit.min, limit.max, type);
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
                for (int i = 0; Integer.compareUnsigned(i, memCount) < 0; i++) {
                    Limits limit = sbb.getLimits();
                    mmv.visitMemory(limit.min, limit.max);
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
                for (int i = 0; Integer.compareUnsigned(i, globCount) < 0; i++) {
                    byte type = sbb.expect();
                    byte mut = expectMut(sbb.expect());
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
                for (int i = 0; Integer.compareUnsigned(i, expCount) < 0; i++) {
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
        if (section == Opcodes.SECTION_START) {
            throw new ValidationException("Multiple start sections",
                    new RuntimeException("multiple start sections"));
        }

        section = acceptCustoms(mv, bb, section);

        if (section == Opcodes.SECTION_ELEMENT) {
            sbb = bb.sectionStream();

            ElementSegmentsVisitor ev = mv.visitElems();
            if (ev == null) {
                sbb.skipAll();
            } else {
                int elemCount = sbb.getVarUInt32();
                for (int i = 0; Integer.compareUnsigned(i, elemCount) < 0; i++) {
                    ElementVisitor elv = ev.visitElem();
                    int elemType = sbb.getVarUInt32();

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
                            byte type = expectRefType(sbb.expect());
                            if (elv != null) elv.visitType(type);
                        }

                        int initLen = sbb.getVarUInt32();
                        for (int j = 0; Integer.compareUnsigned(j, initLen) < 0; j++) {
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

                        int funcIndices = sbb.getVarUInt32();
                        if (funcIndices < 0) throw new ValidationException("Too many elements");
                        int[] indices = new int[funcIndices];
                        for (int j = 0; j < funcIndices; j++) {
                            indices[j] = sbb.getVarUInt32();
                        }
                        if (elv != null) elv.visitElemIndices(indices);
                    }
                    if (elv != null) elv.visitEnd();
                }

                ev.visitEnd();
                sbb.expectEmpty();
            }
            section = bb.get();
        }

        section = acceptCustoms(mv, bb, section);

        long dataCount = -1;
        if (section == Opcodes.SECTION_DATA_COUNT) {
            sbb = bb.sectionStream();
            dataCount = Integer.toUnsignedLong(sbb.getVarUInt32());
            mv.visitDataCount((int) dataCount);
            sbb.expectEmpty();
            section = bb.get();
        }

        section = acceptCustoms(mv, bb, section);

        long codeCount = 0;
        if (section == Opcodes.SECTION_CODE) {
            sbb = bb.sectionStream();

            CodesVisitor cv = mv.visitCode();
            codeCount = Integer.toUnsignedLong(sbb.getVarUInt32());

            DataUse use = DataUse.NO_USE;

            if (cv == null) {
                sbb.skipAll();
            } else {
                for (long i = 0; i < codeCount; i++) {
                    ByteInputStream<E> fbb = sbb.sectionStream();

                    byte[] locals;
                    int localsCount = fbb.getVarUInt32();
                    if (localsCount < 0) throw new ValidationException("Too many local variables");

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
                            if (n < 0 || nsum < 0) {
                                throw new ValidationException("Too many local variables",
                                        new RuntimeException("too many locals"));
                            }
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
                        use = use.or(acceptExpr(fbb, ev));
                        fbb.expectEmpty();
                    }
                }
                cv.visitEnd();

                sbb.expectEmpty();
            }
            section = bb.get();

            if (use != DataUse.NO_USE) {
                if (dataCount == -1) {
                    throw new ValidationException("Data indices used but no data count section present",
                            new RuntimeException("data count section required"));
                }
            }
        }

        section = acceptCustoms(mv, bb, section);

        if (section == Opcodes.SECTION_DATA) {
            sbb = bb.sectionStream();

            DataSegmentsVisitor dv = mv.visitDatas();
            if (dv == null) {
                sbb.skipAll();
            } else {
                long dataLen = Integer.toUnsignedLong(sbb.getVarUInt32());
                if (dataCount != -1 && dataCount != dataLen) {
                    throw new ValidationException("Data section length does not match data count",
                            new RuntimeException("data count and data section have inconsistent lengths"));
                }
                for (long i = 0; i < dataLen; i++) {
                    DataVisitor ddv = dv.visitData();
                    int dataType = sbb.getVarUInt32();
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

        if (section != -1) throw new ValidationException(String.format("Unexpected section: 0x%02x", section),
                new RuntimeException("malformed section id"));

        if (funcCount != codeCount) {
            throw new ValidationException("Function and code section counts differ",
                    new RuntimeException("function and code section have inconsistent lengths"));
        }

        mv.visitEnd();
    }

    private static byte expectMut(byte mut) {
        if (mut != Opcodes.MUT_VAR && mut != Opcodes.MUT_CONST) {
            throw new ValidationException("Malformed global mutability",
                    new RuntimeException("malformed mutability"));
        }
        return mut;
    }

    private enum DataUse {
        NO_USE,
        USES_DATA,
        ;

        DataUse or(DataUse o) {
            if (this == USES_DATA) return this;
            return o;
        }
    }

    private byte expectRefType(byte ty) {
        if (ty != Opcodes.FUNCREF && ty != Opcodes.EXTERNREF) {
            throw new ValidationException(String.format("Invalid reftype %02x", ty),
                    new RuntimeException("malformed reference type"));
        }
        return ty;
    }

    private DataUse acceptExpr(ByteInputStream<E> bb, ExprVisitor ev) throws E {
        int depth = 0;
        if (ev == null) ev = new ExprVisitor();
        byte opcode;
        DataUse use = DataUse.NO_USE;
        while (true) {
            int c = bb.get();
            if (c == -1) {
                throw new ValidationException("Expected more instructions",
                        new RuntimeException("unexpected end of section or function"));
            }
            opcode = (byte) c;
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
                        return use;
                    }
                    break;
                case Opcodes.BR:
                case Opcodes.BR_IF:
                    ev.visitBreakInsn(opcode, bb.getVarUInt32());
                    break;
                case Opcodes.BR_TABLE: {
                    int indexCount = bb.getVarUInt32();
                    int[] indices = new int[indexCount];
                    for (int i = 0; i < indexCount; i++) {
                        indices[i] = bb.getVarUInt32();
                    }
                    ev.visitTableBreakInsn(indices, bb.getVarUInt32());
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
                    ev.visitNullInsn(expectRefType(bb.expect()));
                    break;
                case Opcodes.REF_FUNC:
                    ev.visitFuncRefInsn(bb.getVarUInt32());
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
                    if (bb.expect() != 0x00) throw zeroExpected();
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
                case Opcodes.INSN_PREFIX: {
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
                            use = DataUse.USES_DATA;
                            if (intOpcode == Opcodes.MEMORY_INIT) {
                                if (bb.expect() != 0) throw zeroExpected();
                            }
                            ev.visitIndexedMemInsn(intOpcode, index);
                            break;
                        case Opcodes.MEMORY_COPY:
                            if (bb.expect() != 0) throw zeroExpected();
                            // fall through
                        case Opcodes.MEMORY_FILL:
                            if (bb.expect() != 0) throw zeroExpected();
                            // fall through
                        default:
                            ev.visitPrefixInsn(intOpcode);
                            break;
                    }
                    break;
                }
                case Opcodes.VECTOR_PREFIX: {
                    int intOpcode = bb.getVarUInt32();
                    switch (intOpcode) {
                        case Opcodes.V128_LOAD:
                        case Opcodes.V128_LOAD8X8_S:
                        case Opcodes.V128_LOAD8X8_U:
                        case Opcodes.V128_LOAD16X4_S:
                        case Opcodes.V128_LOAD16X4_U:
                        case Opcodes.V128_LOAD32X2_S:
                        case Opcodes.V128_LOAD32X2_U:
                        case Opcodes.V128_LOAD8_SPLAT:
                        case Opcodes.V128_LOAD16_SPLAT:
                        case Opcodes.V128_LOAD32_SPLAT:
                        case Opcodes.V128_LOAD64_SPLAT:
                        case Opcodes.V128_LOAD32_ZERO:
                        case Opcodes.V128_LOAD64_ZERO:
                        case Opcodes.V128_STORE:
                            ev.visitVectorMemInsn(intOpcode, bb.getVarUInt32(), bb.getVarUInt32());
                            break;
                        case Opcodes.V128_LOAD8_LANE:
                        case Opcodes.V128_LOAD16_LANE:
                        case Opcodes.V128_LOAD32_LANE:
                        case Opcodes.V128_LOAD64_LANE:
                        case Opcodes.V128_STORE8_LANE:
                        case Opcodes.V128_STORE16_LANE:
                        case Opcodes.V128_STORE32_LANE:
                        case Opcodes.V128_STORE64_LANE:
                            ev.visitVectorMemLaneInsn(intOpcode, bb.getVarUInt32(), bb.getVarUInt32(), bb.expect());
                            break;
                        case Opcodes.V128_CONST:
                        case Opcodes.I8X16_SHUFFLE: {
                            byte[] args = new byte[16];
                            if (bb.get(args, 0, 16) < 16) {
                                bb.expect(); // throw
                            }
                            ev.visitVectorConstOrShuffleInsn(intOpcode, args);
                            break;
                        }
                        case Opcodes.I8X16_EXTRACT_LANE_S:
                        case Opcodes.I8X16_EXTRACT_LANE_U:
                        case Opcodes.I8X16_REPLACE_LANE:
                        case Opcodes.I16X8_EXTRACT_LANE_S:
                        case Opcodes.I16X8_EXTRACT_LANE_U:
                        case Opcodes.I16X8_REPLACE_LANE:
                        case Opcodes.I32X4_EXTRACT_LANE:
                        case Opcodes.I32X4_REPLACE_LANE:
                        case Opcodes.I64X2_EXTRACT_LANE:
                        case Opcodes.I64X2_REPLACE_LANE:
                        case Opcodes.F32X4_EXTRACT_LANE:
                        case Opcodes.F32X4_REPLACE_LANE:
                        case Opcodes.F64X2_EXTRACT_LANE:
                        case Opcodes.F64X2_REPLACE_LANE:
                            ev.visitVectorLaneInsn(intOpcode, bb.expect());
                            break;
                        default:
                            ev.visitVectorInsn(intOpcode);
                            break;
                    }
                    break;
                }
                default:
                    if (Byte.toUnsignedInt(opcode)
                            > Byte.toUnsignedInt(Opcodes.I64_EXTEND_I32_S)) {
                        throw new ValidationException(String.format("Unrecognised single-byte opcode %02x", opcode),
                                new RuntimeException("illegal opcode"));
                    }
                    ev.visitInsn(opcode);
                    break;
            }
        }
    }

    @NotNull
    private static ValidationException zeroExpected() {
        return new ValidationException("Expected 0x00",
                new RuntimeException("zero byte expected"));
    }

    private int acceptCustoms(ModuleVisitor mv, ByteInputStream<E> bb, int section) throws E {
        ByteInputStream.SectionInputStream<E> sbb;
        for (; section == Opcodes.SECTION_CUSTOM; section = bb.get()) {
            int length = bb.getVarUInt32();
            sbb = (ByteInputStream.SectionInputStream<E>) bb.sectionStream(length);

            byte[] stringBytes = sbb.getByteArray();
            String name = ByteInputStream.decodeName(stringBytes);
            int payloadLength = length - sbb.gotten;
            if (payloadLength < 0) {
                throw new ValidationException("Expected more bytes",
                        new RuntimeException("unexpected end"));
            }
            byte[] payload = new byte[payloadLength];
            if (sbb.get(payload, 0, payloadLength) < payloadLength) {
                throw new ValidationException("Expected more bytes",
                        new RuntimeException("length out of bounds"));
            }
            mv.visitCustom(name, payload);
            sbb.expectEmpty();
        }
        return section;
    }

}
