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

public class AsArrayTypeDeserializer extends TypeDeserializerBase
{
    @Deprecated
    public AsArrayTypeDeserializer(final JavaType bt, final TypeIdResolver idRes, final BeanProperty property) {
        this(bt, idRes, property, null);
    }
    
    public AsArrayTypeDeserializer(final JavaType bt, final TypeIdResolver idRes, final BeanProperty property, final Class<?> defaultImpl) {
        super(bt, idRes, property, defaultImpl);
    }
    
    @Override
    public JsonTypeInfo.As getTypeInclusion() {
        return JsonTypeInfo.As.WRAPPER_ARRAY;
    }
    
    @Override
    public Object deserializeTypedFromArray(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return this._deserialize(jp, ctxt);
    }
    
    @Override
    public Object deserializeTypedFromObject(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
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
        final boolean hadStartArray = jp.isExpectedStartArrayToken();
        final JsonDeserializer<Object> deser = this._findDeserializer(ctxt, this._locateTypeId(jp, ctxt));
        final Object value = deser.deserialize(jp, ctxt);
        if (hadStartArray && jp.nextToken() != JsonToken.END_ARRAY) {
            throw ctxt.wrongTokenException(jp, JsonToken.END_ARRAY, "expected closing END_ARRAY after type information and deserialized value");
        }
        return value;
    }
    
    protected final String _locateTypeId(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (!jp.isExpectedStartArrayToken()) {
            if (this._idResolver instanceof TypeIdResolverBase && this._defaultImpl != null) {
                return ((TypeIdResolverBase)this._idResolver).idFromBaseType();
            }
            throw ctxt.wrongTokenException(jp, JsonToken.START_ARRAY, "need JSON Array to contain As.WRAPPER_ARRAY type information for class " + this.baseTypeName());
        }
        else {
            if (jp.nextToken() == JsonToken.VALUE_STRING) {
                final String result = jp.getText();
                jp.nextToken();
                return result;
            }
            if (this._idResolver instanceof TypeIdResolverBase && this._defaultImpl != null) {
                return ((TypeIdResolverBase)this._idResolver).idFromBaseType();
            }
            throw ctxt.wrongTokenException(jp, JsonToken.VALUE_STRING, "need JSON String that contains type id (for subtype of " + this.baseTypeName() + ")");
        }
    }
}
