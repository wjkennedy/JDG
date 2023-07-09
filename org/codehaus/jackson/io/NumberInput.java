// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.io;

public final class NumberInput
{
    public static final String NASTY_SMALL_DOUBLE = "2.2250738585072012e-308";
    static final long L_BILLION = 1000000000L;
    static final String MIN_LONG_STR_NO_SIGN;
    static final String MAX_LONG_STR;
    
    public static final int parseInt(final char[] digitChars, int offset, int len) {
        int num = digitChars[offset] - '0';
        len += offset;
        if (++offset < len) {
            num = num * 10 + (digitChars[offset] - '0');
            if (++offset < len) {
                num = num * 10 + (digitChars[offset] - '0');
                if (++offset < len) {
                    num = num * 10 + (digitChars[offset] - '0');
                    if (++offset < len) {
                        num = num * 10 + (digitChars[offset] - '0');
                        if (++offset < len) {
                            num = num * 10 + (digitChars[offset] - '0');
                            if (++offset < len) {
                                num = num * 10 + (digitChars[offset] - '0');
                                if (++offset < len) {
                                    num = num * 10 + (digitChars[offset] - '0');
                                    if (++offset < len) {
                                        num = num * 10 + (digitChars[offset] - '0');
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return num;
    }
    
    public static final int parseInt(final String str) {
        char c = str.charAt(0);
        final int length = str.length();
        final boolean negative = c == '-';
        int offset = 1;
        if (negative) {
            if (length == 1 || length > 10) {
                return Integer.parseInt(str);
            }
            c = str.charAt(offset++);
        }
        else if (length > 9) {
            return Integer.parseInt(str);
        }
        if (c > '9' || c < '0') {
            return Integer.parseInt(str);
        }
        int num = c - '0';
        if (offset < length) {
            c = str.charAt(offset++);
            if (c > '9' || c < '0') {
                return Integer.parseInt(str);
            }
            num = num * 10 + (c - '0');
            if (offset < length) {
                c = str.charAt(offset++);
                if (c > '9' || c < '0') {
                    return Integer.parseInt(str);
                }
                num = num * 10 + (c - '0');
                if (offset < length) {
                    do {
                        c = str.charAt(offset++);
                        if (c > '9' || c < '0') {
                            return Integer.parseInt(str);
                        }
                        num = num * 10 + (c - '0');
                    } while (offset < length);
                }
            }
        }
        return negative ? (-num) : num;
    }
    
    public static final long parseLong(final char[] digitChars, final int offset, final int len) {
        final int len2 = len - 9;
        final long val = parseInt(digitChars, offset, len2) * 1000000000L;
        return val + parseInt(digitChars, offset + len2, 9);
    }
    
    public static final long parseLong(final String str) {
        final int length = str.length();
        if (length <= 9) {
            return parseInt(str);
        }
        return Long.parseLong(str);
    }
    
    public static final boolean inLongRange(final char[] digitChars, final int offset, final int len, final boolean negative) {
        final String cmpStr = negative ? NumberInput.MIN_LONG_STR_NO_SIGN : NumberInput.MAX_LONG_STR;
        final int cmpLen = cmpStr.length();
        if (len < cmpLen) {
            return true;
        }
        if (len > cmpLen) {
            return false;
        }
        for (int i = 0; i < cmpLen; ++i) {
            final int diff = digitChars[offset + i] - cmpStr.charAt(i);
            if (diff != 0) {
                return diff < 0;
            }
        }
        return true;
    }
    
    public static final boolean inLongRange(final String numberStr, final boolean negative) {
        final String cmpStr = negative ? NumberInput.MIN_LONG_STR_NO_SIGN : NumberInput.MAX_LONG_STR;
        final int cmpLen = cmpStr.length();
        final int actualLen = numberStr.length();
        if (actualLen < cmpLen) {
            return true;
        }
        if (actualLen > cmpLen) {
            return false;
        }
        for (int i = 0; i < cmpLen; ++i) {
            final int diff = numberStr.charAt(i) - cmpStr.charAt(i);
            if (diff != 0) {
                return diff < 0;
            }
        }
        return true;
    }
    
    public static int parseAsInt(String input, final int defaultValue) {
        if (input == null) {
            return defaultValue;
        }
        input = input.trim();
        int len = input.length();
        if (len == 0) {
            return defaultValue;
        }
        int i = 0;
        if (i < len) {
            final char c = input.charAt(0);
            if (c == '+') {
                input = input.substring(1);
                len = input.length();
            }
            else if (c == '-') {
                ++i;
            }
        }
        while (i < len) {
            final char c = input.charAt(i);
            Label_0103: {
                if (c <= '9') {
                    if (c >= '0') {
                        break Label_0103;
                    }
                }
                try {
                    return (int)parseDouble(input);
                }
                catch (final NumberFormatException e) {
                    return defaultValue;
                }
            }
            ++i;
        }
        try {
            return Integer.parseInt(input);
        }
        catch (final NumberFormatException ex) {
            return defaultValue;
        }
    }
    
    public static long parseAsLong(String input, final long defaultValue) {
        if (input == null) {
            return defaultValue;
        }
        input = input.trim();
        int len = input.length();
        if (len == 0) {
            return defaultValue;
        }
        int i = 0;
        if (i < len) {
            final char c = input.charAt(0);
            if (c == '+') {
                input = input.substring(1);
                len = input.length();
            }
            else if (c == '-') {
                ++i;
            }
        }
        while (i < len) {
            final char c = input.charAt(i);
            Label_0107: {
                if (c <= '9') {
                    if (c >= '0') {
                        break Label_0107;
                    }
                }
                try {
                    return (long)parseDouble(input);
                }
                catch (final NumberFormatException e) {
                    return defaultValue;
                }
            }
            ++i;
        }
        try {
            return Long.parseLong(input);
        }
        catch (final NumberFormatException ex) {
            return defaultValue;
        }
    }
    
    public static double parseAsDouble(String input, final double defaultValue) {
        if (input == null) {
            return defaultValue;
        }
        input = input.trim();
        final int len = input.length();
        if (len == 0) {
            return defaultValue;
        }
        try {
            return parseDouble(input);
        }
        catch (final NumberFormatException ex) {
            return defaultValue;
        }
    }
    
    public static final double parseDouble(final String numStr) throws NumberFormatException {
        if ("2.2250738585072012e-308".equals(numStr)) {
            return Double.MIN_NORMAL;
        }
        return Double.parseDouble(numStr);
    }
    
    static {
        MIN_LONG_STR_NO_SIGN = String.valueOf(Long.MIN_VALUE).substring(1);
        MAX_LONG_STR = String.valueOf(Long.MAX_VALUE);
    }
}
