// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import java.lang.reflect.Method;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.util.EnumResolver;
import org.codehaus.jackson.map.annotate.JsonCachable;

@JsonCachable
public class EnumDeserializer extends StdScalarDeserializer<Enum<?>>
{
    protected final EnumResolver<?> _resolver;
    
    public EnumDeserializer(final EnumResolver<?> res) {
        super(Enum.class);
        this._resolver = res;
    }
    
    public static JsonDeserializer<?> deserializerForCreator(final DeserializationConfig config, final Class<?> enumClass, final AnnotatedMethod factory) {
        Class<?> raw = factory.getParameterClass(0);
        if (raw == String.class) {
            raw = null;
        }
        else if (raw == Integer.TYPE || raw == Integer.class) {
            raw = Integer.class;
        }
        else {
            if (raw != Long.TYPE && raw != Long.class) {
                throw new IllegalArgumentException("Parameter #0 type for factory method (" + factory + ") not suitable, must be java.lang.String or int/Integer/long/Long");
            }
            raw = Long.class;
        }
        if (config.isEnabled(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
            ClassUtil.checkAndFixAccess(factory.getMember());
        }
        return new FactoryBasedDeserializer(enumClass, factory, raw);
    }
    
    @Override
    public Enum<?> deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonToken curr = jp.getCurrentToken();
        if (curr == JsonToken.VALUE_STRING || curr == JsonToken.FIELD_NAME) {
            final String name = jp.getText();
            final Enum<?> result = (Enum<?>)this._resolver.findEnum(name);
            if (result == null) {
                throw ctxt.weirdStringException(this._resolver.getEnumClass(), "value not one of declared Enum instance names");
            }
            return result;
        }
        else {
            if (curr != JsonToken.VALUE_NUMBER_INT) {
                throw ctxt.mappingException(this._resolver.getEnumClass());
            }
            if (ctxt.isEnabled(DeserializationConfig.Feature.FAIL_ON_NUMBERS_FOR_ENUMS)) {
                throw ctxt.mappingException("Not allowed to deserialize Enum value out of JSON number (disable DeserializationConfig.Feature.FAIL_ON_NUMBERS_FOR_ENUMS to allow)");
            }
            final int index = jp.getIntValue();
            final Enum<?> result = (Enum<?>)this._resolver.getEnum(index);
            if (result == null) {
                throw ctxt.weirdNumberException(this._resolver.getEnumClass(), "index value outside legal index range [0.." + this._resolver.lastValidIndex() + "]");
            }
            return result;
        }
    }
    
    protected static class FactoryBasedDeserializer extends StdScalarDeserializer<Object>
    {
        protected final Class<?> _enumClass;
        protected final Class<?> _inputType;
        protected final Method _factory;
        
        public FactoryBasedDeserializer(final Class<?> cls, final AnnotatedMethod f, final Class<?> inputType) {
            super(Enum.class);
            this._enumClass = cls;
            this._factory = f.getAnnotated();
            this._inputType = inputType;
        }
        
        @Override
        public Object deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            Object value;
            if (this._inputType == null) {
                value = jp.getText();
            }
            else if (this._inputType == Integer.class) {
                value = jp.getValueAsInt();
            }
            else {
                if (this._inputType != Long.class) {
                    throw ctxt.mappingException(this._enumClass);
                }
                value = jp.getValueAsLong();
            }
            try {
                return this._factory.invoke(this._enumClass, value);
            }
            catch (final Exception e) {
                ClassUtil.unwrapAndThrowAsIAE(e);
                return null;
            }
        }
    }
}
