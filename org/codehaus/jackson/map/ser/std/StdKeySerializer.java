// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.JsonNode;
import java.lang.reflect.Type;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import java.util.Date;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;

public class StdKeySerializer extends SerializerBase<Object>
{
    static final StdKeySerializer instace;
    
    public StdKeySerializer() {
        super(Object.class);
    }
    
    @Override
    public void serialize(final Object value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        if (value instanceof Date) {
            provider.defaultSerializeDateKey((Date)value, jgen);
        }
        else {
            jgen.writeFieldName(value.toString());
        }
    }
    
    @Override
    public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) throws JsonMappingException {
        return this.createSchemaNode("string");
    }
    
    static {
        instace = new StdKeySerializer();
    }
}
