// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import java.util.concurrent.atomic.AtomicBoolean;

public class AtomicBooleanDeserializer extends StdScalarDeserializer<AtomicBoolean>
{
    public AtomicBooleanDeserializer() {
        super(AtomicBoolean.class);
    }
    
    @Override
    public AtomicBoolean deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return new AtomicBoolean(this._parseBooleanPrimitive(jp, ctxt));
    }
}
