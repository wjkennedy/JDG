// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.module;

import org.codehaus.jackson.map.type.MapLikeType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.map.type.CollectionLikeType;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.BeanDescription;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.type.ClassKey;
import java.util.HashMap;
import org.codehaus.jackson.map.Serializers;

public class SimpleSerializers extends Serializers.Base
{
    protected HashMap<ClassKey, JsonSerializer<?>> _classMappings;
    protected HashMap<ClassKey, JsonSerializer<?>> _interfaceMappings;
    
    public SimpleSerializers() {
        this._classMappings = null;
        this._interfaceMappings = null;
    }
    
    public void addSerializer(final JsonSerializer<?> ser) {
        final Class<?> cls = ser.handledType();
        if (cls == null || cls == Object.class) {
            throw new IllegalArgumentException("JsonSerializer of type " + ser.getClass().getName() + " does not define valid handledType() -- must either register with method that takes type argument  or make serializer extend 'org.codehaus.jackson.map.ser.std.SerializerBase'");
        }
        this._addSerializer(cls, ser);
    }
    
    public <T> void addSerializer(final Class<? extends T> type, final JsonSerializer<T> ser) {
        this._addSerializer(type, ser);
    }
    
    private void _addSerializer(final Class<?> cls, final JsonSerializer<?> ser) {
        final ClassKey key = new ClassKey(cls);
        if (cls.isInterface()) {
            if (this._interfaceMappings == null) {
                this._interfaceMappings = new HashMap<ClassKey, JsonSerializer<?>>();
            }
            this._interfaceMappings.put(key, ser);
        }
        else {
            if (this._classMappings == null) {
                this._classMappings = new HashMap<ClassKey, JsonSerializer<?>>();
            }
            this._classMappings.put(key, ser);
        }
    }
    
    @Override
    public JsonSerializer<?> findSerializer(final SerializationConfig config, final JavaType type, final BeanDescription beanDesc, final BeanProperty property) {
        Class<?> cls = type.getRawClass();
        final ClassKey key = new ClassKey(cls);
        JsonSerializer<?> ser = null;
        if (cls.isInterface()) {
            if (this._interfaceMappings != null) {
                ser = this._interfaceMappings.get(key);
                if (ser != null) {
                    return ser;
                }
            }
        }
        else if (this._classMappings != null) {
            ser = this._classMappings.get(key);
            if (ser != null) {
                return ser;
            }
            for (Class<?> curr = cls; curr != null; curr = curr.getSuperclass()) {
                key.reset(curr);
                ser = this._classMappings.get(key);
                if (ser != null) {
                    return ser;
                }
            }
        }
        if (this._interfaceMappings != null) {
            ser = this._findInterfaceMapping(cls, key);
            if (ser != null) {
                return ser;
            }
            if (!cls.isInterface()) {
                while ((cls = cls.getSuperclass()) != null) {
                    ser = this._findInterfaceMapping(cls, key);
                    if (ser != null) {
                        return ser;
                    }
                }
            }
        }
        return null;
    }
    
    @Override
    public JsonSerializer<?> findArraySerializer(final SerializationConfig config, final ArrayType type, final BeanDescription beanDesc, final BeanProperty property, final TypeSerializer elementTypeSerializer, final JsonSerializer<Object> elementValueSerializer) {
        return this.findSerializer(config, type, beanDesc, property);
    }
    
    @Override
    public JsonSerializer<?> findCollectionSerializer(final SerializationConfig config, final CollectionType type, final BeanDescription beanDesc, final BeanProperty property, final TypeSerializer elementTypeSerializer, final JsonSerializer<Object> elementValueSerializer) {
        return this.findSerializer(config, type, beanDesc, property);
    }
    
    @Override
    public JsonSerializer<?> findCollectionLikeSerializer(final SerializationConfig config, final CollectionLikeType type, final BeanDescription beanDesc, final BeanProperty property, final TypeSerializer elementTypeSerializer, final JsonSerializer<Object> elementValueSerializer) {
        return this.findSerializer(config, type, beanDesc, property);
    }
    
    @Override
    public JsonSerializer<?> findMapSerializer(final SerializationConfig config, final MapType type, final BeanDescription beanDesc, final BeanProperty property, final JsonSerializer<Object> keySerializer, final TypeSerializer elementTypeSerializer, final JsonSerializer<Object> elementValueSerializer) {
        return this.findSerializer(config, type, beanDesc, property);
    }
    
    @Override
    public JsonSerializer<?> findMapLikeSerializer(final SerializationConfig config, final MapLikeType type, final BeanDescription beanDesc, final BeanProperty property, final JsonSerializer<Object> keySerializer, final TypeSerializer elementTypeSerializer, final JsonSerializer<Object> elementValueSerializer) {
        return this.findSerializer(config, type, beanDesc, property);
    }
    
    protected JsonSerializer<?> _findInterfaceMapping(final Class<?> cls, final ClassKey key) {
        for (final Class<?> iface : cls.getInterfaces()) {
            key.reset(iface);
            JsonSerializer<?> ser = this._interfaceMappings.get(key);
            if (ser != null) {
                return ser;
            }
            ser = this._findInterfaceMapping(iface, key);
            if (ser != null) {
                return ser;
            }
        }
        return null;
    }
}
