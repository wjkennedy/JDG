// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import java.util.Iterator;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.map.ser.impl.PropertySerializerMap;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import java.util.List;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.type.JavaType;

public class StdContainerSerializers
{
    protected StdContainerSerializers() {
    }
    
    public static ContainerSerializerBase<?> indexedListSerializer(final JavaType elemType, final boolean staticTyping, final TypeSerializer vts, final BeanProperty property, final JsonSerializer<Object> valueSerializer) {
        return new IndexedListSerializer(elemType, staticTyping, vts, property, valueSerializer);
    }
    
    public static ContainerSerializerBase<?> collectionSerializer(final JavaType elemType, final boolean staticTyping, final TypeSerializer vts, final BeanProperty property, final JsonSerializer<Object> valueSerializer) {
        return new CollectionSerializer(elemType, staticTyping, vts, property, valueSerializer);
    }
    
    public static ContainerSerializerBase<?> iteratorSerializer(final JavaType elemType, final boolean staticTyping, final TypeSerializer vts, final BeanProperty property) {
        return new IteratorSerializer(elemType, staticTyping, vts, property);
    }
    
    public static ContainerSerializerBase<?> iterableSerializer(final JavaType elemType, final boolean staticTyping, final TypeSerializer vts, final BeanProperty property) {
        return new IterableSerializer(elemType, staticTyping, vts, property);
    }
    
    public static JsonSerializer<?> enumSetSerializer(final JavaType enumType, final BeanProperty property) {
        return new EnumSetSerializer(enumType, property);
    }
    
    @JacksonStdImpl
    public static class IndexedListSerializer extends AsArraySerializerBase<List<?>>
    {
        public IndexedListSerializer(final JavaType elemType, final boolean staticTyping, final TypeSerializer vts, final BeanProperty property, final JsonSerializer<Object> valueSerializer) {
            super(List.class, elemType, staticTyping, vts, property, valueSerializer);
        }
        
        @Override
        public ContainerSerializerBase<?> _withValueTypeSerializer(final TypeSerializer vts) {
            return new IndexedListSerializer(this._elementType, this._staticTyping, vts, this._property, this._elementSerializer);
        }
        
        public void serializeContents(final List<?> value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            if (this._elementSerializer != null) {
                this.serializeContentsUsing(value, jgen, provider, this._elementSerializer);
                return;
            }
            if (this._valueTypeSerializer != null) {
                this.serializeTypedContents(value, jgen, provider);
                return;
            }
            final int len = value.size();
            if (len == 0) {
                return;
            }
            int i = 0;
            try {
                PropertySerializerMap serializers = this._dynamicSerializers;
                while (i < len) {
                    final Object elem = value.get(i);
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
                        serializer.serialize(elem, jgen, provider);
                    }
                    ++i;
                }
            }
            catch (final Exception e) {
                this.wrapAndThrow(provider, e, value, i);
            }
        }
        
        public void serializeContentsUsing(final List<?> value, final JsonGenerator jgen, final SerializerProvider provider, final JsonSerializer<Object> ser) throws IOException, JsonGenerationException {
            final int len = value.size();
            if (len == 0) {
                return;
            }
            final TypeSerializer typeSer = this._valueTypeSerializer;
            for (int i = 0; i < len; ++i) {
                final Object elem = value.get(i);
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
                }
                catch (final Exception e) {
                    this.wrapAndThrow(provider, e, value, i);
                }
            }
        }
        
        public void serializeTypedContents(final List<?> value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            final int len = value.size();
            if (len == 0) {
                return;
            }
            int i = 0;
            try {
                final TypeSerializer typeSer = this._valueTypeSerializer;
                PropertySerializerMap serializers = this._dynamicSerializers;
                while (i < len) {
                    final Object elem = value.get(i);
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
                        serializer.serializeWithType(elem, jgen, provider, typeSer);
                    }
                    ++i;
                }
            }
            catch (final Exception e) {
                this.wrapAndThrow(provider, e, value, i);
            }
        }
    }
    
    @JacksonStdImpl
    public static class IteratorSerializer extends AsArraySerializerBase<Iterator<?>>
    {
        public IteratorSerializer(final JavaType elemType, final boolean staticTyping, final TypeSerializer vts, final BeanProperty property) {
            super(Iterator.class, elemType, staticTyping, vts, property, null);
        }
        
        @Override
        public ContainerSerializerBase<?> _withValueTypeSerializer(final TypeSerializer vts) {
            return new IteratorSerializer(this._elementType, this._staticTyping, vts, this._property);
        }
        
        public void serializeContents(final Iterator<?> value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            if (value.hasNext()) {
                final TypeSerializer typeSer = this._valueTypeSerializer;
                JsonSerializer<Object> prevSerializer = null;
                Class<?> prevClass = null;
                do {
                    final Object elem = value.next();
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
                } while (value.hasNext());
            }
        }
    }
}
