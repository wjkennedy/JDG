// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.jsontype.impl;

import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.type.JavaType;

public class AsWrapperTypeDeserializer extends TypeDeserializerBase
{
    @Deprecated
    public AsWrapperTypeDeserializer(final JavaType bt, final TypeIdResolver idRes, final BeanProperty property) {
        this(bt, idRes, property, null);
    }
    
    public AsWrapperTypeDeserializer(final JavaType bt, final TypeIdResolver idRes, final BeanProperty property, final Class<?> defaultImpl) {
        super(bt, idRes, property, null);
    }
    
    @Override
    public JsonTypeInfo.As getTypeInclusion() {
        return JsonTypeInfo.As.WRAPPER_OBJECT;
    }
    
    @Override
    public Object deserializeTypedFromObject(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return this._deserialize(jp, ctxt);
    }
    
    @Override
    public Object deserializeTypedFromArray(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return this._deserialize(jp, ctxt);
    }
    
    @Override
    public Object deserializeTypedFromScalar(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return this._deserialize(jp, ctxt);
    }
    
    @Override
    public Object deserializeTypedFromAny(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return this._deserialize(jp, ctxt);
    }
    
    private final Object _deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
            throw ctxt.wrongTokenException(jp, JsonToken.START_OBJECT, "need JSON Object to contain As.WRAPPER_OBJECT type information for class " + this.baseTypeName());
        }
        if (jp.nextToken() != JsonToken.FIELD_NAME) {
            throw ctxt.wrongTokenException(jp, JsonToken.FIELD_NAME, "need JSON String that contains type id (for subtype of " + this.baseTypeName() + ")");
        }
        final JsonDeserializer<Object> deser = this._findDeserializer(ctxt, jp.getText());
        jp.nextToken();
        final Object value = deser.deserialize(jp, ctxt);
        if (jp.nextToken() != JsonToken.END_OBJECT) {
            throw ctxt.wrongTokenException(jp, JsonToken.END_OBJECT, "expected closing END_OBJECT after type information and deserialized value");
        }
        return value;
    }
}
