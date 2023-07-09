// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.util;

import java.util.Collection;
import java.util.HashMap;
import org.codehaus.jackson.map.AnnotationIntrospector;
import java.util.Map;
import org.codehaus.jackson.io.SerializedString;
import java.util.EnumMap;

public final class EnumValues
{
    private final EnumMap<?, SerializedString> _values;
    
    private EnumValues(final Map<Enum<?>, SerializedString> v) {
        this._values = new EnumMap<Object, SerializedString>(v);
    }
    
    public static EnumValues construct(final Class<Enum<?>> enumClass, final AnnotationIntrospector intr) {
        return constructFromName(enumClass, intr);
    }
    
    public static EnumValues constructFromName(final Class<Enum<?>> enumClass, final AnnotationIntrospector intr) {
        final Class<? extends Enum<?>> cls = ClassUtil.findEnumType(enumClass);
        final Enum<?>[] values = (Enum<?>[])cls.getEnumConstants();
        if (values != null) {
            final Map<Enum<?>, SerializedString> map = new HashMap<Enum<?>, SerializedString>();
            for (final Enum<?> en : values) {
                final String value = intr.findEnumValue(en);
                map.put(en, new SerializedString(value));
            }
            return new EnumValues(map);
        }
        throw new IllegalArgumentException("Can not determine enum constants for Class " + enumClass.getName());
    }
    
    public static EnumValues constructFromToString(final Class<Enum<?>> enumClass, final AnnotationIntrospector intr) {
        final Class<? extends Enum<?>> cls = ClassUtil.findEnumType(enumClass);
        final Enum<?>[] values = (Enum<?>[])cls.getEnumConstants();
        if (values != null) {
            final Map<Enum<?>, SerializedString> map = new HashMap<Enum<?>, SerializedString>();
            for (final Enum<?> en : values) {
                map.put(en, new SerializedString(en.toString()));
            }
            return new EnumValues(map);
        }
        throw new IllegalArgumentException("Can not determine enum constants for Class " + enumClass.getName());
    }
    
    @Deprecated
    public String valueFor(final Enum<?> key) {
        final SerializedString sstr = this._values.get(key);
        return (sstr == null) ? null : sstr.getValue();
    }
    
    public SerializedString serializedValueFor(final Enum<?> key) {
        return this._values.get(key);
    }
    
    public Collection<SerializedString> values() {
        return this._values.values();
    }
}
