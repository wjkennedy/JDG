// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.module;

import java.util.Iterator;
import java.util.Map;
import org.codehaus.jackson.map.deser.ValueInstantiators;
import org.codehaus.jackson.map.AbstractTypeResolver;
import org.codehaus.jackson.map.KeyDeserializers;
import org.codehaus.jackson.map.Deserializers;
import org.codehaus.jackson.map.Serializers;
import org.codehaus.jackson.map.deser.ValueInstantiator;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import java.util.HashMap;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.Module;

public class SimpleModule extends Module
{
    protected final String _name;
    protected final Version _version;
    protected SimpleSerializers _serializers;
    protected SimpleDeserializers _deserializers;
    protected SimpleSerializers _keySerializers;
    protected SimpleKeyDeserializers _keyDeserializers;
    protected SimpleAbstractTypeResolver _abstractTypes;
    protected SimpleValueInstantiators _valueInstantiators;
    protected HashMap<Class<?>, Class<?>> _mixins;
    
    public SimpleModule(final String name, final Version version) {
        this._serializers = null;
        this._deserializers = null;
        this._keySerializers = null;
        this._keyDeserializers = null;
        this._abstractTypes = null;
        this._valueInstantiators = null;
        this._mixins = null;
        this._name = name;
        this._version = version;
    }
    
    public void setSerializers(final SimpleSerializers s) {
        this._serializers = s;
    }
    
    public void setDeserializers(final SimpleDeserializers d) {
        this._deserializers = d;
    }
    
    public void setKeySerializers(final SimpleSerializers ks) {
        this._keySerializers = ks;
    }
    
    public void setKeyDeserializers(final SimpleKeyDeserializers kd) {
        this._keyDeserializers = kd;
    }
    
    public void setAbstractTypes(final SimpleAbstractTypeResolver atr) {
        this._abstractTypes = atr;
    }
    
    public void setValueInstantiators(final SimpleValueInstantiators svi) {
        this._valueInstantiators = svi;
    }
    
    public SimpleModule addSerializer(final JsonSerializer<?> ser) {
        if (this._serializers == null) {
            this._serializers = new SimpleSerializers();
        }
        this._serializers.addSerializer(ser);
        return this;
    }
    
    public <T> SimpleModule addSerializer(final Class<? extends T> type, final JsonSerializer<T> ser) {
        if (this._serializers == null) {
            this._serializers = new SimpleSerializers();
        }
        this._serializers.addSerializer(type, ser);
        return this;
    }
    
    public <T> SimpleModule addKeySerializer(final Class<? extends T> type, final JsonSerializer<T> ser) {
        if (this._keySerializers == null) {
            this._keySerializers = new SimpleSerializers();
        }
        this._keySerializers.addSerializer(type, ser);
        return this;
    }
    
    public <T> SimpleModule addDeserializer(final Class<T> type, final JsonDeserializer<? extends T> deser) {
        if (this._deserializers == null) {
            this._deserializers = new SimpleDeserializers();
        }
        this._deserializers.addDeserializer(type, deser);
        return this;
    }
    
    public SimpleModule addKeyDeserializer(final Class<?> type, final KeyDeserializer deser) {
        if (this._keyDeserializers == null) {
            this._keyDeserializers = new SimpleKeyDeserializers();
        }
        this._keyDeserializers.addDeserializer(type, deser);
        return this;
    }
    
    public <T> SimpleModule addAbstractTypeMapping(final Class<T> superType, final Class<? extends T> subType) {
        if (this._abstractTypes == null) {
            this._abstractTypes = new SimpleAbstractTypeResolver();
        }
        this._abstractTypes = this._abstractTypes.addMapping(superType, subType);
        return this;
    }
    
    public SimpleModule addValueInstantiator(final Class<?> beanType, final ValueInstantiator inst) {
        if (this._valueInstantiators == null) {
            this._valueInstantiators = new SimpleValueInstantiators();
        }
        this._valueInstantiators = this._valueInstantiators.addValueInstantiator(beanType, inst);
        return this;
    }
    
    public SimpleModule setMixInAnnotation(final Class<?> targetType, final Class<?> mixinClass) {
        if (this._mixins == null) {
            this._mixins = new HashMap<Class<?>, Class<?>>();
        }
        this._mixins.put(targetType, mixinClass);
        return this;
    }
    
    @Override
    public String getModuleName() {
        return this._name;
    }
    
    @Override
    public void setupModule(final SetupContext context) {
        if (this._serializers != null) {
            context.addSerializers(this._serializers);
        }
        if (this._deserializers != null) {
            context.addDeserializers(this._deserializers);
        }
        if (this._keySerializers != null) {
            context.addKeySerializers(this._keySerializers);
        }
        if (this._keyDeserializers != null) {
            context.addKeyDeserializers(this._keyDeserializers);
        }
        if (this._abstractTypes != null) {
            context.addAbstractTypeResolver(this._abstractTypes);
        }
        if (this._valueInstantiators != null) {
            context.addValueInstantiators(this._valueInstantiators);
        }
        if (this._mixins != null) {
            for (final Map.Entry<Class<?>, Class<?>> entry : this._mixins.entrySet()) {
                context.setMixInAnnotations(entry.getKey(), entry.getValue());
            }
        }
    }
    
    @Override
    public Version version() {
        return this._version;
    }
}
