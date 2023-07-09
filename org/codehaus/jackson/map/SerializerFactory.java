// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.ser.BeanSerializerModifier;

public abstract class SerializerFactory
{
    public abstract Config getConfig();
    
    public abstract SerializerFactory withConfig(final Config p0);
    
    public final SerializerFactory withAdditionalSerializers(final Serializers additional) {
        return this.withConfig(this.getConfig().withAdditionalSerializers(additional));
    }
    
    public final SerializerFactory withAdditionalKeySerializers(final Serializers additional) {
        return this.withConfig(this.getConfig().withAdditionalKeySerializers(additional));
    }
    
    public final SerializerFactory withSerializerModifier(final BeanSerializerModifier modifier) {
        return this.withConfig(this.getConfig().withSerializerModifier(modifier));
    }
    
    public abstract JsonSerializer<Object> createSerializer(final SerializationConfig p0, final JavaType p1, final BeanProperty p2) throws JsonMappingException;
    
    public abstract TypeSerializer createTypeSerializer(final SerializationConfig p0, final JavaType p1, final BeanProperty p2) throws JsonMappingException;
    
    public abstract JsonSerializer<Object> createKeySerializer(final SerializationConfig p0, final JavaType p1, final BeanProperty p2) throws JsonMappingException;
    
    @Deprecated
    public final JsonSerializer<Object> createSerializer(final JavaType type, final SerializationConfig config) {
        try {
            return this.createSerializer(config, type, null);
        }
        catch (final JsonMappingException e) {
            throw new RuntimeJsonMappingException(e);
        }
    }
    
    @Deprecated
    public final TypeSerializer createTypeSerializer(final JavaType baseType, final SerializationConfig config) {
        try {
            return this.createTypeSerializer(config, baseType, null);
        }
        catch (final JsonMappingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public abstract static class Config
    {
        public abstract Config withAdditionalSerializers(final Serializers p0);
        
        public abstract Config withAdditionalKeySerializers(final Serializers p0);
        
        public abstract Config withSerializerModifier(final BeanSerializerModifier p0);
        
        public abstract boolean hasSerializers();
        
        public abstract boolean hasKeySerializers();
        
        public abstract boolean hasSerializerModifiers();
        
        public abstract Iterable<Serializers> serializers();
        
        public abstract Iterable<Serializers> keySerializers();
        
        public abstract Iterable<BeanSerializerModifier> serializerModifiers();
    }
}
