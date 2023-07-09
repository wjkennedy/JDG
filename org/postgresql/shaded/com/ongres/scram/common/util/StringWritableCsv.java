// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.util;

import java.util.Arrays;

public class StringWritableCsv
{
    private static void writeStringWritableToStringBuffer(final StringWritable value, final StringBuffer sb) {
        if (null != value) {
            value.writeTo(sb);
        }
    }
    
    public static StringBuffer writeTo(final StringBuffer sb, final StringWritable... values) throws IllegalArgumentException {
        Preconditions.checkNotNull(sb, "sb");
        if (null == values || values.length == 0) {
            return sb;
        }
        writeStringWritableToStringBuffer(values[0], sb);
        for (int i = 1; i < values.length; ++i) {
            sb.append(',');
            writeStringWritableToStringBuffer(values[i], sb);
        }
        return sb;
    }
    
    public static String[] parseFrom(final String value, final int n, final int offset) throws IllegalArgumentException {
        Preconditions.checkNotNull(value, "value");
        if (n < 0 || offset < 0) {
            throw new IllegalArgumentException("Limit and offset have to be >= 0");
        }
        if (value.isEmpty()) {
            return new String[0];
        }
        final String[] split = value.split(",");
        if (split.length < offset) {
            throw new IllegalArgumentException("Not enough items for the given offset");
        }
        return Arrays.copyOfRange(split, offset, ((n == 0) ? split.length : n) + offset);
    }
    
    public static String[] parseFrom(final String value, final int n) throws IllegalArgumentException {
        return parseFrom(value, n, 0);
    }
    
    public static String[] parseFrom(final String value) throws IllegalArgumentException {
        return parseFrom(value, 0, 0);
    }
}
