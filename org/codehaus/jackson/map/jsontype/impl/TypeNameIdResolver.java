// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.jsontype.impl;

import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import java.util.Iterator;
import org.codehaus.jackson.map.jsontype.NamedType;
import java.util.Collection;
import org.codehaus.jackson.type.JavaType;
import java.util.HashMap;
import org.codehaus.jackson.map.MapperConfig;

public class TypeNameIdResolver extends TypeIdResolverBase
{
    protected final MapperConfig<?> _config;
    protected final HashMap<String, String> _typeToId;
    protected final HashMap<String, JavaType> _idToType;
    
    protected TypeNameIdResolver(final MapperConfig<?> config, final JavaType baseType, final HashMap<String, String> typeToId, final HashMap<String, JavaType> idToType) {
        super(baseType, config.getTypeFactory());
        this._config = config;
        this._typeToId = typeToId;
        this._idToType = idToType;
    }
    
    public static TypeNameIdResolver construct(final MapperConfig<?> config, final JavaType baseType, final Collection<NamedType> subtypes, final boolean forSer, final boolean forDeser) {
        if (forSer == forDeser) {
            throw new IllegalArgumentException();
        }
        HashMap<String, String> typeToId = null;
        HashMap<String, JavaType> idToType = null;
        if (forSer) {
            typeToId = new HashMap<String, String>();
        }
        if (forDeser) {
            idToType = new HashMap<String, JavaType>();
        }
        if (subtypes != null) {
            for (final NamedType t : subtypes) {
                final Class<?> cls = t.getType();
                final String id = t.hasName() ? t.getName() : _defaultTypeId(cls);
                if (forSer) {
                    typeToId.put(cls.getName(), id);
                }
                if (forDeser) {
                    final JavaType prev = idToType.get(id);
                    if (prev != null && cls.isAssignableFrom(prev.getRawClass())) {
                        continue;
                    }
                    idToType.put(id, config.constructType(cls));
                }
            }
        }
        return new TypeNameIdResolver(config, baseType, typeToId, idToType);
    }
    
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.NAME;
    }
    
    public String idFromValue(final Object value) {
        final Class<?> cls = value.getClass();
        final String key = cls.getName();
        String name;
        synchronized (this._typeToId) {
            name = this._typeToId.get(key);
            if (name == null) {
                if (this._config.isAnnotationProcessingEnabled()) {
                    final BasicBeanDescription beanDesc = this._config.introspectClassAnnotations(cls);
                    name = this._config.getAnnotationIntrospector().findTypeName(beanDesc.getClassInfo());
                }
                if (name == null) {
                    name = _defaultTypeId(cls);
                }
                this._typeToId.put(key, name);
            }
        }
        return name;
    }
    
    public String idFromValueAndType(final Object value, final Class<?> type) {
        return this.idFromValue(value);
    }
    
    public JavaType typeFromId(final String id) throws IllegalArgumentException {
        final JavaType t = this._idToType.get(id);
        return t;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('[').append(this.getClass().getName());
        sb.append("; id-to-type=").append(this._idToType);
        sb.append(']');
        return sb.toString();
    }
    
    protected static String _defaultTypeId(final Class<?> cls) {
        final String n = cls.getName();
        final int ix = n.lastIndexOf(46);
        return (ix < 0) ? n : n.substring(ix + 1);
    }
}
