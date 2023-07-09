// 
// Decompiled by Procyon v0.6.0
// 

package org.checkerframework.checker.i18nformatter.qual;

import java.util.Date;
import java.util.Collection;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Set;

public enum I18nConversionCategory
{
    UNUSED((Class<?>[])null, (String[])null), 
    GENERAL((Class<?>[])null, (String[])null), 
    DATE((Class<?>[])new Class[] { Date.class, Number.class }, new String[] { "date", "time" }), 
    NUMBER((Class<?>[])new Class[] { Number.class }, new String[] { "number", "choice" });
    
    public final Class<?>[] types;
    public final String[] strings;
    static I18nConversionCategory[] namedCategories;
    
    private I18nConversionCategory(final Class<?>[] types, final String[] strings) {
        this.types = types;
        this.strings = strings;
    }
    
    public static I18nConversionCategory stringToI18nConversionCategory(String string) {
        string = string.toLowerCase();
        for (final I18nConversionCategory v : I18nConversionCategory.namedCategories) {
            for (final String s : v.strings) {
                if (s.equals(string)) {
                    return v;
                }
            }
        }
        throw new IllegalArgumentException("Invalid format type " + string);
    }
    
    private static <E> Set<E> arrayToSet(final E[] a) {
        return new HashSet<E>((Collection<? extends E>)Arrays.asList(a));
    }
    
    public static boolean isSubsetOf(final I18nConversionCategory a, final I18nConversionCategory b) {
        return intersect(a, b) == a;
    }
    
    public static I18nConversionCategory intersect(final I18nConversionCategory a, final I18nConversionCategory b) {
        if (a == I18nConversionCategory.UNUSED) {
            return b;
        }
        if (b == I18nConversionCategory.UNUSED) {
            return a;
        }
        if (a == I18nConversionCategory.GENERAL) {
            return b;
        }
        if (b == I18nConversionCategory.GENERAL) {
            return a;
        }
        final Set<Class<?>> as = arrayToSet(a.types);
        final Set<Class<?>> bs = arrayToSet(b.types);
        as.retainAll(bs);
        for (final I18nConversionCategory v : new I18nConversionCategory[] { I18nConversionCategory.DATE, I18nConversionCategory.NUMBER }) {
            final Set<Class<?>> vs = arrayToSet(v.types);
            if (vs.equals(as)) {
                return v;
            }
        }
        throw new RuntimeException();
    }
    
    public static I18nConversionCategory union(final I18nConversionCategory a, final I18nConversionCategory b) {
        if (a == I18nConversionCategory.UNUSED || b == I18nConversionCategory.UNUSED) {
            return I18nConversionCategory.UNUSED;
        }
        if (a == I18nConversionCategory.GENERAL || b == I18nConversionCategory.GENERAL) {
            return I18nConversionCategory.GENERAL;
        }
        if (a == I18nConversionCategory.DATE || b == I18nConversionCategory.DATE) {
            return I18nConversionCategory.DATE;
        }
        return I18nConversionCategory.NUMBER;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(this.name());
        if (this.types == null) {
            sb.append(" conversion category (all types)");
        }
        else {
            sb.append(" conversion category (one of: ");
            boolean first = true;
            for (final Class<?> cls : this.types) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(cls.getCanonicalName());
                first = false;
            }
            sb.append(")");
        }
        return sb.toString();
    }
    
    static {
        I18nConversionCategory.namedCategories = new I18nConversionCategory[] { I18nConversionCategory.DATE, I18nConversionCategory.NUMBER };
    }
}
