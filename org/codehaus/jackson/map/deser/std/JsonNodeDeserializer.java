// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.JsonNode;

public class JsonNodeDeserializer extends BaseNodeDeserializer<JsonNode>
{
    private static final JsonNodeDeserializer instance;
    
    protected JsonNodeDeserializer() {
        super(JsonNode.class);
    }
    
    public static JsonDeserializer<? extends JsonNode> getDeserializer(final Class<?> nodeClass) {
        if (nodeClass == ObjectNode.class) {
            return ObjectDeserializer.getInstance();
        }
        if (nodeClass == ArrayNode.class) {
            return ArrayDeserializer.getInstance();
        }
        return JsonNodeDeserializer.instance;
    }
    
    @Override
    public JsonNode deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        switch (jp.getCurrentToken()) {
            case START_OBJECT: {
                return this.deserializeObject(jp, ctxt, ctxt.getNodeFactory());
            }
            case START_ARRAY: {
                return this.deserializeArray(jp, ctxt, ctxt.getNodeFactory());
            }
            default: {
                return this.deserializeAny(jp, ctxt, ctxt.getNodeFactory());
            }
        }
    }
    
    static {
        instance = new JsonNodeDeserializer();
    }
    
    static final class ObjectDeserializer extends BaseNodeDeserializer<ObjectNode>
    {
        protected static final ObjectDeserializer _instance;
        
        protected ObjectDeserializer() {
            super(ObjectNode.class);
        }
        
        public static ObjectDeserializer getInstance() {
            return ObjectDeserializer._instance;
        }
        
        @Override
        public ObjectNode deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
                jp.nextToken();
                return this.deserializeObject(jp, ctxt, ctxt.getNodeFactory());
            }
            if (jp.getCurrentToken() == JsonToken.FIELD_NAME) {
                return this.deserializeObject(jp, ctxt, ctxt.getNodeFactory());
            }
            throw ctxt.mappingException(ObjectNode.class);
        }
        
        static {
            _instance = new ObjectDeserializer();
        }
    }
    
    static final class ArrayDeserializer extends BaseNodeDeserializer<ArrayNode>
    {
        protected static final ArrayDeserializer _instance;
        
        protected ArrayDeserializer() {
            super(ArrayNode.class);
        }
        
        public static ArrayDeserializer getInstance() {
            return ArrayDeserializer._instance;
        }
        
        @Override
        public ArrayNode deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (jp.isExpectedStartArrayToken()) {
                return this.deserializeArray(jp, ctxt, ctxt.getNodeFactory());
            }
            throw ctxt.mappingException(ArrayNode.class);
        }
        
        static {
            _instance = new ArrayDeserializer();
        }
    }
}
