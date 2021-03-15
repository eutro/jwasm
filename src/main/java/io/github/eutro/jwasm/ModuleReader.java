package io.github.eutro.jwasm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static io.github.eutro.jwasm.Opcodes.*;

public class ModuleReader<E extends Exception> {
    private final Supplier<ByteInputStream<E>> source;

    public ModuleReader(Supplier<ByteInputStream<E>> source) {
        this.source = source;
    }

    public static ModuleReader<RuntimeException> fromBytes(byte[] b) {
        return fromBytes(b, 0, b.length);
    }

    public static ModuleReader<RuntimeException> fromBytes(byte[] b, int offset, int len) {
        return new ModuleReader<>(() -> new ByteInputStream.ByteBufferByteInputStream(ByteBuffer.wrap(b, offset, len).order(ByteOrder.LITTLE_ENDIAN)));
    }

    public static ModuleReader<IOException> fromInputStream(InputStream is) {
        AtomicBoolean gotten = new AtomicBoolean(false);
        return new ModuleReader<>(() -> {
            if (gotten.getAndSet(true)) throw new IllegalStateException("Cannot get byte stream more than once");
            return new ByteInputStream.InputStreamByteInputStream(is);
        });
    }

    public void accept(ModuleVisitor mv) throws E {
        ByteInputStream<E> bb = source.get();
        if (bb.getUInt32() != MAGIC) throw new ValidationException("Wrong magic");
        if (bb.getUInt32() != VERSION) throw new ValidationException("Wrong version");
        mv.visitHeader(VERSION);

        int section = bb.get();
        ByteInputStream<E> sbb;

        section = acceptCustoms(mv, bb, section);

        if (section == SECTION_TYPE) {
            sbb = bb.sectionBuffer();

            TypesVisitor tv = mv.visitTypes();
            if (tv == null) {
                sbb.skipAll();
            } else {
                int typeCount = sbb.getVarUInt32();
                for (int i = 0; i < typeCount; i++) {
                    if (sbb.expect() != TYPES_FUNCTION) {
                        throw new ValidationException(String.format("functype didn't begin with 0x%02x", TYPES_FUNCTION));
                    }
                    byte[] params = sbb.getByteArray();
                    byte[] returns = sbb.getByteArray();
                    tv.visitType(params, returns);
                }

                tv.visitEnd();
                sbb.expectEmpty();
            }
            section = bb.get();
        }

        section = acceptCustoms(mv, bb, section);

        if (section == SECTION_IMPORT) {
            sbb = bb.sectionBuffer();

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
                        case IMPORTS_FUNC: {
                            int index = sbb.getVarUInt32();
                            iv.visitFuncImport(module, name, index);
                            break;
                        }
                        case IMPORTS_TABLE: {
                            byte type = sbb.expect();
                            Integer[] limit = sbb.getLimit();
                            iv.visitTableImport(module, name, limit[0], limit[1], type);
                            break;
                        }
                        case IMPORTS_MEM: {
                            Integer[] limit = sbb.getLimit();
                            iv.visitMemImport(module, name, limit[0], limit[1]);
                            break;
                        }
                        case IMPORTS_GLOBAL: {
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

        if (section == SECTION_FUNCTION) {
            sbb = bb.sectionBuffer();

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

        if (section == SECTION_TABLE) {
            sbb = bb.sectionBuffer();

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

        if (section == SECTION_MEMORY) {
            sbb = bb.sectionBuffer();

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

        if (section == SECTION_GLOBAL) {
            sbb = bb.sectionBuffer();

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

        if (section == SECTION_EXPORT) {
            sbb = bb.sectionBuffer();

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

        if (section == SECTION_START) {
            sbb = bb.sectionBuffer();

            int index = sbb.getVarUInt32();
            mv.visitStart(index);

            sbb.expectEmpty();
            section = bb.get();
        }

        section = acceptCustoms(mv, bb, section);

        if (section == SECTION_ELEMENT) {
            sbb = bb.sectionBuffer();

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

                    if ((elemType & ELEM_PASSIVE_OR_DECLARATIVE) != 0) {
                        if (elv != null) elv.visitNonActiveMode((elemType & ELEM_TABLE_INDEX) != 0);
                    } else {
                        int table = (elemType & ELEM_TABLE_INDEX) != 0 ? sbb.getVarUInt32() : 0;
                        acceptExpr(sbb, elv == null ? null : elv.visitActiveMode(table));
                    }

                    boolean implicitFuncref = (elemType & 0b011) == 0;
                    if (implicitFuncref && elv != null) elv.visitType(FUNCREF);
                    if ((elemType & ELEM_EXPRESSIONS) != 0) {
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
                            if (b != ELEMKIND) {
                                throw new ValidationException(String.format("Unrecognised elemkind 0x%02x", b));
                            }
                            if (elv != null) elv.visitType(FUNCREF);
                        }

                        int funcIndeces = sbb.getVarUInt32();
                        int[] indeces = new int[funcIndeces];
                        for (int j = 0; j < funcIndeces; j++) {
                            indeces[j] = sbb.getVarUInt32();
                        }
                        if (elv != null) elv.visitElemIneces(indeces);
                    }
                    if (elv != null) elv.visitEnd();
                }

                ev.visitEnd();
                sbb.expectEmpty();
            }
            section = bb.get();
        }

        section = acceptCustoms(mv, bb, section);

        if (section == SECTION_DATA_COUNT) {
            sbb = bb.sectionBuffer();
            mv.visitDataCount(sbb.getVarUInt32());
            sbb.expectEmpty();
            section = bb.get();
        }

        section = acceptCustoms(mv, bb, section);

        if (section == SECTION_CODE) {
            sbb = bb.sectionBuffer();

            CodesVisitor cv = mv.visitCode();
            if (cv == null) {
                sbb.skipAll();
            } else {
                int codeCount = sbb.getVarUInt32();
                for (int i = 0; i < codeCount; i++) {
                    ByteInputStream<E> fbb = sbb.sectionBuffer();

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

        if (section == SECTION_DATA) {
            sbb = bb.sectionBuffer();

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
                    if ((dataType & DATA_PASSIVE) == 0) {
                        int memory = (dataType & DATA_EXPLICIT) == 0 ? 0 : sbb.getVarUInt32();
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
                case BLOCK:
                case LOOP:
                case IF:
                    ++depth;
                    ev.visitBlockInsn(opcode, bb.getBlockType());
                    break;
                case ELSE:
                    ev.visitElseInsn();
                    break;
                case END:
                    ev.visitEndInsn();
                    if (--depth < 0) {
                        ev.visitEnd();
                        return;
                    }
                    break;
                case BR:
                case BR_IF:
                    ev.visitBreakInsn(opcode, bb.getVarUInt32());
                    break;
                case BR_TABLE: {
                    int indexCount = bb.getVarUInt32();
                    int[] indeces = new int[indexCount];
                    for (int i = 0; i < indexCount; i++) {
                        indeces[i] = bb.getVarUInt32();
                    }
                    ev.visitTableBreakInsn(indeces, bb.getVarUInt32());
                    break;
                }
                case CALL:
                    ev.visitCallInsn(bb.getVarUInt32());
                    break;
                case CALL_INDIRECT: {
                    int y = bb.getVarUInt32();
                    int x = bb.getVarUInt32();
                    ev.visitCallIndirectInsn(x, y);
                    break;
                }
                case REF_NULL:
                    ev.visitNullInsn(bb.expect());
                    break;
                case REF_FUNC:
                    ev.visitFuncInsn(bb.getVarUInt32());
                    break;
                case SELECTT:
                    ev.visitSelectInsn(bb.getByteArray());
                    break;
                case LOCAL_GET:
                case LOCAL_SET:
                case LOCAL_TEE:
                case GLOBAL_GET:
                case GLOBAL_SET:
                    ev.visitVariableInsn(opcode, bb.getVarUInt32());
                    break;
                case TABLE_GET:
                case TABLE_SET:
                    ev.visitTableInsn(opcode, bb.getVarUInt32());
                    break;
                case I32_LOAD:
                case I64_LOAD:
                case F32_LOAD:
                case F64_LOAD:
                case I32_LOAD8_S:
                case I32_LOAD8_U:
                case I32_LOAD16_S:
                case I32_LOAD16_U:
                case I64_LOAD8_S:
                case I64_LOAD8_U:
                case I64_LOAD16_S:
                case I64_LOAD16_U:
                case I64_LOAD32_S:
                case I64_LOAD32_U:
                case I32_STORE:
                case I64_STORE:
                case F32_STORE:
                case F64_STORE:
                case I32_STORE8:
                case I32_STORE16:
                case I64_STORE8:
                case I64_STORE16:
                case I64_STORE32:
                    ev.visitMemInsn(opcode, bb.getVarUInt32(), bb.getVarUInt32());
                    break;
                case MEMORY_SIZE:
                case MEMORY_GROW:
                    if (bb.expect() != 0x00) throw new ValidationException("Expected 0x00");
                    ev.visitInsn(opcode);
                    break;
                case I32_CONST:
                    ev.visitConstInsn(bb.getVarSInt32());
                    break;
                case I64_CONST:
                    ev.visitConstInsn(bb.getVarSInt64());
                    break;
                case F32_CONST:
                    ev.visitConstInsn(bb.getFloat32());
                    break;
                case F64_CONST:
                    ev.visitConstInsn(bb.getFloat64());
                    break;
                case INSN_PREFIX:
                    int intOpcode = bb.getVarUInt32();
                    switch (intOpcode) {
                        case TABLE_INIT: {
                            int y = bb.getVarUInt32();
                            int x = bb.getVarUInt32();
                            ev.visitPrefixBinaryTableInsn(intOpcode, x, y);
                            break;
                        }
                        case TABLE_COPY: {
                            int x = bb.getVarUInt32();
                            int y = bb.getVarUInt32();
                            ev.visitPrefixBinaryTableInsn(intOpcode, x, y);
                            break;
                        }
                        case ELEM_DROP:
                        case TABLE_GROW:
                        case TABLE_SIZE:
                        case TABLE_FILL:
                            ev.visitPrefixTableInsn(intOpcode, bb.getVarUInt32());
                            break;
                        case MEMORY_INIT:
                        case DATA_DROP:
                            int index = bb.getVarUInt32();
                            if (intOpcode == MEMORY_INIT) bb.expect();
                            ev.visitIndexedMemInsn(intOpcode, index);
                            break;
                        case MEMORY_COPY:
                            bb.expect();
                            // fall through
                        case MEMORY_FILL:
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
        for (; section == SECTION_CUSTOM; section = bb.get()) {
            int length = bb.getVarUInt32();
            sbb = bb.sectionBuffer(length);

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
