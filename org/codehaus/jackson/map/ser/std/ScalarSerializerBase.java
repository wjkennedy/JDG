// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.JsonNode;
import java.lang.reflect.Type;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;

public abstract class ScalarSerializerBase<T> extends SerializerBase<T>
{
    protected ScalarSerializerBase(final Class<T> t) {
        super(t);
    }
    
    protected ScalarSerializerBase(final Class<?> t, final boolean dummy) {
        super(t);
    }
    
    @Override
    public void serializeWithType(final T value, final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonGenerationException {
        typeSer.writeTypePrefixForScalar(value, jgen);
        this.serialize(value, jgen, provider);
        typeSer.writeTypeSuffixForScalar(value, jgen);
    }
    
    @Override
    public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) throws JsonMappingException {
        return this.createSchemaNode("string", true);
    }
}
