package io.github.eutro.jwasm;

public interface Opcodes {
    // preamble
    int MAGIC = 0X6D736100; // "\0asm"
    int VERSION = 0x00000001;

    // sections
    byte SECTION_CUSTOM = 0x00;
    byte SECTION_TYPE = 0x01;
    byte SECTION_IMPORT = 0x02;
    byte SECTION_FUNCTION = 0x03;
    byte SECTION_TABLE = 0x04;
    byte SECTION_MEMORY = 0x05;
    byte SECTION_GLOBAL = 0x06;
    byte SECTION_EXPORT = 0x07;
    byte SECTION_START = 0x08;
    byte SECTION_ELEMENT = 0x09;
    byte SECTION_CODE = 0x0A;
    byte SECTION_DATA = 0x0B;
    byte SECTION_DATA_COUNT = 0x0C;

    /// valtype
    // numtypes
    byte I32 = 0x7F;
    byte I64 = 0x7E;
    byte F32 = 0x7D;
    byte F64 = 0x7C;

    // reftypes
    byte FUNCREF = 0x70;
    byte EXTERNREF = 0x6F;

    // block types
    byte EMPTY_TYPE = 0x40;

    // types section
    byte TYPES_FUNCTION = 0x60;

    // imports section
    byte IMPORTS_FUNC = 0x00;
    byte IMPORTS_TABLE = 0x01;
    byte IMPORTS_MEM = 0x02;
    byte IMPORTS_GLOBAL = 0x03;

    // limit constants
    byte LIMIT_NOMAX = 0x00;
    byte LIMIT_WMAX = 0x01;

    // global constants
    byte MUT_CONST = 0x00;
    byte MUT_VAR = 0x01;

    // elems section
    byte ELEMKIND = 0x00;

    int ELEM_PASSIVE_OR_DECLARATIVE = 1;
    int ELEM_TABLE_INDEX = 1 << 1;
    int ELEM_EXPRESSIONS = 1 << 2;

    // data section
    int DATA_PASSIVE = 1;
    int DATA_EXPLICIT = 1 << 1;

    /// instructions
    byte INSN_PREFIX = (byte) 0xFC;
    // control
    byte UNREACHABLE = 0x00;
    byte NOP = 0x01;
    byte BLOCK = 0x02;
    byte LOOP = 0x03;
    byte IF = 0x04;
    byte ELSE = 0x05;
    byte END = 0x0B;
    byte BR = 0x0C;
    byte BR_IF = 0x0D;
    byte BR_TABLE = 0x0E;
    byte RETURN = 0x0F;
    byte CALL = 0x10;
    byte CALL_INDIRECT = 0x11;
    // reference
    byte REF_NULL = (byte) 0xD0;
    byte REF_IS_NULL = (byte) 0xD1;
    byte REF_FUNC = (byte) 0xD2;
    // parametric
    byte DROP = 0x1A;
    byte SELECT = 0x1B;
    byte SELECTT = 0x1C;
    // variable
    byte LOCAL_GET = 0x20;
    byte LOCAL_SET = 0x21;
    byte LOCAL_TEE = 0x22;
    byte GLOBAL_GET = 0x23;
    byte GLOBAL_SET = 0x24;
    // table
    byte TABLE_GET = 0x25;
    byte TABLE_SET = 0x26;
    int TABLE_INIT = 12;
    int ELEM_DROP = 13;
    int TABLE_COPY = 14;
    int TABLE_GROW = 15;
    int TABLE_SIZE = 16;
    int TABLE_FILL = 17;
    // mem
    byte I32_LOAD = 0x28;
    byte I64_LOAD = 0x29;
    byte F32_LOAD = 0x2A;
    byte F64_LOAD = 0x2B;
    byte I32_LOAD8_S = 0x2C;
    byte I32_LOAD8_U = 0x2D;
    byte I32_LOAD16_S = 0x2E;
    byte I32_LOAD16_U = 0x2F;
    byte I64_LOAD8_S = 0x30;
    byte I64_LOAD8_U = 0x31;
    byte I64_LOAD16_S = 0x32;
    byte I64_LOAD16_U = 0x33;
    byte I64_LOAD32_S = 0x34;
    byte I64_LOAD32_U = 0x35;
    byte I32_STORE = 0x36;
    byte I64_STORE = 0x37;
    byte F32_STORE = 0x38;
    byte F64_STORE = 0x39;
    byte I32_STORE8 = 0x3A;
    byte I32_STORE16 = 0x3B;
    byte I64_STORE8 = 0x3C;
    byte I64_STORE16 = 0x3D;
    byte I64_STORE32 = 0x3E;
    byte MEMORY_SIZE = 0x3F;
    byte MEMORY_GROW = 0x40;
    int MEMORY_INIT = 8;
    int DATA_DROP = 9;
    int MEMORY_COPY = 10;
    int MEMORY_FILL = 11;
    // numeric
    byte I32_CONST = 0x41;
    byte I64_CONST = 0x42;
    byte F32_CONST = 0x43;
    byte F64_CONST = 0x44;

    byte I32_EQZ = 0x45;
    byte I32_EQ = 0x46;
    byte I32_NE = 0x47;
    byte I32_LT_S = 0x48;
    byte I32_LT_U = 0x49;
    byte I32_GT_S = 0x4A;
    byte I32_GT_U = 0x4B;
    byte I32_LE_S = 0x4C;
    byte I32_LE_U = 0x4D;
    byte I32_GE_S = 0x4E;
    byte I32_GE_U = 0x4F;

    byte I64_EQZ = 0x50;
    byte I64_EQ = 0x51;
    byte I64_NE = 0x52;
    byte I64_LT_S = 0x53;
    byte I64_LT_U = 0x54;
    byte I64_GT_S = 0x55;
    byte I64_GT_U = 0x56;
    byte I64_LE_S = 0x57;
    byte I64_LE_U = 0x58;
    byte I64_GE_S = 0x59;
    byte I64_GE_U = 0x5A;

    byte F32_EQ = 0x5B;
    byte F32_NE = 0x5C;
    byte F32_LT = 0x5D;
    byte F32_GT = 0x5E;
    byte F32_LE = 0x5F;
    byte F32_GE = 0x60;

    byte F64_EQ = 0x61;
    byte F64_NE = 0x62;
    byte F64_LT = 0x63;
    byte F64_GT = 0x64;
    byte F64_LE = 0x65;
    byte F64_GE = 0x66;

    byte I32_CLZ = 0x67;
    byte I32_CTZ = 0x68;
    byte I32_POPCNT = 0x69;
    byte I32_ADD = 0x6A;
    byte I32_SUB = 0x6B;
    byte I32_MUL = 0x6C;
    byte I32_DIV_S = 0x6D;
    byte I32_DIV_U = 0x6E;
    byte I32_REM_S = 0x6F;
    byte I32_REM_U = 0x70;
    byte I32_AND = 0x71;
    byte I32_OR = 0x72;
    byte I32_XOR = 0x73;
    byte I32_SHL = 0x74;
    byte I32_SHR_S = 0x75;
    byte I32_SHR_U = 0x76;
    byte I32_ROTL = 0x77;
    byte I32_ROTR = 0x78;

    byte I64_CLZ = 0x79;
    byte I64_CTZ = 0x7A;
    byte I64_POPCNT = 0x7B;
    byte I64_ADD = 0x7C;
    byte I64_SUB = 0x7D;
    byte I64_MUL = 0x7E;
    byte I64_DIV_S = 0x7F;
    byte I64_DIV_U = (byte) 0x80;
    byte I64_REM_S = (byte) 0x81;
    byte I64_REM_U = (byte) 0x82;
    byte I64_AND = (byte) 0x83;
    byte I64_OR = (byte) 0x84;
    byte I64_XOR = (byte) 0x85;
    byte I64_SHL = (byte) 0x86;
    byte I64_SHR_S = (byte) 0x87;
    byte I64_SHR_U = (byte) 0x88;
    byte I64_ROTL = (byte) 0x89;
    byte I64_ROTR = (byte) 0x8A;

    byte F32_ABS = (byte) 0x8B;
    byte F32_NEG = (byte) 0x8C;
    byte F32_CEIL = (byte) 0x8D;
    byte F32_FLOOR = (byte) 0x8E;
    byte F32_TRUNC = (byte) 0x8F;
    byte F32_NEAREST = (byte) 0x90;
    byte F32_SQRT = (byte) 0x91;
    byte F32_ADD = (byte) 0x92;
    byte F32_SUB = (byte) 0x93;
    byte F32_MUL = (byte) 0x94;
    byte F32_DIV = (byte) 0x95;
    byte F32_MIN = (byte) 0x96;
    byte F32_MAX = (byte) 0x97;
    byte F32_COPYSIGN = (byte) 0x98;

    byte F64_ABS = (byte) 0x99;
    byte F64_NEG = (byte) 0x9A;
    byte F64_CEIL = (byte) 0x9B;
    byte F64_FLOOR = (byte) 0x9C;
    byte F64_TRUNC = (byte) 0x9D;
    byte F64_NEAREST = (byte) 0x9E;
    byte F64_SQRT = (byte) 0x9F;
    byte F64_ADD = (byte) 0xA0;
    byte F64_SUB = (byte) 0xA1;
    byte F64_MUL = (byte) 0xA2;
    byte F64_DIV = (byte) 0xA3;
    byte F64_MIN = (byte) 0xA4;
    byte F64_MAX = (byte) 0xA5;
    byte F64_COPYSIGN = (byte) 0xA6;

    byte I32_WRAP_I64 = (byte) 0xA7;
    byte I32_TRUNC_F32_S = (byte) 0xA8;
    byte I32_TRUNC_F32_U = (byte) 0xA9;
    byte I32_TRUNC_F64_S = (byte) 0xAA;
    byte I32_TRUNC_F64_U = (byte) 0xAB;
    byte I64_EXTEND_I32_S = (byte) 0xAC;
    byte I64_EXTEND_I32_U = (byte) 0xAD;
    byte I64_TRUNC_F32_S = (byte) 0xAE;
    byte I64_TRUNC_F32_U = (byte) 0xAF;
    byte I64_TRUNC_F64_S = (byte) 0xB0;
    byte I64_TRUNC_F64_U = (byte) 0xB1;
    byte F32_CONVERT_I32_S = (byte) 0xB2;
    byte F32_CONVERT_I32_U = (byte) 0xB3;
    byte F32_CONVERT_I64_S = (byte) 0xB4;
    byte F32_CONVERT_I64_U = (byte) 0xB5;
    byte F32_DEMOTE_F64 = (byte) 0xB6;
    byte F64_CONVERT_I32_S = (byte) 0xB7;
    byte F64_CONVERT_I32_U = (byte) 0xB8;
    byte F64_CONVERT_I64_S = (byte) 0xB9;
    byte F64_CONVERT_I64_U = (byte) 0xBA;
    byte F64_PROMOTE_F32 = (byte) 0xBB;
    byte I32_REINTERPRET_F32 = (byte) 0xBC;
    byte I64_REINTERPRET_F64 = (byte) 0xBD;
    byte F32_REINTERPRET_I32 = (byte) 0xBE;
    byte F64_REINTERPRET_I64 = (byte) 0xBF;

    byte I32_EXTEND8_S = (byte) 0xC0;
    byte I32_EXTEND16_S = (byte) 0xC1;
    byte I64_EXTEND8_S = (byte) 0xC2;
    byte I64_EXTEND16_S = (byte) 0xC3;
    byte I64_EXTEND32_S = (byte) 0xC4;

    int I32_TRUNC_SAT_F32_S = 0;
    int I32_TRUNC_SAT_F32_U = 1;
    int I32_TRUNC_SAT_F64_S = 2;
    int I32_TRUNC_SAT_F64_U = 3;
    int I64_TRUNC_SAT_F32_S = 4;
    int I64_TRUNC_SAT_F32_U = 5;
    int I64_TRUNC_SAT_F64_S = 6;
    int I64_TRUNC_SAT_F64_U = 7;
}
