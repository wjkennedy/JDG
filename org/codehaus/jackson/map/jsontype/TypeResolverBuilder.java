// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.jsontype;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.BeanProperty;
import java.util.Collection;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.SerializationConfig;

public interface TypeResolverBuilder<T extends TypeResolverBuilder<T>>
{
    Class<?> getDefaultImpl();
    
    TypeSerializer buildTypeSerializer(final SerializationConfig p0, final JavaType p1, final Collection<NamedType> p2, final BeanProperty p3);
    
    TypeDeserializer buildTypeDeserializer(final DeserializationConfig p0, final JavaType p1, final Collection<NamedType> p2, final BeanProperty p3);
    
    T init(final JsonTypeInfo.Id p0, final TypeIdResolver p1);
    
    T inclusion(final JsonTypeInfo.As p0);
    
    T typeProperty(final String p0);
    
    T defaultImpl(final Class<?> p0);
}
