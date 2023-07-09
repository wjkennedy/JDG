// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.io;

public final class NumberOutput
{
    private static final char NULL_CHAR = '\0';
    private static int MILLION;
    private static int BILLION;
    private static long TEN_BILLION_L;
    private static long THOUSAND_L;
    private static long MIN_INT_AS_LONG;
    private static long MAX_INT_AS_LONG;
    static final String SMALLEST_LONG;
    static final char[] LEADING_TRIPLETS;
    static final char[] FULL_TRIPLETS;
    static final byte[] FULL_TRIPLETS_B;
    static final String[] sSmallIntStrs;
    static final String[] sSmallIntStrs2;
    
    public static int outputInt(int value, final char[] buffer, int offset) {
        if (value < 0) {
            if (value == Integer.MIN_VALUE) {
                return outputLong(value, buffer, offset);
            }
            buffer[offset++] = '-';
            value = -value;
        }
        if (value < NumberOutput.MILLION) {
            if (value < 1000) {
                if (value < 10) {
                    buffer[offset++] = (char)(48 + value);
                }
                else {
                    offset = outputLeadingTriplet(value, buffer, offset);
                }
            }
            else {
                final int thousands = value / 1000;
                value -= thousands * 1000;
                offset = outputLeadingTriplet(thousands, buffer, offset);
                offset = outputFullTriplet(value, buffer, offset);
            }
            return offset;
        }
        final boolean hasBillions = value >= NumberOutput.BILLION;
        if (hasBillions) {
            value -= NumberOutput.BILLION;
            if (value >= NumberOutput.BILLION) {
                value -= NumberOutput.BILLION;
                buffer[offset++] = '2';
            }
            else {
                buffer[offset++] = '1';
            }
        }
        int newValue = value / 1000;
        final int ones = value - newValue * 1000;
        value = newValue;
        newValue /= 1000;
        final int thousands2 = value - newValue * 1000;
        if (hasBillions) {
            offset = outputFullTriplet(newValue, buffer, offset);
        }
        else {
            offset = outputLeadingTriplet(newValue, buffer, offset);
        }
        offset = outputFullTriplet(thousands2, buffer, offset);
        offset = outputFullTriplet(ones, buffer, offset);
        return offset;
    }
    
    public static int outputInt(int value, final byte[] buffer, int offset) {
        if (value < 0) {
            if (value == Integer.MIN_VALUE) {
                return outputLong(value, buffer, offset);
            }
            buffer[offset++] = 45;
            value = -value;
        }
        if (value < NumberOutput.MILLION) {
            if (value < 1000) {
                if (value < 10) {
                    buffer[offset++] = (byte)(48 + value);
                }
                else {
                    offset = outputLeadingTriplet(value, buffer, offset);
                }
            }
            else {
                final int thousands = value / 1000;
                value -= thousands * 1000;
                offset = outputLeadingTriplet(thousands, buffer, offset);
                offset = outputFullTriplet(value, buffer, offset);
            }
            return offset;
        }
        final boolean hasBillions = value >= NumberOutput.BILLION;
        if (hasBillions) {
            value -= NumberOutput.BILLION;
            if (value >= NumberOutput.BILLION) {
                value -= NumberOutput.BILLION;
                buffer[offset++] = 50;
            }
            else {
                buffer[offset++] = 49;
            }
        }
        int newValue = value / 1000;
        final int ones = value - newValue * 1000;
        value = newValue;
        newValue /= 1000;
        final int thousands2 = value - newValue * 1000;
        if (hasBillions) {
            offset = outputFullTriplet(newValue, buffer, offset);
        }
        else {
            offset = outputLeadingTriplet(newValue, buffer, offset);
        }
        offset = outputFullTriplet(thousands2, buffer, offset);
        offset = outputFullTriplet(ones, buffer, offset);
        return offset;
    }
    
    public static int outputLong(long value, final char[] buffer, int offset) {
        if (value < 0L) {
            if (value > NumberOutput.MIN_INT_AS_LONG) {
                return outputInt((int)value, buffer, offset);
            }
            if (value == Long.MIN_VALUE) {
                final int len = NumberOutput.SMALLEST_LONG.length();
                NumberOutput.SMALLEST_LONG.getChars(0, len, buffer, offset);
                return offset + len;
            }
            buffer[offset++] = '-';
            value = -value;
        }
        else if (value <= NumberOutput.MAX_INT_AS_LONG) {
            return outputInt((int)value, buffer, offset);
        }
        final int origOffset = offset;
        int ptr;
        offset = (ptr = offset + calcLongStrLength(value));
        while (value > NumberOutput.MAX_INT_AS_LONG) {
            ptr -= 3;
            final long newValue = value / NumberOutput.THOUSAND_L;
            final int triplet = (int)(value - newValue * NumberOutput.THOUSAND_L);
            outputFullTriplet(triplet, buffer, ptr);
            value = newValue;
        }
        int ivalue;
        int newValue2;
        for (ivalue = (int)value; ivalue >= 1000; ivalue = newValue2) {
            ptr -= 3;
            newValue2 = ivalue / 1000;
            final int triplet = ivalue - newValue2 * 1000;
            outputFullTriplet(triplet, buffer, ptr);
        }
        outputLeadingTriplet(ivalue, buffer, origOffset);
        return offset;
    }
    
    public static int outputLong(long value, final byte[] buffer, int offset) {
        if (value < 0L) {
            if (value > NumberOutput.MIN_INT_AS_LONG) {
                return outputInt((int)value, buffer, offset);
            }
            if (value == Long.MIN_VALUE) {
                for (int len = NumberOutput.SMALLEST_LONG.length(), i = 0; i < len; ++i) {
                    buffer[offset++] = (byte)NumberOutput.SMALLEST_LONG.charAt(i);
                }
                return offset;
            }
            buffer[offset++] = 45;
            value = -value;
        }
        else if (value <= NumberOutput.MAX_INT_AS_LONG) {
            return outputInt((int)value, buffer, offset);
        }
        final int origOffset = offset;
        int ptr;
        offset = (ptr = offset + calcLongStrLength(value));
        while (value > NumberOutput.MAX_INT_AS_LONG) {
            ptr -= 3;
            final long newValue = value / NumberOutput.THOUSAND_L;
            final int triplet = (int)(value - newValue * NumberOutput.THOUSAND_L);
            outputFullTriplet(triplet, buffer, ptr);
            value = newValue;
        }
        int ivalue;
        int newValue2;
        for (ivalue = (int)value; ivalue >= 1000; ivalue = newValue2) {
            ptr -= 3;
            newValue2 = ivalue / 1000;
            final int triplet = ivalue - newValue2 * 1000;
            outputFullTriplet(triplet, buffer, ptr);
        }
        outputLeadingTriplet(ivalue, buffer, origOffset);
        return offset;
    }
    
    public static String toString(final int value) {
        if (value < NumberOutput.sSmallIntStrs.length) {
            if (value >= 0) {
                return NumberOutput.sSmallIntStrs[value];
            }
            final int v2 = -value - 1;
            if (v2 < NumberOutput.sSmallIntStrs2.length) {
                return NumberOutput.sSmallIntStrs2[v2];
            }
        }
        return Integer.toString(value);
    }
    
    public static String toString(final long value) {
        if (value <= 2147483647L && value >= -2147483648L) {
            return toString((int)value);
        }
        return Long.toString(value);
    }
    
    public static String toString(final double value) {
        return Double.toString(value);
    }
    
    private static int outputLeadingTriplet(final int triplet, final char[] buffer, int offset) {
        int digitOffset = triplet << 2;
        char c = NumberOutput.LEADING_TRIPLETS[digitOffset++];
        if (c != '\0') {
            buffer[offset++] = c;
        }
        c = NumberOutput.LEADING_TRIPLETS[digitOffset++];
        if (c != '\0') {
            buffer[offset++] = c;
        }
        buffer[offset++] = NumberOutput.LEADING_TRIPLETS[digitOffset];
        return offset;
    }
    
    private static int outputLeadingTriplet(final int triplet, final byte[] buffer, int offset) {
        int digitOffset = triplet << 2;
        char c = NumberOutput.LEADING_TRIPLETS[digitOffset++];
        if (c != '\0') {
            buffer[offset++] = (byte)c;
        }
        c = NumberOutput.LEADING_TRIPLETS[digitOffset++];
        if (c != '\0') {
            buffer[offset++] = (byte)c;
        }
        buffer[offset++] = (byte)NumberOutput.LEADING_TRIPLETS[digitOffset];
        return offset;
    }
    
    private static int outputFullTriplet(final int triplet, final char[] buffer, int offset) {
        int digitOffset = triplet << 2;
        buffer[offset++] = NumberOutput.FULL_TRIPLETS[digitOffset++];
        buffer[offset++] = NumberOutput.FULL_TRIPLETS[digitOffset++];
        buffer[offset++] = NumberOutput.FULL_TRIPLETS[digitOffset];
        return offset;
    }
    
    private static int outputFullTriplet(final int triplet, final byte[] buffer, int offset) {
        int digitOffset = triplet << 2;
        buffer[offset++] = NumberOutput.FULL_TRIPLETS_B[digitOffset++];
        buffer[offset++] = NumberOutput.FULL_TRIPLETS_B[digitOffset++];
        buffer[offset++] = NumberOutput.FULL_TRIPLETS_B[digitOffset];
        return offset;
    }
    
    private static int calcLongStrLength(final long posValue) {
        int len = 10;
        for (long comp = NumberOutput.TEN_BILLION_L; posValue >= comp && len != 19; ++len, comp = (comp << 3) + (comp << 1)) {}
        return len;
    }
    
    static {
        NumberOutput.MILLION = 1000000;
        NumberOutput.BILLION = 1000000000;
        NumberOutput.TEN_BILLION_L = 10000000000L;
        NumberOutput.THOUSAND_L = 1000L;
        NumberOutput.MIN_INT_AS_LONG = -2147483648L;
        NumberOutput.MAX_INT_AS_LONG = 2147483647L;
        SMALLEST_LONG = String.valueOf(Long.MIN_VALUE);
        LEADING_TRIPLETS = new char[4000];
        FULL_TRIPLETS = new char[4000];
        int ix = 0;
        for (int i1 = 0; i1 < 10; ++i1) {
            final char f1 = (char)(48 + i1);
            final char l1 = (i1 == 0) ? '\0' : f1;
            for (int i2 = 0; i2 < 10; ++i2) {
                final char f2 = (char)(48 + i2);
                final char l2 = (i1 == 0 && i2 == 0) ? '\0' : f2;
                for (int i3 = 0; i3 < 10; ++i3) {
                    final char f3 = (char)(48 + i3);
                    NumberOutput.LEADING_TRIPLETS[ix] = l1;
                    NumberOutput.LEADING_TRIPLETS[ix + 1] = l2;
                    NumberOutput.LEADING_TRIPLETS[ix + 2] = f3;
                    NumberOutput.FULL_TRIPLETS[ix] = f1;
                    NumberOutput.FULL_TRIPLETS[ix + 1] = f2;
                    NumberOutput.FULL_TRIPLETS[ix + 2] = f3;
                    ix += 4;
                }
            }
        }
        FULL_TRIPLETS_B = new byte[4000];
        for (int j = 0; j < 4000; ++j) {
            NumberOutput.FULL_TRIPLETS_B[j] = (byte)NumberOutput.FULL_TRIPLETS[j];
        }
        sSmallIntStrs = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" };
        sSmallIntStrs2 = new String[] { "-1", "-2", "-3", "-4", "-5", "-6", "-7", "-8", "-9", "-10" };
    }
}
