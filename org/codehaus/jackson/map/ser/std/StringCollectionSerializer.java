// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.JsonProcessingException;
import java.util.Iterator;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.map.ResolvableSerializer;
import java.util.Collection;

@JacksonStdImpl
public class StringCollectionSerializer extends StaticListSerializerBase<Collection<String>> implements ResolvableSerializer
{
    protected JsonSerializer<String> _serializer;
    
    @Deprecated
    public StringCollectionSerializer(final BeanProperty property) {
        this(property, null);
    }
    
    public StringCollectionSerializer(final BeanProperty property, final JsonSerializer<?> ser) {
        super(Collection.class, property);
        this._serializer = (JsonSerializer<String>)ser;
    }
    
    @Override
    protected JsonNode contentSchema() {
        return this.createSchemaNode("string", true);
    }
    
    public void resolve(final SerializerProvider provider) throws JsonMappingException {
        if (this._serializer == null) {
            final JsonSerializer<?> ser = provider.findValueSerializer(String.class, this._property);
            if (!this.isDefaultSerializer(ser)) {
                this._serializer = (JsonSerializer<String>)ser;
            }
        }
    }
    
    @Override
    public void serialize(final Collection<String> value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        jgen.writeStartArray();
        if (this._serializer == null) {
            this.serializeContents(value, jgen, provider);
        }
        else {
            this.serializeUsingCustom(value, jgen, provider);
        }
        jgen.writeEndArray();
    }
    
    @Override
    public void serializeWithType(final Collection<String> value, final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonGenerationException {
        typeSer.writeTypePrefixForArray(value, jgen);
        if (this._serializer == null) {
            this.serializeContents(value, jgen, provider);
        }
        else {
            this.serializeUsingCustom(value, jgen, provider);
        }
        typeSer.writeTypeSuffixForArray(value, jgen);
    }
    
    private final void serializeContents(final Collection<String> value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        if (this._serializer != null) {
            this.serializeUsingCustom(value, jgen, provider);
            return;
        }
        int i = 0;
        for (final String str : value) {
            try {
                if (str == null) {
                    provider.defaultSerializeNull(jgen);
                }
                else {
                    jgen.writeString(str);
                }
                ++i;
            }
            catch (final Exception e) {
                this.wrapAndThrow(provider, e, value, i);
            }
        }
    }
    
    private void serializeUsingCustom(final Collection<String> value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        final JsonSerializer<String> ser = this._serializer;
        final int i = 0;
        for (final String str : value) {
            try {
                if (str == null) {
                    provider.defaultSerializeNull(jgen);
                }
                else {
                    ser.serialize(str, jgen, provider);
                }
            }
            catch (final Exception e) {
                this.wrapAndThrow(provider, e, value, i);
            }
        }
    }
}
