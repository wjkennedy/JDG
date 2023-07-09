// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.map.type.TypeFactory;
import java.util.Date;
import org.codehaus.jackson.JsonProcessingException;
import java.lang.reflect.Type;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.schema.JsonSchema;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.type.JavaType;

public abstract class SerializerProvider
{
    protected static final JavaType TYPE_OBJECT;
    protected final SerializationConfig _config;
    protected final Class<?> _serializationView;
    
    protected SerializerProvider(final SerializationConfig config) {
        this._config = config;
        this._serializationView = ((config == null) ? null : this._config.getSerializationView());
    }
    
    public abstract void setNullKeySerializer(final JsonSerializer<Object> p0);
    
    public abstract void setNullValueSerializer(final JsonSerializer<Object> p0);
    
    public abstract void setDefaultKeySerializer(final JsonSerializer<Object> p0);
    
    public abstract void serializeValue(final SerializationConfig p0, final JsonGenerator p1, final Object p2, final SerializerFactory p3) throws IOException, JsonGenerationException;
    
    public abstract void serializeValue(final SerializationConfig p0, final JsonGenerator p1, final Object p2, final JavaType p3, final SerializerFactory p4) throws IOException, JsonGenerationException;
    
    public abstract JsonSchema generateJsonSchema(final Class<?> p0, final SerializationConfig p1, final SerializerFactory p2) throws JsonMappingException;
    
    public abstract boolean hasSerializerFor(final SerializationConfig p0, final Class<?> p1, final SerializerFactory p2);
    
    public final SerializationConfig getConfig() {
        return this._config;
    }
    
    public final boolean isEnabled(final SerializationConfig.Feature feature) {
        return this._config.isEnabled(feature);
    }
    
    public final Class<?> getSerializationView() {
        return this._serializationView;
    }
    
    public final FilterProvider getFilterProvider() {
        return this._config.getFilterProvider();
    }
    
    public JavaType constructType(final Type type) {
        return this._config.getTypeFactory().constructType(type);
    }
    
    public JavaType constructSpecializedType(final JavaType baseType, final Class<?> subclass) {
        return this._config.constructSpecializedType(baseType, subclass);
    }
    
    public abstract JsonSerializer<Object> findValueSerializer(final Class<?> p0, final BeanProperty p1) throws JsonMappingException;
    
    public abstract JsonSerializer<Object> findValueSerializer(final JavaType p0, final BeanProperty p1) throws JsonMappingException;
    
    public abstract JsonSerializer<Object> findTypedValueSerializer(final Class<?> p0, final boolean p1, final BeanProperty p2) throws JsonMappingException;
    
    public abstract JsonSerializer<Object> findTypedValueSerializer(final JavaType p0, final boolean p1, final BeanProperty p2) throws JsonMappingException;
    
    public abstract JsonSerializer<Object> findKeySerializer(final JavaType p0, final BeanProperty p1) throws JsonMappingException;
    
    @Deprecated
    public final JsonSerializer<Object> findValueSerializer(final Class<?> runtimeType) throws JsonMappingException {
        return this.findValueSerializer(runtimeType, null);
    }
    
    @Deprecated
    public final JsonSerializer<Object> findValueSerializer(final JavaType serializationType) throws JsonMappingException {
        return this.findValueSerializer(serializationType, null);
    }
    
    @Deprecated
    public final JsonSerializer<Object> findTypedValueSerializer(final Class<?> valueType, final boolean cache) throws JsonMappingException {
        return this.findTypedValueSerializer(valueType, cache, null);
    }
    
    @Deprecated
    public final JsonSerializer<Object> findTypedValueSerializer(final JavaType valueType, final boolean cache) throws JsonMappingException {
        return this.findTypedValueSerializer(valueType, cache, null);
    }
    
    @Deprecated
    public final JsonSerializer<Object> getKeySerializer() throws JsonMappingException {
        return this.findKeySerializer(SerializerProvider.TYPE_OBJECT, null);
    }
    
    @Deprecated
    public final JsonSerializer<Object> getKeySerializer(final JavaType valueType, final BeanProperty property) throws JsonMappingException {
        return this.findKeySerializer(valueType, property);
    }
    
    public abstract JsonSerializer<Object> getNullKeySerializer();
    
    public abstract JsonSerializer<Object> getNullValueSerializer();
    
    public abstract JsonSerializer<Object> getUnknownTypeSerializer(final Class<?> p0);
    
    public final void defaultSerializeValue(final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        if (value == null) {
            this.getNullValueSerializer().serialize(null, jgen, this);
        }
        else {
            final Class<?> cls = value.getClass();
            this.findTypedValueSerializer(cls, true, null).serialize(value, jgen, this);
        }
    }
    
    public final void defaultSerializeField(final String fieldName, final Object value, final JsonGenerator jgen) throws IOException, JsonProcessingException {
        jgen.writeFieldName(fieldName);
        if (value == null) {
            this.getNullValueSerializer().serialize(null, jgen, this);
        }
        else {
            final Class<?> cls = value.getClass();
            this.findTypedValueSerializer(cls, true, null).serialize(value, jgen, this);
        }
    }
    
    public abstract void defaultSerializeDateValue(final long p0, final JsonGenerator p1) throws IOException, JsonProcessingException;
    
    public abstract void defaultSerializeDateValue(final Date p0, final JsonGenerator p1) throws IOException, JsonProcessingException;
    
    public abstract void defaultSerializeDateKey(final long p0, final JsonGenerator p1) throws IOException, JsonProcessingException;
    
    public abstract void defaultSerializeDateKey(final Date p0, final JsonGenerator p1) throws IOException, JsonProcessingException;
    
    public final void defaultSerializeNull(final JsonGenerator jgen) throws IOException, JsonProcessingException {
        this.getNullValueSerializer().serialize(null, jgen, this);
    }
    
    public abstract int cachedSerializersCount();
    
    public abstract void flushCachedSerializers();
    
    static {
        TYPE_OBJECT = TypeFactory.defaultInstance().uncheckedSimpleType(Object.class);
    }
}
