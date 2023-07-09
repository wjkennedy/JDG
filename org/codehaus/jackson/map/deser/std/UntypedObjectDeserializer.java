// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import java.util.LinkedHashMap;
import org.codehaus.jackson.map.util.ObjectBuffer;
import java.util.List;
import java.util.ArrayList;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;

@JacksonStdImpl
public class UntypedObjectDeserializer extends StdDeserializer<Object>
{
    private static final Object[] NO_OBJECTS;
    
    public UntypedObjectDeserializer() {
        super(Object.class);
    }
    
    @Override
    public Object deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        switch (jp.getCurrentToken()) {
            case START_OBJECT: {
                return this.mapObject(jp, ctxt);
            }
            case START_ARRAY: {
                return this.mapArray(jp, ctxt);
            }
            case FIELD_NAME: {
                return this.mapObject(jp, ctxt);
            }
            case VALUE_EMBEDDED_OBJECT: {
                return jp.getEmbeddedObject();
            }
            case VALUE_STRING: {
                return jp.getText();
            }
            case VALUE_NUMBER_INT: {
                if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_INTEGER_FOR_INTS)) {
                    return jp.getBigIntegerValue();
                }
                return jp.getNumberValue();
            }
            case VALUE_NUMBER_FLOAT: {
                if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS)) {
                    return jp.getDecimalValue();
                }
                return jp.getDoubleValue();
            }
            case VALUE_TRUE: {
                return Boolean.TRUE;
            }
            case VALUE_FALSE: {
                return Boolean.FALSE;
            }
            case VALUE_NULL: {
                return null;
            }
        }
        throw ctxt.mappingException(Object.class);
    }
    
    @Override
    public Object deserializeWithType(final JsonParser jp, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
        final JsonToken t = jp.getCurrentToken();
        switch (t) {
            case START_OBJECT:
            case START_ARRAY:
            case FIELD_NAME: {
                return typeDeserializer.deserializeTypedFromAny(jp, ctxt);
            }
            case VALUE_STRING: {
                return jp.getText();
            }
            case VALUE_NUMBER_INT: {
                if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_INTEGER_FOR_INTS)) {
                    return jp.getBigIntegerValue();
                }
                return jp.getIntValue();
            }
            case VALUE_NUMBER_FLOAT: {
                if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS)) {
                    return jp.getDecimalValue();
                }
                return jp.getDoubleValue();
            }
            case VALUE_TRUE: {
                return Boolean.TRUE;
            }
            case VALUE_FALSE: {
                return Boolean.FALSE;
            }
            case VALUE_EMBEDDED_OBJECT: {
                return jp.getEmbeddedObject();
            }
            case VALUE_NULL: {
                return null;
            }
            default: {
                throw ctxt.mappingException(Object.class);
            }
        }
    }
    
    protected Object mapArray(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (ctxt.isEnabled(DeserializationConfig.Feature.USE_JAVA_ARRAY_FOR_JSON_ARRAY)) {
            return this.mapArrayToArray(jp, ctxt);
        }
        if (jp.nextToken() == JsonToken.END_ARRAY) {
            return new ArrayList(4);
        }
        final ObjectBuffer buffer = ctxt.leaseObjectBuffer();
        Object[] values = buffer.resetAndStart();
        int ptr = 0;
        int totalSize = 0;
        do {
            final Object value = this.deserialize(jp, ctxt);
            ++totalSize;
            if (ptr >= values.length) {
                values = buffer.appendCompletedChunk(values);
                ptr = 0;
            }
            values[ptr++] = value;
        } while (jp.nextToken() != JsonToken.END_ARRAY);
        final ArrayList<Object> result = new ArrayList<Object>(totalSize + (totalSize >> 3) + 1);
        buffer.completeAndClearBuffer(values, ptr, result);
        return result;
    }
    
    protected Object mapObject(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.START_OBJECT) {
            t = jp.nextToken();
        }
        if (t != JsonToken.FIELD_NAME) {
            return new LinkedHashMap(4);
        }
        final String field1 = jp.getText();
        jp.nextToken();
        final Object value1 = this.deserialize(jp, ctxt);
        if (jp.nextToken() != JsonToken.FIELD_NAME) {
            final LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>(4);
            result.put(field1, value1);
            return result;
        }
        final String field2 = jp.getText();
        jp.nextToken();
        final Object value2 = this.deserialize(jp, ctxt);
        if (jp.nextToken() != JsonToken.FIELD_NAME) {
            final LinkedHashMap<String, Object> result2 = new LinkedHashMap<String, Object>(4);
            result2.put(field1, value1);
            result2.put(field2, value2);
            return result2;
        }
        final LinkedHashMap<String, Object> result2 = new LinkedHashMap<String, Object>();
        result2.put(field1, value1);
        result2.put(field2, value2);
        do {
            final String fieldName = jp.getText();
            jp.nextToken();
            result2.put(fieldName, this.deserialize(jp, ctxt));
        } while (jp.nextToken() != JsonToken.END_OBJECT);
        return result2;
    }
    
    protected Object[] mapArrayToArray(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (jp.nextToken() == JsonToken.END_ARRAY) {
            return UntypedObjectDeserializer.NO_OBJECTS;
        }
        final ObjectBuffer buffer = ctxt.leaseObjectBuffer();
        Object[] values = buffer.resetAndStart();
        int ptr = 0;
        do {
            final Object value = this.deserialize(jp, ctxt);
            if (ptr >= values.length) {
                values = buffer.appendCompletedChunk(values);
                ptr = 0;
            }
            values[ptr++] = value;
        } while (jp.nextToken() != JsonToken.END_ARRAY);
        return buffer.completeAndClearBuffer(values, ptr);
    }
    
    static {
        NO_OBJECTS = new Object[0];
    }
}
