// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.type.JavaType;

public abstract class ContainerDeserializerBase<T> extends StdDeserializer<T>
{
    protected ContainerDeserializerBase(final Class<?> selfType) {
        super(selfType);
    }
    
    public abstract JavaType getContentType();
    
    public abstract JsonDeserializer<Object> getContentDeserializer();
}
