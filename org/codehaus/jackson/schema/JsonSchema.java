// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.schema;

import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonValue;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.node.ObjectNode;

public class JsonSchema
{
    private final ObjectNode schema;
    
    @JsonCreator
    public JsonSchema(final ObjectNode schema) {
        this.schema = schema;
    }
    
    @JsonValue
    public ObjectNode getSchemaNode() {
        return this.schema;
    }
    
    @Override
    public String toString() {
        return this.schema.toString();
    }
    
    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof JsonSchema)) {
            return false;
        }
        final JsonSchema other = (JsonSchema)o;
        if (this.schema == null) {
            return other.schema == null;
        }
        return this.schema.equals(other.schema);
    }
    
    public static JsonNode getDefaultSchemaNode() {
        final ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        objectNode.put("type", "any");
        return objectNode;
    }
}
