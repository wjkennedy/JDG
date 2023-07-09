// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.jsontype.impl;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;

public class AsExternalTypeSerializer extends TypeSerializerBase
{
    protected final String _typePropertyName;
    
    public AsExternalTypeSerializer(final TypeIdResolver idRes, final BeanProperty property, final String propName) {
        super(idRes, property);
        this._typePropertyName = propName;
    }
    
    @Override
    public String getPropertyName() {
        return this._typePropertyName;
    }
    
    @Override
    public JsonTypeInfo.As getTypeInclusion() {
        return JsonTypeInfo.As.EXTERNAL_PROPERTY;
    }
    
    @Override
    public void writeTypePrefixForObject(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        this._writePrefix(value, jgen);
    }
    
    @Override
    public void writeTypePrefixForObject(final Object value, final JsonGenerator jgen, final Class<?> type) throws IOException, JsonProcessingException {
        this._writePrefix(value, jgen, type);
    }
    
    @Override
    public void writeTypePrefixForArray(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        this._writePrefix(value, jgen);
    }
    
    @Override
    public void writeTypePrefixForArray(final Object value, final JsonGenerator jgen, final Class<?> type) throws IOException, JsonProcessingException {
        this._writePrefix(value, jgen, type);
    }
    
    @Override
    public void writeTypePrefixForScalar(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        this._writePrefix(value, jgen);
    }
    
    @Override
    public void writeTypePrefixForScalar(final Object value, final JsonGenerator jgen, final Class<?> type) throws IOException, JsonProcessingException {
        this._writePrefix(value, jgen, type);
    }
    
    @Override
    public void writeTypeSuffixForObject(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        this._writeSuffix(value, jgen);
    }
    
    @Override
    public void writeTypeSuffixForArray(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        this._writeSuffix(value, jgen);
    }
    
    @Override
    public void writeTypeSuffixForScalar(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        this._writeSuffix(value, jgen);
    }
    
    protected final void _writePrefix(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
    }
    
    protected final void _writePrefix(final Object value, final JsonGenerator jgen, final Class<?> type) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
    }
    
    protected final void _writeSuffix(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        jgen.writeEndObject();
        jgen.writeStringField(this._typePropertyName, this._idResolver.idFromValue(value));
    }
}
