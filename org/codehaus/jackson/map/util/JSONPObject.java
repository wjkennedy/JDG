// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.util;

import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import java.lang.reflect.Type;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.JsonSerializableWithType;

public class JSONPObject implements JsonSerializableWithType
{
    protected final String _function;
    protected final Object _value;
    protected final JavaType _serializationType;
    
    public JSONPObject(final String function, final Object value) {
        this(function, value, (JavaType)null);
    }
    
    public JSONPObject(final String function, final Object value, final JavaType asType) {
        this._function = function;
        this._value = value;
        this._serializationType = asType;
    }
    
    @Deprecated
    public JSONPObject(final String function, final Object value, final Class<?> rawType) {
        this._function = function;
        this._value = value;
        this._serializationType = ((rawType == null) ? null : TypeFactory.defaultInstance().constructType(rawType));
    }
    
    public void serializeWithType(final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonProcessingException {
        this.serialize(jgen, provider);
    }
    
    public void serialize(final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeRaw(this._function);
        jgen.writeRaw('(');
        if (this._value == null) {
            provider.defaultSerializeNull(jgen);
        }
        else if (this._serializationType != null) {
            provider.findTypedValueSerializer(this._serializationType, true, null).serialize(this._value, jgen, provider);
        }
        else {
            final Class<?> cls = this._value.getClass();
            provider.findTypedValueSerializer(cls, true, null).serialize(this._value, jgen, provider);
        }
        jgen.writeRaw(')');
    }
    
    public String getFunction() {
        return this._function;
    }
    
    public Object getValue() {
        return this._value;
    }
    
    public JavaType getSerializationType() {
        return this._serializationType;
    }
}
