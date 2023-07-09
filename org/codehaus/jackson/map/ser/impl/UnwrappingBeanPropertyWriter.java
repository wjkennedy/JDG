// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.impl;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ser.BeanPropertyWriter;

public class UnwrappingBeanPropertyWriter extends BeanPropertyWriter
{
    public UnwrappingBeanPropertyWriter(final BeanPropertyWriter base) {
        super(base);
    }
    
    public UnwrappingBeanPropertyWriter(final BeanPropertyWriter base, final JsonSerializer<Object> ser) {
        super(base, ser);
    }
    
    @Override
    public BeanPropertyWriter withSerializer(JsonSerializer<Object> ser) {
        if (this.getClass() != UnwrappingBeanPropertyWriter.class) {
            throw new IllegalStateException("UnwrappingBeanPropertyWriter sub-class does not override 'withSerializer()'; needs to!");
        }
        if (!ser.isUnwrappingSerializer()) {
            ser = ser.unwrappingSerializer();
        }
        return new UnwrappingBeanPropertyWriter(this, ser);
    }
    
    @Override
    public void serializeAsField(final Object bean, final JsonGenerator jgen, final SerializerProvider prov) throws Exception {
        final Object value = this.get(bean);
        if (value == null) {
            return;
        }
        if (value == bean) {
            this._reportSelfReference(bean);
        }
        if (this._suppressableValue != null && this._suppressableValue.equals(value)) {
            return;
        }
        JsonSerializer<Object> ser = this._serializer;
        if (ser == null) {
            final Class<?> cls = value.getClass();
            final PropertySerializerMap map = this._dynamicSerializers;
            ser = map.serializerFor(cls);
            if (ser == null) {
                ser = this._findAndAddDynamic(map, cls, prov);
            }
        }
        if (!ser.isUnwrappingSerializer()) {
            jgen.writeFieldName(this._name);
        }
        if (this._typeSerializer == null) {
            ser.serialize(value, jgen, prov);
        }
        else {
            ser.serializeWithType(value, jgen, prov, this._typeSerializer);
        }
    }
    
    @Override
    protected JsonSerializer<Object> _findAndAddDynamic(final PropertySerializerMap map, final Class<?> type, final SerializerProvider provider) throws JsonMappingException {
        JsonSerializer<Object> serializer;
        if (this._nonTrivialBaseType != null) {
            final JavaType subtype = provider.constructSpecializedType(this._nonTrivialBaseType, type);
            serializer = provider.findValueSerializer(subtype, this);
        }
        else {
            serializer = provider.findValueSerializer(type, this);
        }
        if (!serializer.isUnwrappingSerializer()) {
            serializer = serializer.unwrappingSerializer();
        }
        this._dynamicSerializers = this._dynamicSerializers.newWith(type, serializer);
        return serializer;
    }
}
