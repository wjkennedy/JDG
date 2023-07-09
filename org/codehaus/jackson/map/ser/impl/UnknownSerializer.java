// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.impl;

import org.codehaus.jackson.JsonNode;
import java.lang.reflect.Type;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.JsonMappingException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ser.std.SerializerBase;

public class UnknownSerializer extends SerializerBase<Object>
{
    public UnknownSerializer() {
        super(Object.class);
    }
    
    @Override
    public void serialize(final Object value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonMappingException {
        if (provider.isEnabled(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS)) {
            this.failForEmpty(value);
        }
        jgen.writeStartObject();
        jgen.writeEndObject();
    }
    
    @Override
    public final void serializeWithType(final Object value, final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonGenerationException {
        if (provider.isEnabled(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS)) {
            this.failForEmpty(value);
        }
        typeSer.writeTypePrefixForObject(value, jgen);
        typeSer.writeTypeSuffixForObject(value, jgen);
    }
    
    @Override
    public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) throws JsonMappingException {
        return null;
    }
    
    protected void failForEmpty(final Object value) throws JsonMappingException {
        throw new JsonMappingException("No serializer found for class " + value.getClass().getName() + " and no properties discovered to create BeanSerializer (to avoid exception, disable SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS) )");
    }
}
