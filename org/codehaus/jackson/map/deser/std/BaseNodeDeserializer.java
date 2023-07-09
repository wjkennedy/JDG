// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonNode;

abstract class BaseNodeDeserializer<N extends JsonNode> extends StdDeserializer<N>
{
    public BaseNodeDeserializer(final Class<N> nodeClass) {
        super(nodeClass);
    }
    
    @Override
    public Object deserializeWithType(final JsonParser jp, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
        return typeDeserializer.deserializeTypedFromAny(jp, ctxt);
    }
    
    protected void _reportProblem(final JsonParser jp, final String msg) throws JsonMappingException {
        throw new JsonMappingException(msg, jp.getTokenLocation());
    }
    
    protected void _handleDuplicateField(final String fieldName, final ObjectNode objectNode, final JsonNode oldValue, final JsonNode newValue) throws JsonProcessingException {
    }
    
    protected final ObjectNode deserializeObject(final JsonParser jp, final DeserializationContext ctxt, final JsonNodeFactory nodeFactory) throws IOException, JsonProcessingException {
        final ObjectNode node = nodeFactory.objectNode();
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.START_OBJECT) {
            t = jp.nextToken();
        }
        while (t == JsonToken.FIELD_NAME) {
            final String fieldName = jp.getCurrentName();
            JsonNode value = null;
            switch (jp.nextToken()) {
                case START_OBJECT: {
                    value = this.deserializeObject(jp, ctxt, nodeFactory);
                    break;
                }
                case START_ARRAY: {
                    value = this.deserializeArray(jp, ctxt, nodeFactory);
                    break;
                }
                case VALUE_STRING: {
                    value = nodeFactory.textNode(jp.getText());
                    break;
                }
                default: {
                    value = this.deserializeAny(jp, ctxt, nodeFactory);
                    break;
                }
            }
            final JsonNode old = node.put(fieldName, value);
            if (old != null) {
                this._handleDuplicateField(fieldName, node, old, value);
            }
            t = jp.nextToken();
        }
        return node;
    }
    
    protected final ArrayNode deserializeArray(final JsonParser jp, final DeserializationContext ctxt, final JsonNodeFactory nodeFactory) throws IOException, JsonProcessingException {
        final ArrayNode node = nodeFactory.arrayNode();
    Label_0078:
        while (true) {
            switch (jp.nextToken()) {
                case START_OBJECT: {
                    node.add(this.deserializeObject(jp, ctxt, nodeFactory));
                    continue;
                }
                case START_ARRAY: {
                    node.add(this.deserializeArray(jp, ctxt, nodeFactory));
                    continue;
                }
                case END_ARRAY: {
                    break Label_0078;
                }
                case VALUE_STRING: {
                    node.add(nodeFactory.textNode(jp.getText()));
                    continue;
                }
                default: {
                    node.add(this.deserializeAny(jp, ctxt, nodeFactory));
                    continue;
                }
            }
        }
        return node;
    }
    
    protected final JsonNode deserializeAny(final JsonParser jp, final DeserializationContext ctxt, final JsonNodeFactory nodeFactory) throws IOException, JsonProcessingException {
        switch (jp.getCurrentToken()) {
            case START_OBJECT: {
                return this.deserializeObject(jp, ctxt, nodeFactory);
            }
            case START_ARRAY: {
                return this.deserializeArray(jp, ctxt, nodeFactory);
            }
            case FIELD_NAME: {
                return this.deserializeObject(jp, ctxt, nodeFactory);
            }
            case VALUE_EMBEDDED_OBJECT: {
                final Object ob = jp.getEmbeddedObject();
                if (ob == null) {
                    return nodeFactory.nullNode();
                }
                final Class<?> type = ob.getClass();
                if (type == byte[].class) {
                    return nodeFactory.binaryNode((byte[])ob);
                }
                return nodeFactory.POJONode(ob);
            }
            case VALUE_STRING: {
                return nodeFactory.textNode(jp.getText());
            }
            case VALUE_NUMBER_INT: {
                final JsonParser.NumberType nt = jp.getNumberType();
                if (nt == JsonParser.NumberType.BIG_INTEGER || ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_INTEGER_FOR_INTS)) {
                    return nodeFactory.numberNode(jp.getBigIntegerValue());
                }
                if (nt == JsonParser.NumberType.INT) {
                    return nodeFactory.numberNode(jp.getIntValue());
                }
                return nodeFactory.numberNode(jp.getLongValue());
            }
            case VALUE_NUMBER_FLOAT: {
                final JsonParser.NumberType nt = jp.getNumberType();
                if (nt == JsonParser.NumberType.BIG_DECIMAL || ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS)) {
                    return nodeFactory.numberNode(jp.getDecimalValue());
                }
                return nodeFactory.numberNode(jp.getDoubleValue());
            }
            case VALUE_TRUE: {
                return nodeFactory.booleanNode(true);
            }
            case VALUE_FALSE: {
                return nodeFactory.booleanNode(false);
            }
            case VALUE_NULL: {
                return nodeFactory.nullNode();
            }
            default: {
                throw ctxt.mappingException(this.getValueClass());
            }
        }
    }
}
