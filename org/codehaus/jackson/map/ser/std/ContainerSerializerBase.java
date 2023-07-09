// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.map.TypeSerializer;

public abstract class ContainerSerializerBase<T> extends SerializerBase<T>
{
    protected ContainerSerializerBase(final Class<T> t) {
        super(t);
    }
    
    protected ContainerSerializerBase(final Class<?> t, final boolean dummy) {
        super(t, dummy);
    }
    
    public ContainerSerializerBase<?> withValueTypeSerializer(final TypeSerializer vts) {
        if (vts == null) {
            return this;
        }
        return this._withValueTypeSerializer(vts);
    }
    
    public abstract ContainerSerializerBase<?> _withValueTypeSerializer(final TypeSerializer p0);
}
