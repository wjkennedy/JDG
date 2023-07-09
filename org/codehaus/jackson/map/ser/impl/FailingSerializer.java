// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.impl;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.JsonNode;
import java.lang.reflect.Type;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ser.std.SerializerBase;

public final class FailingSerializer extends SerializerBase<Object>
{
    final String _msg;
    
    public FailingSerializer(final String msg) {
        super(Object.class);
        this._msg = msg;
    }
    
    @Override
    public void serialize(final Object value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        throw new JsonGenerationException(this._msg);
    }
    
    @Override
    public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) throws JsonMappingException {
        return null;
    }
}
