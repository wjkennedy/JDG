// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.JsonNode;
import java.lang.reflect.Type;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;

@JacksonStdImpl
public final class StringSerializer extends NonTypedScalarSerializerBase<String>
{
    public StringSerializer() {
        super(String.class);
    }
    
    @Override
    public void serialize(final String value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        jgen.writeString(value);
    }
    
    @Override
    public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
        return this.createSchemaNode("string", true);
    }
}
