// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.annotate.JsonTypeInfo;

public abstract class TypeDeserializer
{
    public abstract JsonTypeInfo.As getTypeInclusion();
    
    public abstract String getPropertyName();
    
    public abstract TypeIdResolver getTypeIdResolver();
    
    public abstract Class<?> getDefaultImpl();
    
    public abstract Object deserializeTypedFromObject(final JsonParser p0, final DeserializationContext p1) throws IOException, JsonProcessingException;
    
    public abstract Object deserializeTypedFromArray(final JsonParser p0, final DeserializationContext p1) throws IOException, JsonProcessingException;
    
    public abstract Object deserializeTypedFromScalar(final JsonParser p0, final DeserializationContext p1) throws IOException, JsonProcessingException;
    
    public abstract Object deserializeTypedFromAny(final JsonParser p0, final DeserializationContext p1) throws IOException, JsonProcessingException;
}
