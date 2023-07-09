// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.util;

import java.util.Arrays;

public final class CharTypes
{
    private static final char[] HEX_CHARS;
    private static final byte[] HEX_BYTES;
    static final int[] sInputCodes;
    static final int[] sInputCodesUtf8;
    static final int[] sInputCodesJsNames;
    static final int[] sInputCodesUtf8JsNames;
    static final int[] sInputCodesComment;
    static final int[] sOutputEscapes128;
    static final int[] sHexValues;
    
    public static final int[] getInputCodeLatin1() {
        return CharTypes.sInputCodes;
    }
    
    public static final int[] getInputCodeUtf8() {
        return CharTypes.sInputCodesUtf8;
    }
    
    public static final int[] getInputCodeLatin1JsNames() {
        return CharTypes.sInputCodesJsNames;
    }
    
    public static final int[] getInputCodeUtf8JsNames() {
        return CharTypes.sInputCodesUtf8JsNames;
    }
    
    public static final int[] getInputCodeComment() {
        return CharTypes.sInputCodesComment;
    }
    
    public static final int[] get7BitOutputEscapes() {
        return CharTypes.sOutputEscapes128;
    }
    
    public static int charToHex(final int ch) {
        return (ch > 127) ? -1 : CharTypes.sHexValues[ch];
    }
    
    public static void appendQuoted(final StringBuilder sb, final String content) {
        final int[] escCodes = CharTypes.sOutputEscapes128;
        final int escLen = escCodes.length;
        for (int i = 0, len = content.length(); i < len; ++i) {
            final char c = content.charAt(i);
            if (c >= escLen || escCodes[c] == 0) {
                sb.append(c);
            }
            else {
                sb.append('\\');
                final int escCode = escCodes[c];
                if (escCode < 0) {
                    sb.append('u');
                    sb.append('0');
                    sb.append('0');
                    final int value = -(escCode + 1);
                    sb.append(CharTypes.HEX_CHARS[value >> 4]);
                    sb.append(CharTypes.HEX_CHARS[value & 0xF]);
                }
                else {
                    sb.append((char)escCode);
                }
            }
        }
    }
    
    public static char[] copyHexChars() {
        return CharTypes.HEX_CHARS.clone();
    }
    
    public static byte[] copyHexBytes() {
        return CharTypes.HEX_BYTES.clone();
    }
    
    static {
        HEX_CHARS = "0123456789ABCDEF".toCharArray();
        final int len = CharTypes.HEX_CHARS.length;
        HEX_BYTES = new byte[len];
        for (int i = 0; i < len; ++i) {
            CharTypes.HEX_BYTES[i] = (byte)CharTypes.HEX_CHARS[i];
        }
        int[] table = new int[256];
        for (int i = 0; i < 32; ++i) {
            table[i] = -1;
        }
        table[92] = (table[34] = 1);
        sInputCodes = table;
        table = new int[CharTypes.sInputCodes.length];
        System.arraycopy(CharTypes.sInputCodes, 0, table, 0, CharTypes.sInputCodes.length);
        for (int c = 128; c < 256; ++c) {
            int code;
            if ((c & 0xE0) == 0xC0) {
                code = 2;
            }
            else if ((c & 0xF0) == 0xE0) {
                code = 3;
            }
            else if ((c & 0xF8) == 0xF0) {
                code = 4;
            }
            else {
                code = -1;
            }
            table[c] = code;
        }
        sInputCodesUtf8 = table;
        table = new int[256];
        Arrays.fill(table, -1);
        for (int i = 33; i < 256; ++i) {
            if (Character.isJavaIdentifierPart((char)i)) {
                table[i] = 0;
            }
        }
        table[64] = 0;
        table[42] = (table[35] = 0);
        table[43] = (table[45] = 0);
        sInputCodesJsNames = table;
        table = new int[256];
        System.arraycopy(CharTypes.sInputCodesJsNames, 0, table, 0, CharTypes.sInputCodesJsNames.length);
        Arrays.fill(table, 128, 128, 0);
        sInputCodesUtf8JsNames = table;
        sInputCodesComment = new int[256];
        System.arraycopy(CharTypes.sInputCodesUtf8, 128, CharTypes.sInputCodesComment, 128, 128);
        Arrays.fill(CharTypes.sInputCodesComment, 0, 32, -1);
        CharTypes.sInputCodesComment[9] = 0;
        CharTypes.sInputCodesComment[10] = 10;
        CharTypes.sInputCodesComment[13] = 13;
        CharTypes.sInputCodesComment[42] = 42;
        table = new int[128];
        for (int i = 0; i < 32; ++i) {
            table[i] = -1;
        }
        table[34] = 34;
        table[92] = 92;
        table[8] = 98;
        table[9] = 116;
        table[12] = 102;
        table[10] = 110;
        table[13] = 114;
        sOutputEscapes128 = table;
        Arrays.fill(sHexValues = new int[128], -1);
        for (int j = 0; j < 10; ++j) {
            CharTypes.sHexValues[48 + j] = j;
        }
        for (int j = 0; j < 6; ++j) {
            CharTypes.sHexValues[97 + j] = 10 + j;
            CharTypes.sHexValues[65 + j] = 10 + j;
        }
    }
}
