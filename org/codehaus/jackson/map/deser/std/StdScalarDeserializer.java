// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.type.JavaType;

public abstract class StdScalarDeserializer<T> extends StdDeserializer<T>
{
    protected StdScalarDeserializer(final Class<?> vc) {
        super(vc);
    }
    
    protected StdScalarDeserializer(final JavaType valueType) {
        super(valueType);
    }
    
    @Override
    public Object deserializeWithType(final JsonParser jp, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
        return typeDeserializer.deserializeTypedFromScalar(jp, ctxt);
    }
}
