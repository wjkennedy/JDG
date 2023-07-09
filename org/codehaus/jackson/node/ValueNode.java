// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.node;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonToken;

public abstract class ValueNode extends BaseJsonNode
{
    protected ValueNode() {
    }
    
    @Override
    public boolean isValueNode() {
        return true;
    }
    
    @Override
    public abstract JsonToken asToken();
    
    @Override
    public void serializeWithType(final JsonGenerator jg, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonProcessingException {
        typeSer.writeTypePrefixForScalar(this, jg);
        this.serialize(jg, provider);
        typeSer.writeTypeSuffixForScalar(this, jg);
    }
    
    @Override
    public JsonNode path(final String fieldName) {
        return MissingNode.getInstance();
    }
    
    @Override
    public JsonNode path(final int index) {
        return MissingNode.getInstance();
    }
    
    @Override
    public String toString() {
        return this.asText();
    }
}
