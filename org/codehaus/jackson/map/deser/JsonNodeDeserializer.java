// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;

@Deprecated
public class JsonNodeDeserializer extends org.codehaus.jackson.map.deser.std.JsonNodeDeserializer
{
    @Deprecated
    public static final JsonNodeDeserializer instance;
    
    @Deprecated
    protected final ObjectNode deserializeObject(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return this.deserializeObject(jp, ctxt, ctxt.getNodeFactory());
    }
    
    @Deprecated
    protected final ArrayNode deserializeArray(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return this.deserializeArray(jp, ctxt, ctxt.getNodeFactory());
    }
    
    @Deprecated
    protected final JsonNode deserializeAny(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return this.deserializeAny(jp, ctxt, ctxt.getNodeFactory());
    }
    
    static {
        instance = new JsonNodeDeserializer();
    }
}
