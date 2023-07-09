// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map;

public interface ResolvableDeserializer
{
    void resolve(final DeserializationConfig p0, final DeserializerProvider p1) throws JsonMappingException;
}
