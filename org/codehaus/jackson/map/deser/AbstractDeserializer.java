// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser;

import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.JsonDeserializer;

public class AbstractDeserializer extends JsonDeserializer<Object>
{
    protected final JavaType _baseType;
    protected final boolean _acceptString;
    protected final boolean _acceptBoolean;
    protected final boolean _acceptInt;
    protected final boolean _acceptDouble;
    
    public AbstractDeserializer(final JavaType bt) {
        this._baseType = bt;
        final Class<?> cls = bt.getRawClass();
        this._acceptString = cls.isAssignableFrom(String.class);
        this._acceptBoolean = (cls == Boolean.TYPE || cls.isAssignableFrom(Boolean.class));
        this._acceptInt = (cls == Integer.TYPE || cls.isAssignableFrom(Integer.class));
        this._acceptDouble = (cls == Double.TYPE || cls.isAssignableFrom(Double.class));
    }
    
    @Override
    public Object deserializeWithType(final JsonParser jp, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
        final Object result = this._deserializeIfNatural(jp, ctxt);
        if (result != null) {
            return result;
        }
        return typeDeserializer.deserializeTypedFromObject(jp, ctxt);
    }
    
    @Override
    public Object deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        throw ctxt.instantiationException(this._baseType.getRawClass(), "abstract types can only be instantiated with additional type information");
    }
    
    protected Object _deserializeIfNatural(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        switch (jp.getCurrentToken()) {
            case VALUE_STRING: {
                if (this._acceptString) {
                    return jp.getText();
                }
                break;
            }
            case VALUE_NUMBER_INT: {
                if (this._acceptInt) {
                    return jp.getIntValue();
                }
                break;
            }
            case VALUE_NUMBER_FLOAT: {
                if (this._acceptDouble) {
                    return jp.getDoubleValue();
                }
                break;
            }
            case VALUE_TRUE: {
                if (this._acceptBoolean) {
                    return Boolean.TRUE;
                }
                break;
            }
            case VALUE_FALSE: {
                if (this._acceptBoolean) {
                    return Boolean.FALSE;
                }
                break;
            }
        }
        return null;
    }
}
