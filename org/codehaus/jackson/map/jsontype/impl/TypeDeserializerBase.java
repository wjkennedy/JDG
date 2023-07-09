// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.jsontype.impl;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.JsonDeserializer;
import java.util.HashMap;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.TypeDeserializer;

public abstract class TypeDeserializerBase extends TypeDeserializer
{
    protected final TypeIdResolver _idResolver;
    protected final JavaType _baseType;
    protected final BeanProperty _property;
    protected final JavaType _defaultImpl;
    protected final HashMap<String, JsonDeserializer<Object>> _deserializers;
    protected JsonDeserializer<Object> _defaultImplDeserializer;
    
    @Deprecated
    protected TypeDeserializerBase(final JavaType baseType, final TypeIdResolver idRes, final BeanProperty property) {
        this(baseType, idRes, property, null);
    }
    
    protected TypeDeserializerBase(final JavaType baseType, final TypeIdResolver idRes, final BeanProperty property, final Class<?> defaultImpl) {
        this._baseType = baseType;
        this._idResolver = idRes;
        this._property = property;
        this._deserializers = new HashMap<String, JsonDeserializer<Object>>();
        if (defaultImpl == null) {
            this._defaultImpl = null;
        }
        else {
            this._defaultImpl = baseType.forcedNarrowBy(defaultImpl);
        }
    }
    
    @Override
    public abstract JsonTypeInfo.As getTypeInclusion();
    
    public String baseTypeName() {
        return this._baseType.getRawClass().getName();
    }
    
    @Override
    public String getPropertyName() {
        return null;
    }
    
    @Override
    public TypeIdResolver getTypeIdResolver() {
        return this._idResolver;
    }
    
    @Override
    public Class<?> getDefaultImpl() {
        return (this._defaultImpl == null) ? null : this._defaultImpl.getRawClass();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('[').append(this.getClass().getName());
        sb.append("; base-type:").append(this._baseType);
        sb.append("; id-resolver: ").append(this._idResolver);
        sb.append(']');
        return sb.toString();
    }
    
    protected final JsonDeserializer<Object> _findDeserializer(final DeserializationContext ctxt, final String typeId) throws IOException, JsonProcessingException {
        JsonDeserializer<Object> deser;
        synchronized (this._deserializers) {
            deser = this._deserializers.get(typeId);
            if (deser == null) {
                JavaType type = this._idResolver.typeFromId(typeId);
                if (type == null) {
                    if (this._defaultImpl == null) {
                        throw ctxt.unknownTypeException(this._baseType, typeId);
                    }
                    deser = this._findDefaultImplDeserializer(ctxt);
                }
                else {
                    if (this._baseType != null && this._baseType.getClass() == type.getClass()) {
                        type = this._baseType.narrowBy(type.getRawClass());
                    }
                    deser = ctxt.getDeserializerProvider().findValueDeserializer(ctxt.getConfig(), type, this._property);
                }
                this._deserializers.put(typeId, deser);
            }
        }
        return deser;
    }
    
    protected final JsonDeserializer<Object> _findDefaultImplDeserializer(final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (this._defaultImpl == null) {
            return null;
        }
        synchronized (this._defaultImpl) {
            if (this._defaultImplDeserializer == null) {
                this._defaultImplDeserializer = ctxt.getDeserializerProvider().findValueDeserializer(ctxt.getConfig(), this._defaultImpl, this._property);
            }
            return this._defaultImplDeserializer;
        }
    }
}
