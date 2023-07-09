// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.shaded.com.ongres.scram.common.util;

public class Preconditions
{
    public static <T> T checkNotNull(final T value, final String valueName) throws IllegalArgumentException {
        if (null == value) {
            throw new IllegalArgumentException("Null value for '" + valueName + "'");
        }
        return value;
    }
    
    public static String checkNotEmpty(final String value, final String valueName) throws IllegalArgumentException {
        if (checkNotNull(value, valueName).isEmpty()) {
            throw new IllegalArgumentException("Empty string '" + valueName + "'");
        }
        return value;
    }
    
    public static void checkArgument(final boolean check, final String valueName) throws IllegalArgumentException {
        if (!check) {
            throw new IllegalArgumentException("Argument '" + valueName + "' is not valid");
        }
    }
    
    public static int gt0(final int value, final String valueName) throws IllegalArgumentException {
        if (value <= 0) {
            throw new IllegalArgumentException("'" + valueName + "' must be positive");
        }
        return value;
    }
}
