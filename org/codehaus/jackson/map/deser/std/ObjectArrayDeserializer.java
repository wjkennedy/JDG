// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import java.lang.reflect.Array;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.util.ObjectBuffer;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;

@JacksonStdImpl
public class ObjectArrayDeserializer extends ContainerDeserializerBase<Object[]>
{
    protected final JavaType _arrayType;
    protected final boolean _untyped;
    protected final Class<?> _elementClass;
    protected final JsonDeserializer<Object> _elementDeserializer;
    protected final TypeDeserializer _elementTypeDeserializer;
    
    public ObjectArrayDeserializer(final ArrayType arrayType, final JsonDeserializer<Object> elemDeser, final TypeDeserializer elemTypeDeser) {
        super(Object[].class);
        this._arrayType = arrayType;
        this._elementClass = arrayType.getContentType().getRawClass();
        this._untyped = (this._elementClass == Object.class);
        this._elementDeserializer = elemDeser;
        this._elementTypeDeserializer = elemTypeDeser;
    }
    
    @Override
    public JavaType getContentType() {
        return this._arrayType.getContentType();
    }
    
    @Override
    public JsonDeserializer<Object> getContentDeserializer() {
        return this._elementDeserializer;
    }
    
    @Override
    public Object[] deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (!jp.isExpectedStartArrayToken()) {
            return this.handleNonArray(jp, ctxt);
        }
        final ObjectBuffer buffer = ctxt.leaseObjectBuffer();
        Object[] chunk = buffer.resetAndStart();
        int ix = 0;
        final TypeDeserializer typeDeser = this._elementTypeDeserializer;
        JsonToken t;
        while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
            Object value;
            if (t == JsonToken.VALUE_NULL) {
                value = null;
            }
            else if (typeDeser == null) {
                value = this._elementDeserializer.deserialize(jp, ctxt);
            }
            else {
                value = this._elementDeserializer.deserializeWithType(jp, ctxt, typeDeser);
            }
            if (ix >= chunk.length) {
                chunk = buffer.appendCompletedChunk(chunk);
                ix = 0;
            }
            chunk[ix++] = value;
        }
        Object[] result;
        if (this._untyped) {
            result = buffer.completeAndClearBuffer(chunk, ix);
        }
        else {
            result = buffer.completeAndClearBuffer(chunk, ix, this._elementClass);
        }
        ctxt.returnObjectBuffer(buffer);
        return result;
    }
    
    @Override
    public Object[] deserializeWithType(final JsonParser jp, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
        return (Object[])typeDeserializer.deserializeTypedFromArray(jp, ctxt);
    }
    
    protected Byte[] deserializeFromBase64(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final byte[] b = jp.getBinaryValue(ctxt.getBase64Variant());
        final Byte[] result = new Byte[b.length];
        for (int i = 0, len = b.length; i < len; ++i) {
            result[i] = b[i];
        }
        return result;
    }
    
    private final Object[] handleNonArray(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (jp.getCurrentToken() == JsonToken.VALUE_STRING && ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)) {
            final String str = jp.getText();
            if (str.length() == 0) {
                return null;
            }
        }
        if (ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
            final JsonToken t = jp.getCurrentToken();
            Object value;
            if (t == JsonToken.VALUE_NULL) {
                value = null;
            }
            else if (this._elementTypeDeserializer == null) {
                value = this._elementDeserializer.deserialize(jp, ctxt);
            }
            else {
                value = this._elementDeserializer.deserializeWithType(jp, ctxt, this._elementTypeDeserializer);
            }
            Object[] result;
            if (this._untyped) {
                result = new Object[] { null };
            }
            else {
                result = (Object[])Array.newInstance(this._elementClass, 1);
            }
            result[0] = value;
            return result;
        }
        if (jp.getCurrentToken() == JsonToken.VALUE_STRING && this._elementClass == Byte.class) {
            return this.deserializeFromBase64(jp, ctxt);
        }
        throw ctxt.mappingException(this._arrayType.getRawClass());
    }
}
