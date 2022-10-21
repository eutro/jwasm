package io.github.eutro.jwasm;

/**
 * A list of WebAssembly constants and opcodes.
 */
public final class Opcodes {
    // region Preamble
    public static final int MAGIC = 0x6D736100; // "\0asm"
    public static final int VERSION = 0x00000001;
    // endregion

    // region Sections
    public static final byte SECTION_CUSTOM = 0x00;
    public static final byte SECTION_TYPE = 0x01;
    public static final byte SECTION_IMPORT = 0x02;
    public static final byte SECTION_FUNCTION = 0x03;
    public static final byte SECTION_TABLE = 0x04;
    public static final byte SECTION_MEMORY = 0x05;
    public static final byte SECTION_GLOBAL = 0x06;
    public static final byte SECTION_EXPORT = 0x07;
    public static final byte SECTION_START = 0x08;
    public static final byte SECTION_ELEMENT = 0x09;
    public static final byte SECTION_CODE = 0x0A;
    public static final byte SECTION_DATA = 0x0B;
    public static final byte SECTION_DATA_COUNT = 0x0C;
    // endregion

    // region blocktype
    /// region valtype
    // region numtype
    public static final byte I32 = 0x7F;
    public static final byte I64 = 0x7E;
    public static final byte F32 = 0x7D;
    public static final byte F64 = 0x7C;
    // endregion

    // region vectype
    public static final byte V128 = 0x7B;
    // endregion

    // region reftype
    public static final byte FUNCREF = 0x70;
    public static final byte EXTERNREF = 0x6F;
    // endregion
    /// endregion

    public static final byte EMPTY_TYPE = 0x40;
    // endregion

    // region Types Section
    public static final byte TYPES_FUNCTION = 0x60;
    // endregion

    // region Imports Section
    public static final byte IMPORTS_FUNC = 0x00;
    public static final byte IMPORTS_TABLE = 0x01;
    public static final byte IMPORTS_MEM = 0x02;
    public static final byte IMPORTS_GLOBAL = 0x03;
    // endregion

    // region Exports Section
    public static final byte EXPORTS_FUNC = 0x00;
    public static final byte EXPORTS_TABLE = 0x01;
    public static final byte EXPORTS_MEM = 0x02;
    public static final byte EXPORTS_GLOBAL = 0x03;
    // endregion

    // region Limit Constants
    public static final byte LIMIT_NOMAX = 0x00;
    public static final byte LIMIT_WMAX = 0x01;
    public static final int PAGE_SIZE = 65536;
    // endregion

    // region Global Type Constants
    public static final byte MUT_CONST = 0x00;
    public static final byte MUT_VAR = 0x01;
    // endregion

    // region Element Segments Section
    public static final byte ELEMKIND = 0x00;

    // region Element Bit Mask
    public static final int ELEM_EXPRESSIONS = 1 << 2;
    public static final int ELEM_PASSIVE_OR_DECLARATIVE = 1;
    public static final int ELEM_TABLE_INDEX = 1 << 1;
    // endregion
    // endregion

    // region Data Section
    public static final int DATA_PASSIVE = 1;
    public static final int DATA_EXPLICIT = 1 << 1;
    // endregion

    /// region Instructions
    public static final byte INSN_PREFIX = (byte) 0xFC;
    public static final byte VECTOR_PREFIX = (byte) 0xFD;
    // region Control
    public static final byte UNREACHABLE = 0x00;
    public static final byte NOP = 0x01;
    public static final byte BLOCK = 0x02;
    public static final byte LOOP = 0x03;
    public static final byte IF = 0x04;
    public static final byte ELSE = 0x05;
    public static final byte END = 0x0B;
    public static final byte BR = 0x0C;
    public static final byte BR_IF = 0x0D;
    public static final byte BR_TABLE = 0x0E;
    public static final byte RETURN = 0x0F;
    public static final byte CALL = 0x10;
    public static final byte CALL_INDIRECT = 0x11;
    // endregion
    // region Reference
    public static final byte REF_NULL = (byte) 0xD0;
    public static final byte REF_IS_NULL = (byte) 0xD1;
    public static final byte REF_FUNC = (byte) 0xD2;
    // endregion
    // region Parametric
    public static final byte DROP = 0x1A;
    public static final byte SELECT = 0x1B;
    public static final byte SELECTT = 0x1C;
    // endregion
    // region Variable
    public static final byte LOCAL_GET = 0x20;
    public static final byte LOCAL_SET = 0x21;
    public static final byte LOCAL_TEE = 0x22;
    public static final byte GLOBAL_GET = 0x23;
    public static final byte GLOBAL_SET = 0x24;
    // endregion
    // region Table
    public static final byte TABLE_GET = 0x25;
    public static final byte TABLE_SET = 0x26;
    public static final int TABLE_INIT = 12;
    public static final int ELEM_DROP = 13;
    public static final int TABLE_COPY = 14;
    public static final int TABLE_GROW = 15;
    public static final int TABLE_SIZE = 16;
    public static final int TABLE_FILL = 17;
    // endregion
    // region Memory
    public static final byte I32_LOAD = 0x28;
    public static final byte I64_LOAD = 0x29;
    public static final byte F32_LOAD = 0x2A;
    public static final byte F64_LOAD = 0x2B;
    public static final byte I32_LOAD8_S = 0x2C;
    public static final byte I32_LOAD8_U = 0x2D;
    public static final byte I32_LOAD16_S = 0x2E;
    public static final byte I32_LOAD16_U = 0x2F;
    public static final byte I64_LOAD8_S = 0x30;
    public static final byte I64_LOAD8_U = 0x31;
    public static final byte I64_LOAD16_S = 0x32;
    public static final byte I64_LOAD16_U = 0x33;
    public static final byte I64_LOAD32_S = 0x34;
    public static final byte I64_LOAD32_U = 0x35;
    public static final byte I32_STORE = 0x36;
    public static final byte I64_STORE = 0x37;
    public static final byte F32_STORE = 0x38;
    public static final byte F64_STORE = 0x39;
    public static final byte I32_STORE8 = 0x3A;
    public static final byte I32_STORE16 = 0x3B;
    public static final byte I64_STORE8 = 0x3C;
    public static final byte I64_STORE16 = 0x3D;
    public static final byte I64_STORE32 = 0x3E;
    public static final byte MEMORY_SIZE = 0x3F;
    public static final byte MEMORY_GROW = 0x40;
    public static final int MEMORY_INIT = 8;
    public static final int DATA_DROP = 9;
    public static final int MEMORY_COPY = 10;
    public static final int MEMORY_FILL = 11;
    // endregion
    // region Numeric
    // region Const
    public static final byte I32_CONST = 0x41;
    public static final byte I64_CONST = 0x42;
    public static final byte F32_CONST = 0x43;
    public static final byte F64_CONST = 0x44;
    // endregion
    // region Comparisons
    // region i32
    public static final byte I32_EQZ = 0x45;
    public static final byte I32_EQ = 0x46;
    public static final byte I32_NE = 0x47;
    public static final byte I32_LT_S = 0x48;
    public static final byte I32_LT_U = 0x49;
    public static final byte I32_GT_S = 0x4A;
    public static final byte I32_GT_U = 0x4B;
    public static final byte I32_LE_S = 0x4C;
    public static final byte I32_LE_U = 0x4D;
    public static final byte I32_GE_S = 0x4E;
    public static final byte I32_GE_U = 0x4F;
    // endregion
    // region i64
    public static final byte I64_EQZ = 0x50;
    public static final byte I64_EQ = 0x51;
    public static final byte I64_NE = 0x52;
    public static final byte I64_LT_S = 0x53;
    public static final byte I64_LT_U = 0x54;
    public static final byte I64_GT_S = 0x55;
    public static final byte I64_GT_U = 0x56;
    public static final byte I64_LE_S = 0x57;
    public static final byte I64_LE_U = 0x58;
    public static final byte I64_GE_S = 0x59;
    public static final byte I64_GE_U = 0x5A;
    // endregion
    // region f32
    public static final byte F32_EQ = 0x5B;
    public static final byte F32_NE = 0x5C;
    public static final byte F32_LT = 0x5D;
    public static final byte F32_GT = 0x5E;
    public static final byte F32_LE = 0x5F;
    public static final byte F32_GE = 0x60;
    // endregion
    // region f64
    public static final byte F64_EQ = 0x61;
    public static final byte F64_NE = 0x62;
    public static final byte F64_LT = 0x63;
    public static final byte F64_GT = 0x64;
    public static final byte F64_LE = 0x65;
    public static final byte F64_GE = 0x66;
    // endregion
    // endregion
    // region Mathematical
    // region i32
    public static final byte I32_CLZ = 0x67;
    public static final byte I32_CTZ = 0x68;
    public static final byte I32_POPCNT = 0x69;
    public static final byte I32_ADD = 0x6A;
    public static final byte I32_SUB = 0x6B;
    public static final byte I32_MUL = 0x6C;
    public static final byte I32_DIV_S = 0x6D;
    public static final byte I32_DIV_U = 0x6E;
    public static final byte I32_REM_S = 0x6F;
    public static final byte I32_REM_U = 0x70;
    public static final byte I32_AND = 0x71;
    public static final byte I32_OR = 0x72;
    public static final byte I32_XOR = 0x73;
    public static final byte I32_SHL = 0x74;
    public static final byte I32_SHR_S = 0x75;
    public static final byte I32_SHR_U = 0x76;
    public static final byte I32_ROTL = 0x77;
    public static final byte I32_ROTR = 0x78;
    // endregion
    // region i64
    public static final byte I64_CLZ = 0x79;
    public static final byte I64_CTZ = 0x7A;
    public static final byte I64_POPCNT = 0x7B;
    public static final byte I64_ADD = 0x7C;
    public static final byte I64_SUB = 0x7D;
    public static final byte I64_MUL = 0x7E;
    public static final byte I64_DIV_S = 0x7F;
    public static final byte I64_DIV_U = (byte) 0x80;
    public static final byte I64_REM_S = (byte) 0x81;
    public static final byte I64_REM_U = (byte) 0x82;
    public static final byte I64_AND = (byte) 0x83;
    public static final byte I64_OR = (byte) 0x84;
    public static final byte I64_XOR = (byte) 0x85;
    public static final byte I64_SHL = (byte) 0x86;
    public static final byte I64_SHR_S = (byte) 0x87;
    public static final byte I64_SHR_U = (byte) 0x88;
    public static final byte I64_ROTL = (byte) 0x89;
    public static final byte I64_ROTR = (byte) 0x8A;
    // endregion
    // region f32
    public static final byte F32_ABS = (byte) 0x8B;
    public static final byte F32_NEG = (byte) 0x8C;
    public static final byte F32_CEIL = (byte) 0x8D;
    public static final byte F32_FLOOR = (byte) 0x8E;
    public static final byte F32_TRUNC = (byte) 0x8F;
    public static final byte F32_NEAREST = (byte) 0x90;
    public static final byte F32_SQRT = (byte) 0x91;
    public static final byte F32_ADD = (byte) 0x92;
    public static final byte F32_SUB = (byte) 0x93;
    public static final byte F32_MUL = (byte) 0x94;
    public static final byte F32_DIV = (byte) 0x95;
    public static final byte F32_MIN = (byte) 0x96;
    public static final byte F32_MAX = (byte) 0x97;
    public static final byte F32_COPYSIGN = (byte) 0x98;
    // endregion
    // region f64
    public static final byte F64_ABS = (byte) 0x99;
    public static final byte F64_NEG = (byte) 0x9A;
    public static final byte F64_CEIL = (byte) 0x9B;
    public static final byte F64_FLOOR = (byte) 0x9C;
    public static final byte F64_TRUNC = (byte) 0x9D;
    public static final byte F64_NEAREST = (byte) 0x9E;
    public static final byte F64_SQRT = (byte) 0x9F;
    public static final byte F64_ADD = (byte) 0xA0;
    public static final byte F64_SUB = (byte) 0xA1;
    public static final byte F64_MUL = (byte) 0xA2;
    public static final byte F64_DIV = (byte) 0xA3;
    public static final byte F64_MIN = (byte) 0xA4;
    public static final byte F64_MAX = (byte) 0xA5;
    public static final byte F64_COPYSIGN = (byte) 0xA6;
    // endregion
    // endregion
    // region Conversions
    public static final byte I32_WRAP_I64 = (byte) 0xA7;
    public static final byte I32_TRUNC_F32_S = (byte) 0xA8;
    public static final byte I32_TRUNC_F32_U = (byte) 0xA9;
    public static final byte I32_TRUNC_F64_S = (byte) 0xAA;
    public static final byte I32_TRUNC_F64_U = (byte) 0xAB;
    public static final byte I64_EXTEND_I32_S = (byte) 0xAC;
    public static final byte I64_EXTEND_I32_U = (byte) 0xAD;
    public static final byte I64_TRUNC_F32_S = (byte) 0xAE;
    public static final byte I64_TRUNC_F32_U = (byte) 0xAF;
    public static final byte I64_TRUNC_F64_S = (byte) 0xB0;
    public static final byte I64_TRUNC_F64_U = (byte) 0xB1;
    public static final byte F32_CONVERT_I32_S = (byte) 0xB2;
    public static final byte F32_CONVERT_I32_U = (byte) 0xB3;
    public static final byte F32_CONVERT_I64_S = (byte) 0xB4;
    public static final byte F32_CONVERT_I64_U = (byte) 0xB5;
    public static final byte F32_DEMOTE_F64 = (byte) 0xB6;
    public static final byte F64_CONVERT_I32_S = (byte) 0xB7;
    public static final byte F64_CONVERT_I32_U = (byte) 0xB8;
    public static final byte F64_CONVERT_I64_S = (byte) 0xB9;
    public static final byte F64_CONVERT_I64_U = (byte) 0xBA;
    public static final byte F64_PROMOTE_F32 = (byte) 0xBB;
    public static final byte I32_REINTERPRET_F32 = (byte) 0xBC;
    public static final byte I64_REINTERPRET_F64 = (byte) 0xBD;
    public static final byte F32_REINTERPRET_I32 = (byte) 0xBE;
    public static final byte F64_REINTERPRET_I64 = (byte) 0xBF;
    // endregion
    // region Extension
    public static final byte I32_EXTEND8_S = (byte) 0xC0;
    public static final byte I32_EXTEND16_S = (byte) 0xC1;
    public static final byte I64_EXTEND8_S = (byte) 0xC2;
    public static final byte I64_EXTEND16_S = (byte) 0xC3;
    public static final byte I64_EXTEND32_S = (byte) 0xC4;
    // endregion
    // region Saturating Truncation
    public static final int I32_TRUNC_SAT_F32_S = 0;
    public static final int I32_TRUNC_SAT_F32_U = 1;
    public static final int I32_TRUNC_SAT_F64_S = 2;
    public static final int I32_TRUNC_SAT_F64_U = 3;
    public static final int I64_TRUNC_SAT_F32_S = 4;
    public static final int I64_TRUNC_SAT_F32_U = 5;
    public static final int I64_TRUNC_SAT_F64_S = 6;
    public static final int I64_TRUNC_SAT_F64_U = 7;
    // endregion
    // endregion
    // region Vector
    // region Memory
    public static final int V128_LOAD = 0;
    public static final int V128_LOAD8X8_S = 1;
    public static final int V128_LOAD8X8_U = 2;
    public static final int V128_LOAD16X4_S = 3;
    public static final int V128_LOAD16X4_U = 4;
    public static final int V128_LOAD32X2_S = 5;
    public static final int V128_LOAD32X2_U = 6;
    public static final int V128_LOAD8_SPLAT = 7;
    public static final int V128_LOAD16_SPLAT = 8;
    public static final int V128_LOAD32_SPLAT = 9;
    public static final int V128_LOAD64_SPLAT = 10;
    public static final int V128_LOAD32_ZERO = 92;
    public static final int V128_LOAD64_ZERO = 93;
    public static final int V128_STORE = 11;
    public static final int V128_LOAD8_LANE = 84;
    public static final int V128_LOAD16_LANE = 85;
    public static final int V128_LOAD32_LANE = 86;
    public static final int V128_LOAD64_LANE = 87;
    public static final int V128_STORE8_LANE = 88;
    public static final int V128_STORE16_LANE = 89;
    public static final int V128_STORE32_LANE = 90;
    public static final int V128_STORE64_LANE = 91;
    // endregion
    // region Const and Shuffle
    public static final int V128_CONST = 12;
    public static final int I8X16_SHUFFLE = 13;
    // endregion
    // region Lane
    public static final int I8X16_EXTRACT_LANE_S = 21;
    public static final int I8X16_EXTRACT_LANE_U = 22;
    public static final int I8X16_REPLACE_LANE = 23;
    public static final int I16X8_EXTRACT_LANE_S = 24;
    public static final int I16X8_EXTRACT_LANE_U = 25;
    public static final int I16X8_REPLACE_LANE = 26;
    public static final int I32X4_EXTRACT_LANE = 27;
    public static final int I32X4_REPLACE_LANE = 28;
    public static final int I64X2_EXTRACT_LANE = 29;
    public static final int I64X2_REPLACE_LANE = 30;
    public static final int F32X4_EXTRACT_LANE = 31;
    public static final int F32X4_REPLACE_LANE = 32;
    public static final int F64X2_EXTRACT_LANE = 33;
    public static final int F64X2_REPLACE_LANE = 34;
    // endregion
    // region Plain
    public static final int I8X16_SWIZZLE = 14;
    public static final int I8X16_SPLAT = 15;
    public static final int I16X8_SPLAT = 16;
    public static final int I32X4_SPLAT = 17;
    public static final int I64X2_SPLAT = 18;
    public static final int F32X4_SPLAT = 19;
    public static final int F64X2_SPLAT = 20;
    public static final int I8X16_EQ = 35;
    public static final int I8X16_NE = 36;
    public static final int I8X16_LT_S = 37;
    public static final int I8X16_LT_U = 38;
    public static final int I8X16_GT_S = 39;
    public static final int I8X16_GT_U = 40;
    public static final int I8X16_LE_S = 41;
    public static final int I8X16_LE_U = 42;
    public static final int I8X16_GE_S = 43;
    public static final int I8X16_GE_U = 44;
    public static final int I16X8_EQ = 45;
    public static final int I16X8_NE = 46;
    public static final int I16X8_LT_S = 47;
    public static final int I16X8_LT_U = 48;
    public static final int I16X8_GT_S = 49;
    public static final int I16X8_GT_U = 50;
    public static final int I16X8_LE_S = 51;
    public static final int I16X8_LE_U = 52;
    public static final int I16X8_GE_S = 53;
    public static final int I16X8_GE_U = 54;
    public static final int I32X4_EQ = 55;
    public static final int I32X4_NE = 56;
    public static final int I32X4_LT_S = 57;
    public static final int I32X4_LT_U = 58;
    public static final int I32X4_GT_S = 59;
    public static final int I32X4_GT_U = 60;
    public static final int I32X4_LE_S = 61;
    public static final int I32X4_LE_U = 62;
    public static final int I32X4_GE_S = 63;
    public static final int I32X4_GE_U = 64;
    public static final int I64X2_EQ = 214;
    public static final int I64X2_NE = 215;
    public static final int I64X2_LT_S = 216;
    public static final int I64X2_GT_S = 217;
    public static final int I64X2_LE_S = 218;
    public static final int I64X2_GE_S = 219;
    public static final int F32X4_EQ = 65;
    public static final int F32X4_NE = 66;
    public static final int F32X4_LT = 67;
    public static final int F32X4_GT = 68;
    public static final int F32X4_LE = 69;
    public static final int F32X4_GE = 70;
    public static final int F64X2_EQ = 71;
    public static final int F64X2_NE = 72;
    public static final int F64X2_LT = 73;
    public static final int F64X2_GT = 74;
    public static final int F64X2_LE = 75;
    public static final int F64X2_GE = 76;
    public static final int V128_NOT = 77;
    public static final int V128_AND = 78;
    public static final int V128_ANDNOT = 79;
    public static final int V128_OR = 80;
    public static final int V128_XOR = 81;
    public static final int V128_BITSELECT = 82;
    public static final int V128_ANY_TRUE = 83;
    public static final int I8X16_ABS = 96;
    public static final int I8X16_NEG = 97;
    public static final int I8X16_POPCNT = 98;
    public static final int I8X16_ALL_TRUE = 99;
    public static final int I8X16_BITMASK = 100;
    public static final int I8X16_NARROW_I16X8_S = 101;
    public static final int I8X16_NARROW_I16X8_U = 102;
    public static final int I8X16_SHL = 107;
    public static final int I8X16_SHR_S = 108;
    public static final int I8X16_SHR_U = 109;
    public static final int I8X16_ADD = 110;
    public static final int I8X16_ADD_SAT_S = 111;
    public static final int I8X16_ADD_SAT_U = 112;
    public static final int I8X16_SUB = 113;
    public static final int I8X16_SUB_SAT_S = 114;
    public static final int I8X16_SUB_SAT_U = 115;
    public static final int I8X16_MIN_S = 118;
    public static final int I8X16_MIN_U = 119;
    public static final int I8X16_MAX_S = 120;
    public static final int I8X16_MAX_U = 121;
    public static final int I8X16_AVGR_U = 123;
    public static final int I16X8_EXTADD_PAIRWISE_I8X16_S = 124;
    public static final int I16X8_EXTADD_PAIRWISE_I8X16_U = 125;
    public static final int I16X8_ABS = 128;
    public static final int I16X8_NEG = 129;
    public static final int I16X8_Q15MULR_SAT_S = 130;
    public static final int I16X8_ALL_TRUE = 131;
    public static final int I16X8_BITMASK = 132;
    public static final int I16X8_NARROW_I32X4_S = 133;
    public static final int I16X8_NARROW_I32X4_U = 134;
    public static final int I16X8_EXTEND_LOW_I8X16_S = 135;
    public static final int I16X8_EXTEND_HIGH_I8X16_S = 136;
    public static final int I16X8_EXTEND_LOW_I8X16_U = 137;
    public static final int I16X8_EXTEND_HIGH_I8X16_U = 138;
    public static final int I16X8_SHL = 139;
    public static final int I16X8_SHR_S = 140;
    public static final int I16X8_SHR_U = 141;
    public static final int I16X8_ADD = 142;
    public static final int I16X8_ADD_SAT_S = 143;
    public static final int I16X8_ADD_SAT_U = 144;
    public static final int I16X8_SUB = 145;
    public static final int I16X8_SUB_SAT_S = 146;
    public static final int I16X8_SUB_SAT_U = 147;
    public static final int I16X8_MUL = 149;
    public static final int I16X8_MIN_S = 150;
    public static final int I16X8_MIN_U = 151;
    public static final int I16X8_MAX_S = 152;
    public static final int I16X8_MAX_U = 153;
    public static final int I16X8_AVGR_U = 155;
    public static final int I16X8_EXTMUL_LOW_I8X16_S = 156;
    public static final int I16X8_EXTMUL_HIGH_I8X16_S = 157;
    public static final int I16X8_EXTMUL_LOW_I8X16_U = 158;
    public static final int I16X8_EXTMUL_HIGH_I8X16_U = 159;
    public static final int I32X4_EXTADD_PAIRWISE_I16X8_S = 126;
    public static final int I32X4_EXTADD_PAIRWISE_I16X8_U = 127;
    public static final int I32X4_ABS = 160;
    public static final int I32X4_NEG = 161;
    public static final int I32X4_ALL_TRUE = 163;
    public static final int I32X4_BITMASK = 164;
    public static final int I32X4_EXTEND_LOW_I16X8_S = 167;
    public static final int I32X4_EXTEND_HIGH_I16X8_S = 168;
    public static final int I32X4_EXTEND_LOW_I16X8_U = 169;
    public static final int I32X4_EXTEND_HIGH_I16X8_U = 170;
    public static final int I32X4_SHL = 171;
    public static final int I32X4_SHR_S = 172;
    public static final int I32X4_SHR_U = 173;
    public static final int I32X4_ADD = 174;
    public static final int I32X4_SUB = 177;
    public static final int I32X4_MUL = 181;
    public static final int I32X4_MIN_S = 182;
    public static final int I32X4_MIN_U = 183;
    public static final int I32X4_MAX_S = 184;
    public static final int I32X4_MAX_U = 185;
    public static final int I32X4_DOT_I16X8_S = 186;
    public static final int I32X4_EXTMUL_LOW_I16X8_S = 188;
    public static final int I32X4_EXTMUL_HIGH_I16X8_S = 189;
    public static final int I32X4_EXTMUL_LOW_I16X8_U = 190;
    public static final int I32X4_EXTMUL_HIGH_I16X8_U = 191;
    public static final int I64X2_ABS = 192;
    public static final int I64X2_NEG = 193;
    public static final int I64X2_ALL_TRUE = 195;
    public static final int I64X2_BITMASK = 196;
    public static final int I64X2_EXTEND_LOW_I32X4_S = 199;
    public static final int I64X2_EXTEND_HIGH_I32X4_S = 200;
    public static final int I64X2_EXTEND_LOW_I32X4_U = 201;
    public static final int I64X2_EXTEND_HIGH_I32X4_U = 202;
    public static final int I64X2_SHL = 203;
    public static final int I64X2_SHR_S = 204;
    public static final int I64X2_SHR_U = 205;
    public static final int I64X2_ADD = 206;
    public static final int I64X2_SUB = 209;
    public static final int I64X2_MUL = 213;
    public static final int I64X2_EXTMUL_LOW_I32X4_S = 220;
    public static final int I64X2_EXTMUL_HIGH_I32X4_S = 221;
    public static final int I64X2_EXTMUL_LOW_I32X4_U = 222;
    public static final int I64X2_EXTMUL_HIGH_I32X4_U = 223;
    public static final int F32X4_CEIL = 103;
    public static final int F32X4_FLOOR = 104;
    public static final int F32X4_TRUNC = 105;
    public static final int F32X4_NEAREST = 106;
    public static final int F32X4_ABS = 224;
    public static final int F32X4_NEG = 225;
    public static final int F32X4_SQRT = 227;
    public static final int F32X4_ADD = 228;
    public static final int F32X4_SUB = 229;
    public static final int F32X4_MUL = 230;
    public static final int F32X4_DIV = 231;
    public static final int F32X4_MIN = 232;
    public static final int F32X4_MAX = 233;
    public static final int F32X4_PMIN = 234;
    public static final int F32X4_PMAX = 235;
    public static final int F64X2_CEIL = 116;
    public static final int F64X2_FLOOR = 117;
    public static final int F64X2_TRUNC = 122;
    public static final int F64X2_NEAREST = 148;
    public static final int F64X2_ABS = 236;
    public static final int F64X2_NEG = 237;
    public static final int F64X2_SQRT = 239;
    public static final int F64X2_ADD = 240;
    public static final int F64X2_SUB = 241;
    public static final int F64X2_MUL = 242;
    public static final int F64X2_DIV = 243;
    public static final int F64X2_MIN = 244;
    public static final int F64X2_MAX = 245;
    public static final int F64X2_PMIN = 246;
    public static final int F64X2_PMAX = 247;
    public static final int I32X4_TRUNC_SAT_F32X4_S = 248;
    public static final int I32X4_TRUNC_SAT_F32X4_U = 249;
    public static final int F32X4_CONVERT_I32X4_S = 250;
    public static final int F32X4_CONVERT_I32X4_U = 251;
    public static final int I32X4_TRUNC_SAT_F64X2_S_ZERO = 252;
    public static final int I32X4_TRUNC_SAT_F64X2_U_ZERO = 253;
    public static final int F64X2_CONVERT_LOW_I32X4_S = 254;
    public static final int F64X2_CONVERT_LOW_I32X4_U = 255;
    public static final int F32X4_DEMOTE_F64X2_ZERO = 94;
    public static final int F64X2_PROMOTE_LOW_F32X4 = 95;
    // endregion
    // endregion
    // endregion
}
