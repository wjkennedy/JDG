// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.map.type.MapLikeType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.map.type.CollectionLikeType;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.type.JavaType;

public interface Serializers
{
    JsonSerializer<?> findSerializer(final SerializationConfig p0, final JavaType p1, final BeanDescription p2, final BeanProperty p3);
    
    JsonSerializer<?> findArraySerializer(final SerializationConfig p0, final ArrayType p1, final BeanDescription p2, final BeanProperty p3, final TypeSerializer p4, final JsonSerializer<Object> p5);
    
    JsonSerializer<?> findCollectionSerializer(final SerializationConfig p0, final CollectionType p1, final BeanDescription p2, final BeanProperty p3, final TypeSerializer p4, final JsonSerializer<Object> p5);
    
    JsonSerializer<?> findCollectionLikeSerializer(final SerializationConfig p0, final CollectionLikeType p1, final BeanDescription p2, final BeanProperty p3, final TypeSerializer p4, final JsonSerializer<Object> p5);
    
    JsonSerializer<?> findMapSerializer(final SerializationConfig p0, final MapType p1, final BeanDescription p2, final BeanProperty p3, final JsonSerializer<Object> p4, final TypeSerializer p5, final JsonSerializer<Object> p6);
    
    JsonSerializer<?> findMapLikeSerializer(final SerializationConfig p0, final MapLikeType p1, final BeanDescription p2, final BeanProperty p3, final JsonSerializer<Object> p4, final TypeSerializer p5, final JsonSerializer<Object> p6);
    
    public static class Base implements Serializers
    {
        public JsonSerializer<?> findSerializer(final SerializationConfig config, final JavaType type, final BeanDescription beanDesc, final BeanProperty property) {
            return null;
        }
        
        public JsonSerializer<?> findArraySerializer(final SerializationConfig config, final ArrayType type, final BeanDescription beanDesc, final BeanProperty property, final TypeSerializer elementTypeSerializer, final JsonSerializer<Object> elementValueSerializer) {
            return null;
        }
        
        public JsonSerializer<?> findCollectionSerializer(final SerializationConfig config, final CollectionType type, final BeanDescription beanDesc, final BeanProperty property, final TypeSerializer elementTypeSerializer, final JsonSerializer<Object> elementValueSerializer) {
            return null;
        }
        
        public JsonSerializer<?> findCollectionLikeSerializer(final SerializationConfig config, final CollectionLikeType type, final BeanDescription beanDesc, final BeanProperty property, final TypeSerializer elementTypeSerializer, final JsonSerializer<Object> elementValueSerializer) {
            return null;
        }
        
        public JsonSerializer<?> findMapSerializer(final SerializationConfig config, final MapType type, final BeanDescription beanDesc, final BeanProperty property, final JsonSerializer<Object> keySerializer, final TypeSerializer elementTypeSerializer, final JsonSerializer<Object> elementValueSerializer) {
            return null;
        }
        
        public JsonSerializer<?> findMapLikeSerializer(final SerializationConfig config, final MapLikeType type, final BeanDescription beanDesc, final BeanProperty property, final JsonSerializer<Object> keySerializer, final TypeSerializer elementTypeSerializer, final JsonSerializer<Object> elementValueSerializer) {
            return null;
        }
    }
    
    @Deprecated
    public static class None extends Base
    {
    }
}
