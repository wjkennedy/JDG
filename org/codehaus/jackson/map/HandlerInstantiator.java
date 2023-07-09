// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.map.deser.ValueInstantiator;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.map.introspect.Annotated;

public abstract class HandlerInstantiator
{
    public abstract JsonDeserializer<?> deserializerInstance(final DeserializationConfig p0, final Annotated p1, final Class<? extends JsonDeserializer<?>> p2);
    
    public abstract KeyDeserializer keyDeserializerInstance(final DeserializationConfig p0, final Annotated p1, final Class<? extends KeyDeserializer> p2);
    
    public abstract JsonSerializer<?> serializerInstance(final SerializationConfig p0, final Annotated p1, final Class<? extends JsonSerializer<?>> p2);
    
    public abstract TypeResolverBuilder<?> typeResolverBuilderInstance(final MapperConfig<?> p0, final Annotated p1, final Class<? extends TypeResolverBuilder<?>> p2);
    
    public abstract TypeIdResolver typeIdResolverInstance(final MapperConfig<?> p0, final Annotated p1, final Class<? extends TypeIdResolver> p2);
    
    public ValueInstantiator valueInstantiatorInstance(final MapperConfig<?> config, final Annotated annotated, final Class<? extends ValueInstantiator> resolverClass) {
        return null;
    }
}
