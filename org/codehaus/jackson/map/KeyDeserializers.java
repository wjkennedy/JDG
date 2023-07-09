// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

import org.codehaus.jackson.type.JavaType;

public interface KeyDeserializers
{
    KeyDeserializer findKeyDeserializer(final JavaType p0, final DeserializationConfig p1, final BeanDescription p2, final BeanProperty p3) throws JsonMappingException;
}
