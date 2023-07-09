// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.JsonNode;
import java.lang.reflect.Type;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;

@JacksonStdImpl
public class ToStringSerializer extends SerializerBase<Object>
{
    public static final ToStringSerializer instance;
    
    public ToStringSerializer() {
        super(Object.class);
    }
    
    @Override
    public void serialize(final Object value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        jgen.writeString(value.toString());
    }
    
    @Override
    public void serializeWithType(final Object value, final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonGenerationException {
        typeSer.writeTypePrefixForScalar(value, jgen);
        this.serialize(value, jgen, provider);
        typeSer.writeTypeSuffixForScalar(value, jgen);
    }
    
    @Override
    public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) throws JsonMappingException {
        return this.createSchemaNode("string", true);
    }
    
    static {
        instance = new ToStringSerializer();
    }
}
