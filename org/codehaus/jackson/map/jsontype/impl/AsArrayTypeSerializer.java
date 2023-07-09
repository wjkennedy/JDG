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

public class AsArrayTypeSerializer extends TypeSerializerBase
{
    public AsArrayTypeSerializer(final TypeIdResolver idRes, final BeanProperty property) {
        super(idRes, property);
    }
    
    @Override
    public JsonTypeInfo.As getTypeInclusion() {
        return JsonTypeInfo.As.WRAPPER_ARRAY;
    }
    
    @Override
    public void writeTypePrefixForObject(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        jgen.writeStartArray();
        jgen.writeString(this._idResolver.idFromValue(value));
        jgen.writeStartObject();
    }
    
    @Override
    public void writeTypePrefixForObject(final Object value, final JsonGenerator jgen, final Class<?> type) throws IOException, JsonProcessingException {
        jgen.writeStartArray();
        jgen.writeString(this._idResolver.idFromValueAndType(value, type));
        jgen.writeStartObject();
    }
    
    @Override
    public void writeTypePrefixForArray(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        jgen.writeStartArray();
        jgen.writeString(this._idResolver.idFromValue(value));
        jgen.writeStartArray();
    }
    
    @Override
    public void writeTypePrefixForArray(final Object value, final JsonGenerator jgen, final Class<?> type) throws IOException, JsonProcessingException {
        jgen.writeStartArray();
        jgen.writeString(this._idResolver.idFromValueAndType(value, type));
        jgen.writeStartArray();
    }
    
    @Override
    public void writeTypePrefixForScalar(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        jgen.writeStartArray();
        jgen.writeString(this._idResolver.idFromValue(value));
    }
    
    @Override
    public void writeTypePrefixForScalar(final Object value, final JsonGenerator jgen, final Class<?> type) throws IOException, JsonProcessingException {
        jgen.writeStartArray();
        jgen.writeString(this._idResolver.idFromValueAndType(value, type));
    }
    
    @Override
    public void writeTypeSuffixForObject(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        jgen.writeEndObject();
        jgen.writeEndArray();
    }
    
    @Override
    public void writeTypeSuffixForArray(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        jgen.writeEndArray();
        jgen.writeEndArray();
    }
    
    @Override
    public void writeTypeSuffixForScalar(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        jgen.writeEndArray();
    }
}
