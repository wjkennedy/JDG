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

public class AsPropertyTypeSerializer extends AsArrayTypeSerializer
{
    protected final String _typePropertyName;
    
    public AsPropertyTypeSerializer(final TypeIdResolver idRes, final BeanProperty property, final String propName) {
        super(idRes, property);
        this._typePropertyName = propName;
    }
    
    @Override
    public String getPropertyName() {
        return this._typePropertyName;
    }
    
    @Override
    public JsonTypeInfo.As getTypeInclusion() {
        return JsonTypeInfo.As.PROPERTY;
    }
    
    @Override
    public void writeTypePrefixForObject(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeStringField(this._typePropertyName, this._idResolver.idFromValue(value));
    }
    
    @Override
    public void writeTypePrefixForObject(final Object value, final JsonGenerator jgen, final Class<?> type) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeStringField(this._typePropertyName, this._idResolver.idFromValueAndType(value, type));
    }
    
    @Override
    public void writeTypeSuffixForObject(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        jgen.writeEndObject();
    }
}
