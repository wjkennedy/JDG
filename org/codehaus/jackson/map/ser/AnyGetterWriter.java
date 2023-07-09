// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser;

import org.codehaus.jackson.map.JsonMappingException;
import java.util.Map;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.ser.std.MapSerializer;
import java.lang.reflect.Method;

public class AnyGetterWriter
{
    protected final Method _anyGetter;
    protected final MapSerializer _serializer;
    
    public AnyGetterWriter(final AnnotatedMethod anyGetter, final MapSerializer serializer) {
        this._anyGetter = anyGetter.getAnnotated();
        this._serializer = serializer;
    }
    
    public void getAndSerialize(final Object bean, final JsonGenerator jgen, final SerializerProvider provider) throws Exception {
        final Object value = this._anyGetter.invoke(bean, new Object[0]);
        if (value == null) {
            return;
        }
        if (!(value instanceof Map)) {
            throw new JsonMappingException("Value returned by 'any-getter' (" + this._anyGetter.getName() + "()) not java.util.Map but " + value.getClass().getName());
        }
        this._serializer.serializeFields((Map<?, ?>)value, jgen, provider);
    }
    
    public void resolve(final SerializerProvider provider) throws JsonMappingException {
        this._serializer.resolve(provider);
    }
}
