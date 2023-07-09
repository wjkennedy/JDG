// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser;

import org.codehaus.jackson.map.ser.std.SerializerBase;

@Deprecated
public abstract class ScalarSerializerBase<T> extends SerializerBase<T>
{
    protected ScalarSerializerBase(final Class<T> t) {
        super(t);
    }
    
    protected ScalarSerializerBase(final Class<?> t, final boolean dummy) {
        super(t);
    }
}
