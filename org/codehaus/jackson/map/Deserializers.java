// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.type.MapLikeType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.map.type.CollectionLikeType;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.ArrayType;

public interface Deserializers
{
    JsonDeserializer<?> findArrayDeserializer(final ArrayType p0, final DeserializationConfig p1, final DeserializerProvider p2, final BeanProperty p3, final TypeDeserializer p4, final JsonDeserializer<?> p5) throws JsonMappingException;
    
    JsonDeserializer<?> findCollectionDeserializer(final CollectionType p0, final DeserializationConfig p1, final DeserializerProvider p2, final BeanDescription p3, final BeanProperty p4, final TypeDeserializer p5, final JsonDeserializer<?> p6) throws JsonMappingException;
    
    JsonDeserializer<?> findCollectionLikeDeserializer(final CollectionLikeType p0, final DeserializationConfig p1, final DeserializerProvider p2, final BeanDescription p3, final BeanProperty p4, final TypeDeserializer p5, final JsonDeserializer<?> p6) throws JsonMappingException;
    
    JsonDeserializer<?> findEnumDeserializer(final Class<?> p0, final DeserializationConfig p1, final BeanDescription p2, final BeanProperty p3) throws JsonMappingException;
    
    JsonDeserializer<?> findMapDeserializer(final MapType p0, final DeserializationConfig p1, final DeserializerProvider p2, final BeanDescription p3, final BeanProperty p4, final KeyDeserializer p5, final TypeDeserializer p6, final JsonDeserializer<?> p7) throws JsonMappingException;
    
    JsonDeserializer<?> findMapLikeDeserializer(final MapLikeType p0, final DeserializationConfig p1, final DeserializerProvider p2, final BeanDescription p3, final BeanProperty p4, final KeyDeserializer p5, final TypeDeserializer p6, final JsonDeserializer<?> p7) throws JsonMappingException;
    
    JsonDeserializer<?> findTreeNodeDeserializer(final Class<? extends JsonNode> p0, final DeserializationConfig p1, final BeanProperty p2) throws JsonMappingException;
    
    JsonDeserializer<?> findBeanDeserializer(final JavaType p0, final DeserializationConfig p1, final DeserializerProvider p2, final BeanDescription p3, final BeanProperty p4) throws JsonMappingException;
    
    public static class Base implements Deserializers
    {
        public JsonDeserializer<?> findArrayDeserializer(final ArrayType type, final DeserializationConfig config, final DeserializerProvider provider, final BeanProperty property, final TypeDeserializer elementTypeDeserializer, final JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
            return null;
        }
        
        public JsonDeserializer<?> findCollectionDeserializer(final CollectionType type, final DeserializationConfig config, final DeserializerProvider provider, final BeanDescription beanDesc, final BeanProperty property, final TypeDeserializer elementTypeDeserializer, final JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
            return null;
        }
        
        public JsonDeserializer<?> findCollectionLikeDeserializer(final CollectionLikeType type, final DeserializationConfig config, final DeserializerProvider provider, final BeanDescription beanDesc, final BeanProperty property, final TypeDeserializer elementTypeDeserializer, final JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
            return null;
        }
        
        public JsonDeserializer<?> findMapDeserializer(final MapType type, final DeserializationConfig config, final DeserializerProvider provider, final BeanDescription beanDesc, final BeanProperty property, final KeyDeserializer keyDeserializer, final TypeDeserializer elementTypeDeserializer, final JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
            return null;
        }
        
        public JsonDeserializer<?> findMapLikeDeserializer(final MapLikeType type, final DeserializationConfig config, final DeserializerProvider provider, final BeanDescription beanDesc, final BeanProperty property, final KeyDeserializer keyDeserializer, final TypeDeserializer elementTypeDeserializer, final JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
            return null;
        }
        
        public JsonDeserializer<?> findEnumDeserializer(final Class<?> type, final DeserializationConfig config, final BeanDescription beanDesc, final BeanProperty property) throws JsonMappingException {
            return null;
        }
        
        public JsonDeserializer<?> findTreeNodeDeserializer(final Class<? extends JsonNode> nodeType, final DeserializationConfig config, final BeanProperty property) throws JsonMappingException {
            return null;
        }
        
        public JsonDeserializer<?> findBeanDeserializer(final JavaType type, final DeserializationConfig config, final DeserializerProvider provider, final BeanDescription beanDesc, final BeanProperty property) throws JsonMappingException {
            return null;
        }
    }
    
    @Deprecated
    public static class None extends Base
    {
    }
}
