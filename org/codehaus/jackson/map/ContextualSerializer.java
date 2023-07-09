// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

public interface ContextualSerializer<T>
{
    JsonSerializer<T> createContextual(final SerializationConfig p0, final BeanProperty p1) throws JsonMappingException;
}
