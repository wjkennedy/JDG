// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.checker.formatter.qual;

import java.util.Date;
import java.util.Calendar;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.checkerframework.dataflow.qual.Pure;
import java.util.Collection;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Set;

public enum ConversionCategory
{
    GENERAL((Class<?>[])null, "bBhHsS"), 
    CHAR((Class<?>[])new Class[] { Character.class, Byte.class, Short.class, Integer.class }, "cC"), 
    INT((Class<?>[])new Class[] { Byte.class, Short.class, Integer.class, Long.class, BigInteger.class }, "doxX"), 
    FLOAT((Class<?>[])new Class[] { Float.class, Double.class, BigDecimal.class }, "eEfgGaA"), 
    TIME((Class<?>[])new Class[] { Long.class, Calendar.class, Date.class }, "tT"), 
    CHAR_AND_INT((Class<?>[])new Class[] { Byte.class, Short.class, Integer.class }, (String)null), 
    INT_AND_TIME((Class<?>[])new Class[] { Long.class }, (String)null), 
    NULL((Class<?>[])new Class[0], (String)null), 
    UNUSED((Class<?>[])null, (String)null);
    
    public final Class<?>[] types;
    public final String chars;
    
    private ConversionCategory(final Class<?>[] types, final String chars) {
        this.types = types;
        this.chars = chars;
    }
    
    public static ConversionCategory fromConversionChar(final char c) {
        for (final ConversionCategory v : new ConversionCategory[] { ConversionCategory.GENERAL, ConversionCategory.CHAR, ConversionCategory.INT, ConversionCategory.FLOAT, ConversionCategory.TIME }) {
            if (v.chars.contains(String.valueOf(c))) {
                return v;
            }
        }
        throw new IllegalArgumentException("Bad conversion character " + c);
    }
    
    private static <E> Set<E> arrayToSet(final E[] a) {
        return new HashSet<E>((Collection<? extends E>)Arrays.asList(a));
    }
    
    public static boolean isSubsetOf(final ConversionCategory a, final ConversionCategory b) {
        return intersect(a, b) == a;
    }
    
    public static ConversionCategory intersect(final ConversionCategory a, final ConversionCategory b) {
        if (a == ConversionCategory.UNUSED) {
            return b;
        }
        if (b == ConversionCategory.UNUSED) {
            return a;
        }
        if (a == ConversionCategory.GENERAL) {
            return b;
        }
        if (b == ConversionCategory.GENERAL) {
            return a;
        }
        final Set<Class<?>> as = arrayToSet(a.types);
        final Set<Class<?>> bs = arrayToSet(b.types);
        as.retainAll(bs);
        for (final ConversionCategory v : new ConversionCategory[] { ConversionCategory.CHAR, ConversionCategory.INT, ConversionCategory.FLOAT, ConversionCategory.TIME, ConversionCategory.CHAR_AND_INT, ConversionCategory.INT_AND_TIME, ConversionCategory.NULL }) {
            final Set<Class<?>> vs = arrayToSet(v.types);
            if (vs.equals(as)) {
                return v;
            }
        }
        throw new RuntimeException();
    }
    
    public static ConversionCategory union(final ConversionCategory a, final ConversionCategory b) {
        if (a == ConversionCategory.UNUSED || b == ConversionCategory.UNUSED) {
            return ConversionCategory.UNUSED;
        }
        if (a == ConversionCategory.GENERAL || b == ConversionCategory.GENERAL) {
            return ConversionCategory.GENERAL;
        }
        if ((a == ConversionCategory.CHAR_AND_INT && b == ConversionCategory.INT_AND_TIME) || (a == ConversionCategory.INT_AND_TIME && b == ConversionCategory.CHAR_AND_INT)) {
            return ConversionCategory.INT;
        }
        final Set<Class<?>> as = arrayToSet(a.types);
        final Set<Class<?>> bs = arrayToSet(b.types);
        as.addAll(bs);
        for (final ConversionCategory v : new ConversionCategory[] { ConversionCategory.NULL, ConversionCategory.CHAR_AND_INT, ConversionCategory.INT_AND_TIME, ConversionCategory.CHAR, ConversionCategory.INT, ConversionCategory.FLOAT, ConversionCategory.TIME }) {
            final Set<Class<?>> vs = arrayToSet(v.types);
            if (vs.equals(as)) {
                return v;
            }
        }
        return ConversionCategory.GENERAL;
    }
    
    private String className(final Class<?> cls) {
        if (cls == Boolean.class) {
            return "boolean";
        }
        if (cls == Character.class) {
            return "char";
        }
        if (cls == Byte.class) {
            return "byte";
        }
        if (cls == Short.class) {
            return "short";
        }
        if (cls == Integer.class) {
            return "int";
        }
        if (cls == Long.class) {
            return "long";
        }
        if (cls == Float.class) {
            return "float";
        }
        if (cls == Double.class) {
            return "double";
        }
        return cls.getSimpleName();
    }
    
    @Pure
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(this.name());
        sb.append(" conversion category (one of: ");
        boolean first = true;
        for (final Class<?> cls : this.types) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(this.className(cls));
            first = false;
        }
        sb.append(")");
        return sb.toString();
    }
}
