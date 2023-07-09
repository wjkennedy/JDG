// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonNode;
import java.lang.reflect.Type;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.util.TokenBuffer;

@JacksonStdImpl
public class TokenBufferSerializer extends SerializerBase<TokenBuffer>
{
    public TokenBufferSerializer() {
        super(TokenBuffer.class);
    }
    
    @Override
    public void serialize(final TokenBuffer value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        value.serialize(jgen);
    }
    
    @Override
    public final void serializeWithType(final TokenBuffer value, final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonGenerationException {
        typeSer.writeTypePrefixForScalar(value, jgen);
        this.serialize(value, jgen, provider);
        typeSer.writeTypeSuffixForScalar(value, jgen);
    }
    
    @Override
    public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
        return this.createSchemaNode("any", true);
    }
}
