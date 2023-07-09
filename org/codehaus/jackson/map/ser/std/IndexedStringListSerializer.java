// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.JsonProcessingException;
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
import java.util.List;

@JacksonStdImpl
public final class IndexedStringListSerializer extends StaticListSerializerBase<List<String>> implements ResolvableSerializer
{
    protected JsonSerializer<String> _serializer;
    
    public IndexedStringListSerializer(final BeanProperty property) {
        this(property, null);
    }
    
    public IndexedStringListSerializer(final BeanProperty property, final JsonSerializer<?> ser) {
        super(List.class, property);
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
    public void serialize(final List<String> value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
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
    public void serializeWithType(final List<String> value, final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonGenerationException {
        typeSer.writeTypePrefixForArray(value, jgen);
        if (this._serializer == null) {
            this.serializeContents(value, jgen, provider);
        }
        else {
            this.serializeUsingCustom(value, jgen, provider);
        }
        typeSer.writeTypeSuffixForArray(value, jgen);
    }
    
    private final void serializeContents(final List<String> value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        int i = 0;
        try {
            for (int len = value.size(); i < len; ++i) {
                final String str = value.get(i);
                if (str == null) {
                    provider.defaultSerializeNull(jgen);
                }
                else {
                    jgen.writeString(str);
                }
            }
        }
        catch (final Exception e) {
            this.wrapAndThrow(provider, e, value, i);
        }
    }
    
    private final void serializeUsingCustom(final List<String> value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
        int i = 0;
        try {
            final int len = value.size();
            final JsonSerializer<String> ser = this._serializer;
            for (i = 0; i < len; ++i) {
                final String str = value.get(i);
                if (str == null) {
                    provider.defaultSerializeNull(jgen);
                }
                else {
                    ser.serialize(str, jgen, provider);
                }
            }
        }
        catch (final Exception e) {
            this.wrapAndThrow(provider, e, value, i);
        }
    }
}
