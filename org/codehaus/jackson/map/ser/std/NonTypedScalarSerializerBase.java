// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;

public abstract class NonTypedScalarSerializerBase<T> extends ScalarSerializerBase<T>
{
    protected NonTypedScalarSerializerBase(final Class<T> t) {
        super(t);
    }
    
    @Override
    public final void serializeWithType(final T value, final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonGenerationException {
        this.serialize(value, jgen, provider);
    }
}
