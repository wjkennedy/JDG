// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.module;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.type.MapLikeType;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.map.type.CollectionLikeType;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.BeanDescription;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.type.ClassKey;
import java.util.HashMap;
import org.codehaus.jackson.map.Deserializers;

public class SimpleDeserializers implements Deserializers
{
    protected HashMap<ClassKey, JsonDeserializer<?>> _classMappings;
    
    public SimpleDeserializers() {
        this._classMappings = null;
    }
    
    public <T> void addDeserializer(final Class<T> forClass, final JsonDeserializer<? extends T> deser) {
        final ClassKey key = new ClassKey(forClass);
        if (this._classMappings == null) {
            this._classMappings = new HashMap<ClassKey, JsonDeserializer<?>>();
        }
        this._classMappings.put(key, deser);
    }
    
    public JsonDeserializer<?> findArrayDeserializer(final ArrayType type, final DeserializationConfig config, final DeserializerProvider provider, final BeanProperty property, final TypeDeserializer elementTypeDeserializer, final JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        return (this._classMappings == null) ? null : this._classMappings.get(new ClassKey(type.getRawClass()));
    }
    
    public JsonDeserializer<?> findBeanDeserializer(final JavaType type, final DeserializationConfig config, final DeserializerProvider provider, final BeanDescription beanDesc, final BeanProperty property) throws JsonMappingException {
        return (this._classMappings == null) ? null : this._classMappings.get(new ClassKey(type.getRawClass()));
    }
    
    public JsonDeserializer<?> findCollectionDeserializer(final CollectionType type, final DeserializationConfig config, final DeserializerProvider provider, final BeanDescription beanDesc, final BeanProperty property, final TypeDeserializer elementTypeDeserializer, final JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        return (this._classMappings == null) ? null : this._classMappings.get(new ClassKey(type.getRawClass()));
    }
    
    public JsonDeserializer<?> findCollectionLikeDeserializer(final CollectionLikeType type, final DeserializationConfig config, final DeserializerProvider provider, final BeanDescription beanDesc, final BeanProperty property, final TypeDeserializer elementTypeDeserializer, final JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        return (this._classMappings == null) ? null : this._classMappings.get(new ClassKey(type.getRawClass()));
    }
    
    public JsonDeserializer<?> findEnumDeserializer(final Class<?> type, final DeserializationConfig config, final BeanDescription beanDesc, final BeanProperty property) throws JsonMappingException {
        return (this._classMappings == null) ? null : this._classMappings.get(new ClassKey(type));
    }
    
    public JsonDeserializer<?> findMapDeserializer(final MapType type, final DeserializationConfig config, final DeserializerProvider provider, final BeanDescription beanDesc, final BeanProperty property, final KeyDeserializer keyDeserializer, final TypeDeserializer elementTypeDeserializer, final JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        return (this._classMappings == null) ? null : this._classMappings.get(new ClassKey(type.getRawClass()));
    }
    
    public JsonDeserializer<?> findMapLikeDeserializer(final MapLikeType type, final DeserializationConfig config, final DeserializerProvider provider, final BeanDescription beanDesc, final BeanProperty property, final KeyDeserializer keyDeserializer, final TypeDeserializer elementTypeDeserializer, final JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        return (this._classMappings == null) ? null : this._classMappings.get(new ClassKey(type.getRawClass()));
    }
    
    public JsonDeserializer<?> findTreeNodeDeserializer(final Class<? extends JsonNode> nodeType, final DeserializationConfig config, final BeanProperty property) throws JsonMappingException {
        return (this._classMappings == null) ? null : this._classMappings.get(new ClassKey(nodeType));
    }
}
