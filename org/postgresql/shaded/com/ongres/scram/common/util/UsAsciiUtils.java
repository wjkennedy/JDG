// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.util;

public class UsAsciiUtils
{
    public static String toPrintable(final String value) throws IllegalArgumentException {
        Preconditions.checkNotNull(value, "value");
        final char[] printable = new char[value.length()];
        int i = 0;
        for (final int c : value.toCharArray()) {
            final char chr = (char)c;
            if (c < 0 || c >= 127) {
                throw new IllegalArgumentException("value contains character '" + chr + "' which is non US-ASCII");
            }
            if (c > 32) {
                printable[i++] = chr;
            }
        }
        return (i == value.length()) ? value : new String(printable, 0, i);
    }
}
