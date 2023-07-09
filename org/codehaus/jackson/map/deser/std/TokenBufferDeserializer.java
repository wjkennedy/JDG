// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.util.TokenBuffer;

@JacksonStdImpl
public class TokenBufferDeserializer extends StdScalarDeserializer<TokenBuffer>
{
    public TokenBufferDeserializer() {
        super(TokenBuffer.class);
    }
    
    @Override
    public TokenBuffer deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final TokenBuffer tb = new TokenBuffer(jp.getCodec());
        tb.copyCurrentStructure(jp);
        return tb;
    }
}
