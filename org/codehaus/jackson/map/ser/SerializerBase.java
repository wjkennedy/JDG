// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser;

import org.codehaus.jackson.type.JavaType;

@Deprecated
public abstract class SerializerBase<T> extends org.codehaus.jackson.map.ser.std.SerializerBase<T>
{
    protected SerializerBase(final Class<T> t) {
        super(t);
    }
    
    protected SerializerBase(final JavaType type) {
        super(type);
    }
    
    protected SerializerBase(final Class<?> t, final boolean dummy) {
        super(t, dummy);
    }
}
