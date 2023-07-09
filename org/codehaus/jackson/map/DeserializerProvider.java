// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.io.SerializedString;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.deser.ValueInstantiators;
import org.codehaus.jackson.map.deser.BeanDeserializerModifier;

public abstract class DeserializerProvider
{
    protected DeserializerProvider() {
    }
    
    public abstract DeserializerProvider withFactory(final DeserializerFactory p0);
    
    public abstract DeserializerProvider withAdditionalDeserializers(final Deserializers p0);
    
    public abstract DeserializerProvider withAdditionalKeyDeserializers(final KeyDeserializers p0);
    
    public abstract DeserializerProvider withDeserializerModifier(final BeanDeserializerModifier p0);
    
    public abstract DeserializerProvider withAbstractTypeResolver(final AbstractTypeResolver p0);
    
    public abstract DeserializerProvider withValueInstantiators(final ValueInstantiators p0);
    
    public abstract JsonDeserializer<Object> findValueDeserializer(final DeserializationConfig p0, final JavaType p1, final BeanProperty p2) throws JsonMappingException;
    
    public abstract JsonDeserializer<Object> findTypedValueDeserializer(final DeserializationConfig p0, final JavaType p1, final BeanProperty p2) throws JsonMappingException;
    
    public abstract KeyDeserializer findKeyDeserializer(final DeserializationConfig p0, final JavaType p1, final BeanProperty p2) throws JsonMappingException;
    
    public abstract boolean hasValueDeserializerFor(final DeserializationConfig p0, final JavaType p1);
    
    public abstract JavaType mapAbstractType(final DeserializationConfig p0, final JavaType p1) throws JsonMappingException;
    
    public abstract SerializedString findExpectedRootName(final DeserializationConfig p0, final JavaType p1) throws JsonMappingException;
    
    public abstract int cachedDeserializersCount();
    
    public abstract void flushCachedDeserializers();
}
