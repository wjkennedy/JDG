// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.math.BigDecimal;
import java.nio.CharBuffer;

public class ByteConverter
{
    private static final int NBASE = 10000;
    private static final int NUMERIC_DSCALE_MASK = 16383;
    private static final short NUMERIC_POS = 0;
    private static final short NUMERIC_NEG = 16384;
    private static final short NUMERIC_NAN = -16384;
    private static final int DEC_DIGITS = 4;
    private static final int[] round_powers;
    private static final int SHORT_BYTES = 2;
    private static final int LONG_BYTES = 4;
    
    private ByteConverter() {
    }
    
    public static int bytesToInt(final byte[] bytes) {
        if (bytes.length == 1) {
            return bytes[0];
        }
        if (bytes.length == 2) {
            return int2(bytes, 0);
        }
        if (bytes.length == 4) {
            return int4(bytes, 0);
        }
        throw new IllegalArgumentException("Argument bytes is empty");
    }
    
    private static void digitToString(final int idx, final short[] digits, final CharBuffer buffer, boolean alwaysPutIt) {
        short dig = (short)((idx >= 0 && idx < digits.length) ? digits[idx] : 0);
        for (int p = 1; p < ByteConverter.round_powers.length; ++p) {
            final int pow = ByteConverter.round_powers[p];
            final short d1 = (short)(dig / pow);
            dig -= (short)(d1 * pow);
            final boolean putit = d1 > 0;
            if (putit || alwaysPutIt) {
                buffer.put((char)(d1 + 48));
                alwaysPutIt = true;
            }
        }
        buffer.put((char)(dig + 48));
    }
    
    private static String numberBytesToString(final short[] digits, final int scale, final int weight, final int sign) {
        int i = (weight + 1) * 4;
        if (i <= 0) {
            i = 1;
        }
        final CharBuffer buffer = CharBuffer.allocate(i + scale + 4 + 2);
        if (sign == 16384) {
            buffer.put('-');
        }
        int d;
        if (weight < 0) {
            d = weight + 1;
            buffer.put('0');
        }
        else {
            for (d = 0; d <= weight; ++d) {
                digitToString(d, digits, buffer, d != 0);
            }
        }
        if (scale > 0) {
            buffer.put('.');
            for (i = 0; i < scale; i += 4) {
                digitToString(d, digits, buffer, true);
                ++d;
            }
        }
        final int extra = (i - scale) % 4;
        return new String(buffer.array(), 0, buffer.position() - extra);
    }
    
    public static Number numeric(final byte[] bytes) {
        return numeric(bytes, 0, bytes.length);
    }
    
    public static Number numeric(final byte[] bytes, final int pos, final int numBytes) {
        if (numBytes < 8) {
            throw new IllegalArgumentException("number of bytes should be at-least 8");
        }
        final short len = int2(bytes, pos);
        final short weight = int2(bytes, pos + 2);
        final short sign = int2(bytes, pos + 4);
        final short scale = int2(bytes, pos + 6);
        if (numBytes != len * 2 + 8) {
            throw new IllegalArgumentException("invalid length of bytes \"numeric\" value");
        }
        if (sign != 0 && sign != 16384 && sign != -16384) {
            throw new IllegalArgumentException("invalid sign in \"numeric\" value");
        }
        if (sign == -16384) {
            return Double.NaN;
        }
        if ((scale & 0x3FFF) != scale) {
            throw new IllegalArgumentException("invalid scale in \"numeric\" value");
        }
        final short[] digits = new short[len];
        int idx = pos + 8;
        for (int i = 0; i < len; ++i) {
            final short d = int2(bytes, idx);
            idx += 2;
            if (d < 0 || d >= 10000) {
                throw new IllegalArgumentException("invalid digit in \"numeric\" value");
            }
            digits[i] = d;
        }
        final String numString = numberBytesToString(digits, scale, weight, sign);
        return new BigDecimal(numString);
    }
    
    public static long int8(final byte[] bytes, final int idx) {
        return ((long)(bytes[idx + 0] & 0xFF) << 56) + ((long)(bytes[idx + 1] & 0xFF) << 48) + ((long)(bytes[idx + 2] & 0xFF) << 40) + ((long)(bytes[idx + 3] & 0xFF) << 32) + ((long)(bytes[idx + 4] & 0xFF) << 24) + ((long)(bytes[idx + 5] & 0xFF) << 16) + ((long)(bytes[idx + 6] & 0xFF) << 8) + (bytes[idx + 7] & 0xFF);
    }
    
    public static int int4(final byte[] bytes, final int idx) {
        return ((bytes[idx] & 0xFF) << 24) + ((bytes[idx + 1] & 0xFF) << 16) + ((bytes[idx + 2] & 0xFF) << 8) + (bytes[idx + 3] & 0xFF);
    }
    
    public static short int2(final byte[] bytes, final int idx) {
        return (short)(((bytes[idx] & 0xFF) << 8) + (bytes[idx + 1] & 0xFF));
    }
    
    public static boolean bool(final byte[] bytes, final int idx) {
        return bytes[idx] == 1;
    }
    
    public static float float4(final byte[] bytes, final int idx) {
        return Float.intBitsToFloat(int4(bytes, idx));
    }
    
    public static double float8(final byte[] bytes, final int idx) {
        return Double.longBitsToDouble(int8(bytes, idx));
    }
    
    public static void int8(final byte[] target, final int idx, final long value) {
        target[idx + 0] = (byte)(value >>> 56);
        target[idx + 1] = (byte)(value >>> 48);
        target[idx + 2] = (byte)(value >>> 40);
        target[idx + 3] = (byte)(value >>> 32);
        target[idx + 4] = (byte)(value >>> 24);
        target[idx + 5] = (byte)(value >>> 16);
        target[idx + 6] = (byte)(value >>> 8);
        target[idx + 7] = (byte)value;
    }
    
    public static void int4(final byte[] target, final int idx, final int value) {
        target[idx + 0] = (byte)(value >>> 24);
        target[idx + 1] = (byte)(value >>> 16);
        target[idx + 2] = (byte)(value >>> 8);
        target[idx + 3] = (byte)value;
    }
    
    public static void int2(final byte[] target, final int idx, final int value) {
        target[idx + 0] = (byte)(value >>> 8);
        target[idx + 1] = (byte)value;
    }
    
    public static void bool(final byte[] target, final int idx, final boolean value) {
        target[idx] = (byte)(value ? 1 : 0);
    }
    
    public static void float4(final byte[] target, final int idx, final float value) {
        int4(target, idx, Float.floatToRawIntBits(value));
    }
    
    public static void float8(final byte[] target, final int idx, final double value) {
        int8(target, idx, Double.doubleToRawLongBits(value));
    }
    
    static {
        round_powers = new int[] { 0, 1000, 100, 10 };
    }
}
