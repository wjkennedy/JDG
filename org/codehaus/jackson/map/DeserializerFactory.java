// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.map.type.MapLikeType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.map.type.CollectionLikeType;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.map.deser.ValueInstantiator;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.deser.ValueInstantiators;
import org.codehaus.jackson.map.deser.BeanDeserializerModifier;

public abstract class DeserializerFactory
{
    protected static final Deserializers[] NO_DESERIALIZERS;
    
    public abstract Config getConfig();
    
    public abstract DeserializerFactory withConfig(final Config p0);
    
    public final DeserializerFactory withAdditionalDeserializers(final Deserializers additional) {
        return this.withConfig(this.getConfig().withAdditionalDeserializers(additional));
    }
    
    public final DeserializerFactory withAdditionalKeyDeserializers(final KeyDeserializers additional) {
        return this.withConfig(this.getConfig().withAdditionalKeyDeserializers(additional));
    }
    
    public final DeserializerFactory withDeserializerModifier(final BeanDeserializerModifier modifier) {
        return this.withConfig(this.getConfig().withDeserializerModifier(modifier));
    }
    
    public final DeserializerFactory withAbstractTypeResolver(final AbstractTypeResolver resolver) {
        return this.withConfig(this.getConfig().withAbstractTypeResolver(resolver));
    }
    
    public final DeserializerFactory withValueInstantiators(final ValueInstantiators instantiators) {
        return this.withConfig(this.getConfig().withValueInstantiators(instantiators));
    }
    
    public abstract JavaType mapAbstractType(final DeserializationConfig p0, final JavaType p1) throws JsonMappingException;
    
    public abstract ValueInstantiator findValueInstantiator(final DeserializationConfig p0, final BasicBeanDescription p1) throws JsonMappingException;
    
    public abstract JsonDeserializer<Object> createBeanDeserializer(final DeserializationConfig p0, final DeserializerProvider p1, final JavaType p2, final BeanProperty p3) throws JsonMappingException;
    
    public abstract JsonDeserializer<?> createArrayDeserializer(final DeserializationConfig p0, final DeserializerProvider p1, final ArrayType p2, final BeanProperty p3) throws JsonMappingException;
    
    public abstract JsonDeserializer<?> createCollectionDeserializer(final DeserializationConfig p0, final DeserializerProvider p1, final CollectionType p2, final BeanProperty p3) throws JsonMappingException;
    
    public abstract JsonDeserializer<?> createCollectionLikeDeserializer(final DeserializationConfig p0, final DeserializerProvider p1, final CollectionLikeType p2, final BeanProperty p3) throws JsonMappingException;
    
    public abstract JsonDeserializer<?> createEnumDeserializer(final DeserializationConfig p0, final DeserializerProvider p1, final JavaType p2, final BeanProperty p3) throws JsonMappingException;
    
    public abstract JsonDeserializer<?> createMapDeserializer(final DeserializationConfig p0, final DeserializerProvider p1, final MapType p2, final BeanProperty p3) throws JsonMappingException;
    
    public abstract JsonDeserializer<?> createMapLikeDeserializer(final DeserializationConfig p0, final DeserializerProvider p1, final MapLikeType p2, final BeanProperty p3) throws JsonMappingException;
    
    public abstract JsonDeserializer<?> createTreeDeserializer(final DeserializationConfig p0, final DeserializerProvider p1, final JavaType p2, final BeanProperty p3) throws JsonMappingException;
    
    public KeyDeserializer createKeyDeserializer(final DeserializationConfig config, final JavaType type, final BeanProperty property) throws JsonMappingException {
        return null;
    }
    
    public TypeDeserializer findTypeDeserializer(final DeserializationConfig config, final JavaType baseType, final BeanProperty property) throws JsonMappingException {
        return null;
    }
    
    static {
        NO_DESERIALIZERS = new Deserializers[0];
    }
    
    public abstract static class Config
    {
        public abstract Config withAdditionalDeserializers(final Deserializers p0);
        
        public abstract Config withAdditionalKeyDeserializers(final KeyDeserializers p0);
        
        public abstract Config withDeserializerModifier(final BeanDeserializerModifier p0);
        
        public abstract Config withAbstractTypeResolver(final AbstractTypeResolver p0);
        
        public abstract Config withValueInstantiators(final ValueInstantiators p0);
        
        public abstract Iterable<Deserializers> deserializers();
        
        public abstract Iterable<KeyDeserializers> keyDeserializers();
        
        public abstract Iterable<BeanDeserializerModifier> deserializerModifiers();
        
        public abstract Iterable<AbstractTypeResolver> abstractTypeResolvers();
        
        public abstract Iterable<ValueInstantiators> valueInstantiators();
        
        public abstract boolean hasDeserializers();
        
        public abstract boolean hasKeyDeserializers();
        
        public abstract boolean hasDeserializerModifiers();
        
        public abstract boolean hasAbstractTypeResolvers();
        
        public abstract boolean hasValueInstantiators();
    }
}
