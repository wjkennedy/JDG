// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import java.util.Iterator;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;

@JacksonStdImpl
public class IterableSerializer extends AsArraySerializerBase<Iterable<?>>
{
    public IterableSerializer(final JavaType elemType, final boolean staticTyping, final TypeSerializer vts, final BeanProperty property) {
        super(Iterable.class, elemType, staticTyping, vts, property, null);
    }
    
    @Override
    public ContainerSerializerBase<?> _withValueTypeSerializer(final TypeSerializer vts) {
        return new IterableSerializer(this._elementType, this._staticTyping, vts, this._property);
    }
    
    public void serializeContents(final Iterable<?> value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        final Iterator<?> it = value.iterator();
        if (it.hasNext()) {
            final TypeSerializer typeSer = this._valueTypeSerializer;
            JsonSerializer<Object> prevSerializer = null;
            Class<?> prevClass = null;
            do {
                final Object elem = it.next();
                if (elem == null) {
                    provider.defaultSerializeNull(jgen);
                }
                else {
                    final Class<?> cc = elem.getClass();
                    JsonSerializer<Object> currSerializer;
                    if (cc == prevClass) {
                        currSerializer = prevSerializer;
                    }
                    else {
                        currSerializer = (prevSerializer = provider.findValueSerializer(cc, this._property));
                        prevClass = cc;
                    }
                    if (typeSer == null) {
                        currSerializer.serialize(elem, jgen, provider);
                    }
                    else {
                        currSerializer.serializeWithType(elem, jgen, provider, typeSer);
                    }
                }
            } while (it.hasNext());
        }
    }
}
