// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.annotate.JsonTypeInfo;

public abstract class TypeSerializer
{
    public abstract JsonTypeInfo.As getTypeInclusion();
    
    public abstract String getPropertyName();
    
    public abstract TypeIdResolver getTypeIdResolver();
    
    public abstract void writeTypePrefixForScalar(final Object p0, final JsonGenerator p1) throws IOException, JsonProcessingException;
    
    public abstract void writeTypePrefixForObject(final Object p0, final JsonGenerator p1) throws IOException, JsonProcessingException;
    
    public abstract void writeTypePrefixForArray(final Object p0, final JsonGenerator p1) throws IOException, JsonProcessingException;
    
    public abstract void writeTypeSuffixForScalar(final Object p0, final JsonGenerator p1) throws IOException, JsonProcessingException;
    
    public abstract void writeTypeSuffixForObject(final Object p0, final JsonGenerator p1) throws IOException, JsonProcessingException;
    
    public abstract void writeTypeSuffixForArray(final Object p0, final JsonGenerator p1) throws IOException, JsonProcessingException;
    
    public void writeTypePrefixForScalar(final Object value, final JsonGenerator jgen, final Class<?> type) throws IOException, JsonProcessingException {
        this.writeTypePrefixForScalar(value, jgen);
    }
    
    public void writeTypePrefixForObject(final Object value, final JsonGenerator jgen, final Class<?> type) throws IOException, JsonProcessingException {
        this.writeTypePrefixForObject(value, jgen);
    }
    
    public void writeTypePrefixForArray(final Object value, final JsonGenerator jgen, final Class<?> type) throws IOException, JsonProcessingException {
        this.writeTypePrefixForArray(value, jgen);
    }
}
