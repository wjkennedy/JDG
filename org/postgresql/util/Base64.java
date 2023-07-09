// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.io.UnsupportedEncodingException;

public class Base64
{
    public static final int NO_OPTIONS = 0;
    public static final int ENCODE = 1;
    public static final int DECODE = 0;
    public static final int DONT_BREAK_LINES = 8;
    private static final int MAX_LINE_LENGTH = 76;
    private static final byte EQUALS_SIGN = 61;
    private static final byte NEW_LINE = 10;
    private static final String PREFERRED_ENCODING = "UTF-8";
    private static final byte[] ALPHABET;
    private static final byte[] _NATIVE_ALPHABET;
    private static final byte[] DECODABET;
    private static final byte WHITE_SPACE_ENC = -5;
    private static final byte EQUALS_SIGN_ENC = -1;
    
    private Base64() {
    }
    
    private static byte[] encode3to4(final byte[] source, final int srcOffset, final int numSigBytes, final byte[] destination, final int destOffset) {
        final int inBuff = ((numSigBytes > 0) ? (source[srcOffset] << 24 >>> 8) : 0) | ((numSigBytes > 1) ? (source[srcOffset + 1] << 24 >>> 16) : 0) | ((numSigBytes > 2) ? (source[srcOffset + 2] << 24 >>> 24) : 0);
        switch (numSigBytes) {
            case 3: {
                destination[destOffset] = Base64.ALPHABET[inBuff >>> 18];
                destination[destOffset + 1] = Base64.ALPHABET[inBuff >>> 12 & 0x3F];
                destination[destOffset + 2] = Base64.ALPHABET[inBuff >>> 6 & 0x3F];
                destination[destOffset + 3] = Base64.ALPHABET[inBuff & 0x3F];
                return destination;
            }
            case 2: {
                destination[destOffset] = Base64.ALPHABET[inBuff >>> 18];
                destination[destOffset + 1] = Base64.ALPHABET[inBuff >>> 12 & 0x3F];
                destination[destOffset + 2] = Base64.ALPHABET[inBuff >>> 6 & 0x3F];
                destination[destOffset + 3] = 61;
                return destination;
            }
            case 1: {
                destination[destOffset] = Base64.ALPHABET[inBuff >>> 18];
                destination[destOffset + 1] = Base64.ALPHABET[inBuff >>> 12 & 0x3F];
                destination[destOffset + 3] = (destination[destOffset + 2] = 61);
                return destination;
            }
            default: {
                return destination;
            }
        }
    }
    
    public static String encodeBytes(final byte[] source) {
        return encodeBytes(source, 0, source.length, 0);
    }
    
    public static String encodeBytes(final byte[] source, final int options) {
        return encodeBytes(source, 0, source.length, options);
    }
    
    public static String encodeBytes(final byte[] source, final int off, final int len) {
        return encodeBytes(source, off, len, 0);
    }
    
    public static String encodeBytes(final byte[] source, final int off, final int len, final int options) {
        final int dontBreakLines = options & 0x8;
        final boolean breakLines = dontBreakLines == 0;
        final int len2 = len * 4 / 3;
        final byte[] outBuff = new byte[len2 + ((len % 3 > 0) ? 4 : 0) + (breakLines ? (len2 / 76) : 0)];
        int d = 0;
        int e = 0;
        final int len3 = len - 2;
        int lineLength = 0;
        while (d < len3) {
            encode3to4(source, d + off, 3, outBuff, e);
            lineLength += 4;
            if (breakLines && lineLength == 76) {
                outBuff[e + 4] = 10;
                ++e;
                lineLength = 0;
            }
            d += 3;
            e += 4;
        }
        if (d < len) {
            encode3to4(source, d + off, len - d, outBuff, e);
            e += 4;
        }
        try {
            return new String(outBuff, 0, e, "UTF-8");
        }
        catch (final UnsupportedEncodingException uue) {
            return new String(outBuff, 0, e);
        }
    }
    
    private static int decode4to3(final byte[] source, final int srcOffset, final byte[] destination, final int destOffset) {
        if (source[srcOffset + 2] == 61) {
            final int outBuff = (Base64.DECODABET[source[srcOffset]] & 0xFF) << 18 | (Base64.DECODABET[source[srcOffset + 1]] & 0xFF) << 12;
            destination[destOffset] = (byte)(outBuff >>> 16);
            return 1;
        }
        if (source[srcOffset + 3] == 61) {
            final int outBuff = (Base64.DECODABET[source[srcOffset]] & 0xFF) << 18 | (Base64.DECODABET[source[srcOffset + 1]] & 0xFF) << 12 | (Base64.DECODABET[source[srcOffset + 2]] & 0xFF) << 6;
            destination[destOffset] = (byte)(outBuff >>> 16);
            destination[destOffset + 1] = (byte)(outBuff >>> 8);
            return 2;
        }
        try {
            final int outBuff = (Base64.DECODABET[source[srcOffset]] & 0xFF) << 18 | (Base64.DECODABET[source[srcOffset + 1]] & 0xFF) << 12 | (Base64.DECODABET[source[srcOffset + 2]] & 0xFF) << 6 | (Base64.DECODABET[source[srcOffset + 3]] & 0xFF);
            destination[destOffset] = (byte)(outBuff >> 16);
            destination[destOffset + 1] = (byte)(outBuff >> 8);
            destination[destOffset + 2] = (byte)outBuff;
            return 3;
        }
        catch (final Exception e) {
            System.out.println("" + source[srcOffset] + ": " + Base64.DECODABET[source[srcOffset]]);
            System.out.println("" + source[srcOffset + 1] + ": " + Base64.DECODABET[source[srcOffset + 1]]);
            System.out.println("" + source[srcOffset + 2] + ": " + Base64.DECODABET[source[srcOffset + 2]]);
            System.out.println("" + source[srcOffset + 3] + ": " + Base64.DECODABET[source[srcOffset + 3]]);
            return -1;
        }
    }
    
    public static byte[] decode(final byte[] source, final int off, final int len) {
        final int len2 = len * 3 / 4;
        final byte[] outBuff = new byte[len2];
        int outBuffPosn = 0;
        final byte[] b4 = new byte[4];
        int b4Posn = 0;
        int i = 0;
        byte sbiCrop = 0;
        byte sbiDecode = 0;
        for (i = off; i < off + len; ++i) {
            sbiCrop = (byte)(source[i] & 0x7F);
            sbiDecode = Base64.DECODABET[sbiCrop];
            if (sbiDecode < -5) {
                throw new IllegalArgumentException("Bad Base64 input character at " + i + ": " + source[i] + "(decimal)");
            }
            if (sbiDecode >= -1) {
                b4[b4Posn++] = sbiCrop;
                if (b4Posn > 3) {
                    outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn);
                    b4Posn = 0;
                    if (sbiCrop == 61) {
                        break;
                    }
                }
            }
        }
        final byte[] out = new byte[outBuffPosn];
        System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
        return out;
    }
    
    public static byte[] decode(final String s) {
        byte[] bytes;
        try {
            bytes = s.getBytes("UTF-8");
        }
        catch (final UnsupportedEncodingException uee) {
            bytes = s.getBytes();
        }
        bytes = decode(bytes, 0, bytes.length);
        return bytes;
    }
    
    static {
        _NATIVE_ALPHABET = new byte[] { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47 };
        byte[] bytes;
        try {
            bytes = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes("UTF-8");
        }
        catch (final UnsupportedEncodingException use) {
            bytes = Base64._NATIVE_ALPHABET;
        }
        ALPHABET = bytes;
        DECODABET = new byte[] { -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -5, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 62, -9, -9, -9, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -9, -9, -9, -1, -9, -9, -9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -9, -9, -9, -9, -9, -9, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -9, -9, -9, -9 };
    }
}
