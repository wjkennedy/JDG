// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.JsonParser;

public abstract class JsonDeserializer<T>
{
    public abstract T deserialize(final JsonParser p0, final DeserializationContext p1) throws IOException, JsonProcessingException;
    
    public T deserialize(final JsonParser jp, final DeserializationContext ctxt, final T intoValue) throws IOException, JsonProcessingException {
        throw new UnsupportedOperationException("Can not update object of type " + intoValue.getClass().getName() + " (by deserializer of type " + this.getClass().getName() + ")");
    }
    
    public Object deserializeWithType(final JsonParser jp, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
        return typeDeserializer.deserializeTypedFromAny(jp, ctxt);
    }
    
    public JsonDeserializer<T> unwrappingDeserializer() {
        return this;
    }
    
    public T getNullValue() {
        return null;
    }
    
    public T getEmptyValue() {
        return this.getNullValue();
    }
    
    public abstract static class None extends JsonDeserializer<Object>
    {
    }
}
