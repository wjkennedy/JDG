// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.node;

import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.JsonParser;
import java.util.List;
import org.codehaus.jackson.map.JsonSerializableWithType;
import org.codehaus.jackson.JsonNode;

public abstract class BaseJsonNode extends JsonNode implements JsonSerializableWithType
{
    protected BaseJsonNode() {
    }
    
    @Override
    public JsonNode findValue(final String fieldName) {
        return null;
    }
    
    @Override
    public final JsonNode findPath(final String fieldName) {
        final JsonNode value = this.findValue(fieldName);
        if (value == null) {
            return MissingNode.getInstance();
        }
        return value;
    }
    
    @Override
    public ObjectNode findParent(final String fieldName) {
        return null;
    }
    
    @Override
    public List<JsonNode> findValues(final String fieldName, final List<JsonNode> foundSoFar) {
        return foundSoFar;
    }
    
    @Override
    public List<String> findValuesAsText(final String fieldName, final List<String> foundSoFar) {
        return foundSoFar;
    }
    
    @Override
    public List<JsonNode> findParents(final String fieldName, final List<JsonNode> foundSoFar) {
        return foundSoFar;
    }
    
    @Override
    public JsonParser traverse() {
        return new TreeTraversingParser(this);
    }
    
    @Override
    public abstract JsonToken asToken();
    
    @Override
    public JsonParser.NumberType getNumberType() {
        return null;
    }
    
    public abstract void serialize(final JsonGenerator p0, final SerializerProvider p1) throws IOException, JsonProcessingException;
    
    public abstract void serializeWithType(final JsonGenerator p0, final SerializerProvider p1, final TypeSerializer p2) throws IOException, JsonProcessingException;
}
