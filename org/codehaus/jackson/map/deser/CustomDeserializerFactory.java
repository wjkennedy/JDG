// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser;

import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializerFactory;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.type.ClassKey;
import java.util.HashMap;

@Deprecated
public class CustomDeserializerFactory extends BeanDeserializerFactory
{
    protected HashMap<ClassKey, JsonDeserializer<Object>> _directClassMappings;
    protected HashMap<ClassKey, Class<?>> _mixInAnnotations;
    
    public CustomDeserializerFactory() {
        this(null);
    }
    
    protected CustomDeserializerFactory(final Config config) {
        super(config);
        this._directClassMappings = null;
    }
    
    @Override
    public DeserializerFactory withConfig(final Config config) {
        if (this.getClass() != CustomDeserializerFactory.class) {
            throw new IllegalStateException("Subtype of CustomDeserializerFactory (" + this.getClass().getName() + ") has not properly overridden method 'withAdditionalDeserializers': can not instantiate subtype with additional deserializer definitions");
        }
        return new CustomDeserializerFactory(config);
    }
    
    public <T> void addSpecificMapping(final Class<T> forClass, final JsonDeserializer<? extends T> deser) {
        final ClassKey key = new ClassKey(forClass);
        if (this._directClassMappings == null) {
            this._directClassMappings = new HashMap<ClassKey, JsonDeserializer<Object>>();
        }
        this._directClassMappings.put(key, (JsonDeserializer<Object>)deser);
    }
    
    public void addMixInAnnotationMapping(final Class<?> destinationClass, final Class<?> classWithMixIns) {
        if (this._mixInAnnotations == null) {
            this._mixInAnnotations = new HashMap<ClassKey, Class<?>>();
        }
        this._mixInAnnotations.put(new ClassKey(destinationClass), classWithMixIns);
    }
    
    @Override
    public JsonDeserializer<Object> createBeanDeserializer(final DeserializationConfig config, final DeserializerProvider p, final JavaType type, final BeanProperty property) throws JsonMappingException {
        final Class<?> cls = type.getRawClass();
        final ClassKey key = new ClassKey(cls);
        if (this._directClassMappings != null) {
            final JsonDeserializer<Object> deser = this._directClassMappings.get(key);
            if (deser != null) {
                return deser;
            }
        }
        return super.createBeanDeserializer(config, p, type, property);
    }
    
    @Override
    public JsonDeserializer<?> createArrayDeserializer(final DeserializationConfig config, final DeserializerProvider p, final ArrayType type, final BeanProperty property) throws JsonMappingException {
        final ClassKey key = new ClassKey(type.getRawClass());
        if (this._directClassMappings != null) {
            final JsonDeserializer<Object> deser = this._directClassMappings.get(key);
            if (deser != null) {
                return deser;
            }
        }
        return super.createArrayDeserializer(config, p, type, property);
    }
    
    @Override
    public JsonDeserializer<?> createEnumDeserializer(final DeserializationConfig config, final DeserializerProvider p, final JavaType enumType, final BeanProperty property) throws JsonMappingException {
        if (this._directClassMappings != null) {
            final ClassKey key = new ClassKey(enumType.getRawClass());
            final JsonDeserializer<?> deser = this._directClassMappings.get(key);
            if (deser != null) {
                return deser;
            }
        }
        return super.createEnumDeserializer(config, p, enumType, property);
    }
}
