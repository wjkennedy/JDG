// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.util.EnumResolver;
import org.codehaus.jackson.map.JsonDeserializer;
import java.util.EnumMap;

public class EnumMapDeserializer extends StdDeserializer<EnumMap<?, ?>>
{
    protected final Class<?> _enumClass;
    protected final JsonDeserializer<Enum<?>> _keyDeserializer;
    protected final JsonDeserializer<Object> _valueDeserializer;
    
    @Deprecated
    public EnumMapDeserializer(final EnumResolver<?> enumRes, final JsonDeserializer<Object> valueDeser) {
        this(enumRes.getEnumClass(), new EnumDeserializer(enumRes), valueDeser);
    }
    
    public EnumMapDeserializer(final Class<?> enumClass, final JsonDeserializer<?> keyDeserializer, final JsonDeserializer<Object> valueDeser) {
        super(EnumMap.class);
        this._enumClass = enumClass;
        this._keyDeserializer = (JsonDeserializer<Enum<?>>)keyDeserializer;
        this._valueDeserializer = valueDeser;
    }
    
    @Override
    public EnumMap<?, ?> deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
            throw ctxt.mappingException(EnumMap.class);
        }
        final EnumMap result = this.constructMap();
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            final Enum<?> key = this._keyDeserializer.deserialize(jp, ctxt);
            if (key == null) {
                throw ctxt.weirdStringException(this._enumClass, "value not one of declared Enum instance names");
            }
            final JsonToken t = jp.nextToken();
            final Object value = (t == JsonToken.VALUE_NULL) ? null : this._valueDeserializer.deserialize(jp, ctxt);
            result.put(key, value);
        }
        return result;
    }
    
    @Override
    public Object deserializeWithType(final JsonParser jp, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
        return typeDeserializer.deserializeTypedFromObject(jp, ctxt);
    }
    
    private EnumMap<?, ?> constructMap() {
        return new EnumMap<Object, Object>(this._enumClass);
    }
}
