// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;

public abstract class JsonSerializer<T>
{
    public JsonSerializer<T> unwrappingSerializer() {
        return this;
    }
    
    public boolean isUnwrappingSerializer() {
        return false;
    }
    
    public abstract void serialize(final T p0, final JsonGenerator p1, final SerializerProvider p2) throws IOException, JsonProcessingException;
    
    public void serializeWithType(final T value, final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonProcessingException {
        this.serialize(value, jgen, provider);
    }
    
    public Class<T> handledType() {
        return null;
    }
    
    public abstract static class None extends JsonSerializer<Object>
    {
    }
}
