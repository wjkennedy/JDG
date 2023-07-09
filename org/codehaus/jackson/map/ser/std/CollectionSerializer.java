// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.map.ser.impl.PropertySerializerMap;
import java.util.Iterator;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.type.JavaType;
import java.util.Collection;

public class CollectionSerializer extends AsArraySerializerBase<Collection<?>>
{
    public CollectionSerializer(final JavaType elemType, final boolean staticTyping, final TypeSerializer vts, final BeanProperty property, final JsonSerializer<Object> valueSerializer) {
        super(Collection.class, elemType, staticTyping, vts, property, valueSerializer);
    }
    
    @Override
    public ContainerSerializerBase<?> _withValueTypeSerializer(final TypeSerializer vts) {
        return new CollectionSerializer(this._elementType, this._staticTyping, vts, this._property, this._elementSerializer);
    }
    
    public void serializeContents(final Collection<?> value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        if (this._elementSerializer != null) {
            this.serializeContentsUsing(value, jgen, provider, this._elementSerializer);
            return;
        }
        final Iterator<?> it = value.iterator();
        if (!it.hasNext()) {
            return;
        }
        PropertySerializerMap serializers = this._dynamicSerializers;
        final TypeSerializer typeSer = this._valueTypeSerializer;
        int i = 0;
        try {
            do {
                final Object elem = it.next();
                if (elem == null) {
                    provider.defaultSerializeNull(jgen);
                }
                else {
                    final Class<?> cc = elem.getClass();
                    JsonSerializer<Object> serializer = serializers.serializerFor(cc);
                    if (serializer == null) {
                        if (this._elementType.hasGenericTypes()) {
                            serializer = this._findAndAddDynamic(serializers, provider.constructSpecializedType(this._elementType, cc), provider);
                        }
                        else {
                            serializer = this._findAndAddDynamic(serializers, cc, provider);
                        }
                        serializers = this._dynamicSerializers;
                    }
                    if (typeSer == null) {
                        serializer.serialize(elem, jgen, provider);
                    }
                    else {
                        serializer.serializeWithType(elem, jgen, provider, typeSer);
                    }
                }
                ++i;
            } while (it.hasNext());
        }
        catch (final Exception e) {
            this.wrapAndThrow(provider, e, value, i);
        }
    }
    
    public void serializeContentsUsing(final Collection<?> value, final JsonGenerator jgen, final SerializerProvider provider, final JsonSerializer<Object> ser) throws IOException, JsonGenerationException {
        final Iterator<?> it = value.iterator();
        if (it.hasNext()) {
            final TypeSerializer typeSer = this._valueTypeSerializer;
            int i = 0;
            do {
                final Object elem = it.next();
                try {
                    if (elem == null) {
                        provider.defaultSerializeNull(jgen);
                    }
                    else if (typeSer == null) {
                        ser.serialize(elem, jgen, provider);
                    }
                    else {
                        ser.serializeWithType(elem, jgen, provider, typeSer);
                    }
                    ++i;
                }
                catch (final Exception e) {
                    this.wrapAndThrow(provider, e, value, i);
                }
            } while (it.hasNext());
        }
    }
}
