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

public class AsWrapperTypeSerializer extends TypeSerializerBase
{
    public AsWrapperTypeSerializer(final TypeIdResolver idRes, final BeanProperty property) {
        super(idRes, property);
    }
    
    @Override
    public JsonTypeInfo.As getTypeInclusion() {
        return JsonTypeInfo.As.WRAPPER_OBJECT;
    }
    
    @Override
    public void writeTypePrefixForObject(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeObjectFieldStart(this._idResolver.idFromValue(value));
    }
    
    @Override
    public void writeTypePrefixForObject(final Object value, final JsonGenerator jgen, final Class<?> type) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeObjectFieldStart(this._idResolver.idFromValueAndType(value, type));
    }
    
    @Override
    public void writeTypePrefixForArray(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeArrayFieldStart(this._idResolver.idFromValue(value));
    }
    
    @Override
    public void writeTypePrefixForArray(final Object value, final JsonGenerator jgen, final Class<?> type) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeArrayFieldStart(this._idResolver.idFromValueAndType(value, type));
    }
    
    @Override
    public void writeTypePrefixForScalar(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeFieldName(this._idResolver.idFromValue(value));
    }
    
    @Override
    public void writeTypePrefixForScalar(final Object value, final JsonGenerator jgen, final Class<?> type) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeFieldName(this._idResolver.idFromValueAndType(value, type));
    }
    
    @Override
    public void writeTypeSuffixForObject(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        jgen.writeEndObject();
        jgen.writeEndObject();
    }
    
    @Override
    public void writeTypeSuffixForArray(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        jgen.writeEndArray();
        jgen.writeEndObject();
    }
    
    @Override
    public void writeTypeSuffixForScalar(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        jgen.writeEndObject();
    }
}
