// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.util;

import org.codehaus.jackson.map.AnnotationIntrospector;
import java.util.HashMap;

public class EnumResolver<T extends Enum<T>>
{
    protected final Class<T> _enumClass;
    protected final T[] _enums;
    protected final HashMap<String, T> _enumsById;
    
    protected EnumResolver(final Class<T> enumClass, final T[] enums, final HashMap<String, T> map) {
        this._enumClass = enumClass;
        this._enums = enums;
        this._enumsById = map;
    }
    
    public static <ET extends Enum<ET>> EnumResolver<ET> constructFor(final Class<ET> enumCls, final AnnotationIntrospector ai) {
        final ET[] enumValues = enumCls.getEnumConstants();
        if (enumValues == null) {
            throw new IllegalArgumentException("No enum constants for class " + enumCls.getName());
        }
        final HashMap<String, ET> map = new HashMap<String, ET>();
        for (final ET e : enumValues) {
            map.put(ai.findEnumValue(e), e);
        }
        return new EnumResolver<ET>(enumCls, enumValues, map);
    }
    
    public static <ET extends Enum<ET>> EnumResolver<ET> constructUsingToString(final Class<ET> enumCls) {
        final ET[] enumValues = enumCls.getEnumConstants();
        final HashMap<String, ET> map = new HashMap<String, ET>();
        int i = enumValues.length;
        while (--i >= 0) {
            final ET e = enumValues[i];
            map.put(e.toString(), e);
        }
        return new EnumResolver<ET>(enumCls, enumValues, map);
    }
    
    public static EnumResolver<?> constructUnsafe(final Class<?> rawEnumCls, final AnnotationIntrospector ai) {
        final Class<Enum> enumCls = (Class<Enum>)rawEnumCls;
        return constructFor((Class<?>)enumCls, ai);
    }
    
    public static EnumResolver<?> constructUnsafeUsingToString(final Class<?> rawEnumCls) {
        final Class<Enum> enumCls = (Class<Enum>)rawEnumCls;
        return constructUsingToString((Class<?>)enumCls);
    }
    
    public T findEnum(final String key) {
        return this._enumsById.get(key);
    }
    
    public T getEnum(final int index) {
        if (index < 0 || index >= this._enums.length) {
            return null;
        }
        return this._enums[index];
    }
    
    public Class<T> getEnumClass() {
        return this._enumClass;
    }
    
    public int lastValidIndex() {
        return this._enums.length - 1;
    }
}
