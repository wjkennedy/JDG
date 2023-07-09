// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.jsontype.impl;

import java.util.Map;
import java.util.EnumMap;
import java.util.Collection;
import java.util.EnumSet;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

public class ClassNameIdResolver extends TypeIdResolverBase
{
    public ClassNameIdResolver(final JavaType baseType, final TypeFactory typeFactory) {
        super(baseType, typeFactory);
    }
    
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CLASS;
    }
    
    public void registerSubtype(final Class<?> type, final String name) {
    }
    
    public String idFromValue(final Object value) {
        return this._idFrom(value, value.getClass());
    }
    
    public String idFromValueAndType(final Object value, final Class<?> type) {
        return this._idFrom(value, type);
    }
    
    public JavaType typeFromId(final String id) {
        if (id.indexOf(60) > 0) {
            final JavaType t = this._typeFactory.constructFromCanonical(id);
            if (!t.isTypeOrSubTypeOf(this._baseType.getRawClass())) {
                throw new IllegalArgumentException(String.format("Class %s not subtype of %s", t.getRawClass().getName(), this._baseType));
            }
            return t;
        }
        else {
            try {
                final Class<?> cls = ClassUtil.findClass(id);
                return this._typeFactory.constructSpecializedType(this._baseType, cls);
            }
            catch (final ClassNotFoundException e) {
                throw new IllegalArgumentException("Invalid type id '" + id + "' (for id type 'Id.class'): no such class found");
            }
            catch (final Exception e2) {
                throw new IllegalArgumentException("Invalid type id '" + id + "' (for id type 'Id.class'): " + e2.getMessage(), e2);
            }
        }
    }
    
    protected final String _idFrom(final Object value, Class<?> cls) {
        if (Enum.class.isAssignableFrom(cls) && !cls.isEnum()) {
            cls = cls.getSuperclass();
        }
        String str = cls.getName();
        if (str.startsWith("java.util")) {
            if (value instanceof EnumSet) {
                final Class<?> enumClass = ClassUtil.findEnumType((EnumSet<?>)value);
                str = TypeFactory.defaultInstance().constructCollectionType(EnumSet.class, enumClass).toCanonical();
            }
            else if (value instanceof EnumMap) {
                final Class<?> enumClass = ClassUtil.findEnumType((EnumMap<?, ?>)value);
                final Class<?> valueClass = Object.class;
                str = TypeFactory.defaultInstance().constructMapType(EnumMap.class, enumClass, valueClass).toCanonical();
            }
            else {
                final String end = str.substring(9);
                if ((end.startsWith(".Arrays$") || end.startsWith(".Collections$")) && str.indexOf("List") >= 0) {
                    str = "java.util.ArrayList";
                }
            }
        }
        else if (str.indexOf(36) >= 0) {
            final Class<?> outer = ClassUtil.getOuterClass(cls);
            if (outer != null) {
                final Class<?> staticType = this._baseType.getRawClass();
                if (ClassUtil.getOuterClass(staticType) == null) {
                    cls = this._baseType.getRawClass();
                    str = cls.getName();
                }
            }
        }
        return str;
    }
}
