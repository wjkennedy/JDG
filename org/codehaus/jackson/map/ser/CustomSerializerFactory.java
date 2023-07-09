// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.SerializationConfig;
import java.lang.reflect.Modifier;
import org.codehaus.jackson.map.SerializerFactory;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.type.ClassKey;
import java.util.HashMap;

public class CustomSerializerFactory extends BeanSerializerFactory
{
    protected HashMap<ClassKey, JsonSerializer<?>> _directClassMappings;
    protected JsonSerializer<?> _enumSerializerOverride;
    protected HashMap<ClassKey, JsonSerializer<?>> _transitiveClassMappings;
    protected HashMap<ClassKey, JsonSerializer<?>> _interfaceMappings;
    
    public CustomSerializerFactory() {
        this(null);
    }
    
    public CustomSerializerFactory(final Config config) {
        super(config);
        this._directClassMappings = null;
        this._transitiveClassMappings = null;
        this._interfaceMappings = null;
    }
    
    @Override
    public SerializerFactory withConfig(final Config config) {
        if (this.getClass() != CustomSerializerFactory.class) {
            throw new IllegalStateException("Subtype of CustomSerializerFactory (" + this.getClass().getName() + ") has not properly overridden method 'withAdditionalSerializers': can not instantiate subtype with additional serializer definitions");
        }
        return new CustomSerializerFactory(config);
    }
    
    public <T> void addGenericMapping(final Class<? extends T> type, final JsonSerializer<T> ser) {
        final ClassKey key = new ClassKey(type);
        if (type.isInterface()) {
            if (this._interfaceMappings == null) {
                this._interfaceMappings = new HashMap<ClassKey, JsonSerializer<?>>();
            }
            this._interfaceMappings.put(key, ser);
        }
        else {
            if (this._transitiveClassMappings == null) {
                this._transitiveClassMappings = new HashMap<ClassKey, JsonSerializer<?>>();
            }
            this._transitiveClassMappings.put(key, ser);
        }
    }
    
    public <T> void addSpecificMapping(final Class<? extends T> forClass, final JsonSerializer<T> ser) {
        final ClassKey key = new ClassKey(forClass);
        if (forClass.isInterface()) {
            throw new IllegalArgumentException("Can not add specific mapping for an interface (" + forClass.getName() + ")");
        }
        if (Modifier.isAbstract(forClass.getModifiers())) {
            throw new IllegalArgumentException("Can not add specific mapping for an abstract class (" + forClass.getName() + ")");
        }
        if (this._directClassMappings == null) {
            this._directClassMappings = new HashMap<ClassKey, JsonSerializer<?>>();
        }
        this._directClassMappings.put(key, ser);
    }
    
    public void setEnumSerializer(final JsonSerializer<?> enumSer) {
        this._enumSerializerOverride = enumSer;
    }
    
    @Override
    public JsonSerializer<Object> createSerializer(final SerializationConfig config, final JavaType type, final BeanProperty property) throws JsonMappingException {
        final JsonSerializer<?> ser = this.findCustomSerializer(type.getRawClass(), config);
        if (ser != null) {
            return (JsonSerializer<Object>)ser;
        }
        return super.createSerializer(config, type, property);
    }
    
    protected JsonSerializer<?> findCustomSerializer(final Class<?> type, final SerializationConfig config) {
        JsonSerializer<?> ser = null;
        final ClassKey key = new ClassKey(type);
        if (this._directClassMappings != null) {
            ser = this._directClassMappings.get(key);
            if (ser != null) {
                return ser;
            }
        }
        if (type.isEnum() && this._enumSerializerOverride != null) {
            return this._enumSerializerOverride;
        }
        if (this._transitiveClassMappings != null) {
            for (Class<?> curr = type; curr != null; curr = curr.getSuperclass()) {
                key.reset(curr);
                ser = this._transitiveClassMappings.get(key);
                if (ser != null) {
                    return ser;
                }
            }
        }
        if (this._interfaceMappings != null) {
            key.reset(type);
            ser = this._interfaceMappings.get(key);
            if (ser != null) {
                return ser;
            }
            for (Class<?> curr = type; curr != null; curr = curr.getSuperclass()) {
                ser = this._findInterfaceMapping(curr, key);
                if (ser != null) {
                    return ser;
                }
            }
        }
        return null;
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
