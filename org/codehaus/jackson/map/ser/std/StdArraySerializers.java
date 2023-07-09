// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.JsonNode;
import java.lang.reflect.Type;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.map.ResolvableSerializer;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.TypeSerializer;

public class StdArraySerializers
{
    protected StdArraySerializers() {
    }
    
    public abstract static class ArraySerializerBase<T> extends ContainerSerializerBase<T>
    {
        protected final TypeSerializer _valueTypeSerializer;
        protected final BeanProperty _property;
        
        protected ArraySerializerBase(final Class<T> cls, final TypeSerializer vts, final BeanProperty property) {
            super(cls);
            this._valueTypeSerializer = vts;
            this._property = property;
        }
        
        @Override
        public final void serialize(final T value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeStartArray();
            this.serializeContents(value, jgen, provider);
            jgen.writeEndArray();
        }
        
        @Override
        public final void serializeWithType(final T value, final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonGenerationException {
            typeSer.writeTypePrefixForArray(value, jgen);
            this.serializeContents(value, jgen, provider);
            typeSer.writeTypeSuffixForArray(value, jgen);
        }
        
        protected abstract void serializeContents(final T p0, final JsonGenerator p1, final SerializerProvider p2) throws IOException, JsonGenerationException;
    }
    
    @JacksonStdImpl
    public static final class StringArraySerializer extends ArraySerializerBase<String[]> implements ResolvableSerializer
    {
        protected JsonSerializer<Object> _elementSerializer;
        
        public StringArraySerializer(final BeanProperty prop) {
            super(String[].class, null, prop);
        }
        
        @Override
        public ContainerSerializerBase<?> _withValueTypeSerializer(final TypeSerializer vts) {
            return this;
        }
        
        public void serializeContents(final String[] value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            final int len = value.length;
            if (len == 0) {
                return;
            }
            if (this._elementSerializer != null) {
                this.serializeContentsSlow(value, jgen, provider, this._elementSerializer);
                return;
            }
            for (int i = 0; i < len; ++i) {
                final String str = value[i];
                if (str == null) {
                    jgen.writeNull();
                }
                else {
                    jgen.writeString(value[i]);
                }
            }
        }
        
        private void serializeContentsSlow(final String[] value, final JsonGenerator jgen, final SerializerProvider provider, final JsonSerializer<Object> ser) throws IOException, JsonGenerationException {
            for (int i = 0, len = value.length; i < len; ++i) {
                final String str = value[i];
                if (str == null) {
                    provider.defaultSerializeNull(jgen);
                }
                else {
                    ser.serialize(value[i], jgen, provider);
                }
            }
        }
        
        public void resolve(final SerializerProvider provider) throws JsonMappingException {
            final JsonSerializer<Object> ser = provider.findValueSerializer(String.class, this._property);
            if (ser != null && ser.getClass().getAnnotation(JacksonStdImpl.class) == null) {
                this._elementSerializer = ser;
            }
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            final ObjectNode o = this.createSchemaNode("array", true);
            o.put("items", this.createSchemaNode("string"));
            return o;
        }
    }
    
    @JacksonStdImpl
    public static final class BooleanArraySerializer extends ArraySerializerBase<boolean[]>
    {
        public BooleanArraySerializer() {
            super(boolean[].class, null, null);
        }
        
        @Override
        public ContainerSerializerBase<?> _withValueTypeSerializer(final TypeSerializer vts) {
            return this;
        }
        
        public void serializeContents(final boolean[] value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            for (int i = 0, len = value.length; i < len; ++i) {
                jgen.writeBoolean(value[i]);
            }
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            final ObjectNode o = this.createSchemaNode("array", true);
            o.put("items", this.createSchemaNode("boolean"));
            return o;
        }
    }
    
    @JacksonStdImpl
    public static final class ByteArraySerializer extends SerializerBase<byte[]>
    {
        public ByteArraySerializer() {
            super(byte[].class);
        }
        
        @Override
        public void serialize(final byte[] value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeBinary(value);
        }
        
        @Override
        public void serializeWithType(final byte[] value, final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonGenerationException {
            typeSer.writeTypePrefixForScalar(value, jgen);
            jgen.writeBinary(value);
            typeSer.writeTypeSuffixForScalar(value, jgen);
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            final ObjectNode o = this.createSchemaNode("array", true);
            final ObjectNode itemSchema = this.createSchemaNode("string");
            o.put("items", itemSchema);
            return o;
        }
    }
    
    @JacksonStdImpl
    public static final class ShortArraySerializer extends ArraySerializerBase<short[]>
    {
        public ShortArraySerializer() {
            this((TypeSerializer)null);
        }
        
        public ShortArraySerializer(final TypeSerializer vts) {
            super(short[].class, vts, null);
        }
        
        @Override
        public ContainerSerializerBase<?> _withValueTypeSerializer(final TypeSerializer vts) {
            return new ShortArraySerializer(vts);
        }
        
        public void serializeContents(final short[] value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            for (int i = 0, len = value.length; i < len; ++i) {
                jgen.writeNumber(value[i]);
            }
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            final ObjectNode o = this.createSchemaNode("array", true);
            o.put("items", this.createSchemaNode("integer"));
            return o;
        }
    }
    
    @JacksonStdImpl
    public static final class CharArraySerializer extends SerializerBase<char[]>
    {
        public CharArraySerializer() {
            super(char[].class);
        }
        
        @Override
        public void serialize(final char[] value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            if (provider.isEnabled(SerializationConfig.Feature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS)) {
                jgen.writeStartArray();
                this._writeArrayContents(jgen, value);
                jgen.writeEndArray();
            }
            else {
                jgen.writeString(value, 0, value.length);
            }
        }
        
        @Override
        public void serializeWithType(final char[] value, final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer) throws IOException, JsonGenerationException {
            if (provider.isEnabled(SerializationConfig.Feature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS)) {
                typeSer.writeTypePrefixForArray(value, jgen);
                this._writeArrayContents(jgen, value);
                typeSer.writeTypeSuffixForArray(value, jgen);
            }
            else {
                typeSer.writeTypePrefixForScalar(value, jgen);
                jgen.writeString(value, 0, value.length);
                typeSer.writeTypeSuffixForScalar(value, jgen);
            }
        }
        
        private final void _writeArrayContents(final JsonGenerator jgen, final char[] value) throws IOException, JsonGenerationException {
            for (int i = 0, len = value.length; i < len; ++i) {
                jgen.writeString(value, i, 1);
            }
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            final ObjectNode o = this.createSchemaNode("array", true);
            final ObjectNode itemSchema = this.createSchemaNode("string");
            itemSchema.put("type", "string");
            o.put("items", itemSchema);
            return o;
        }
    }
    
    @JacksonStdImpl
    public static final class IntArraySerializer extends ArraySerializerBase<int[]>
    {
        public IntArraySerializer() {
            super(int[].class, null, null);
        }
        
        @Override
        public ContainerSerializerBase<?> _withValueTypeSerializer(final TypeSerializer vts) {
            return this;
        }
        
        public void serializeContents(final int[] value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            for (int i = 0, len = value.length; i < len; ++i) {
                jgen.writeNumber(value[i]);
            }
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            final ObjectNode o = this.createSchemaNode("array", true);
            o.put("items", this.createSchemaNode("integer"));
            return o;
        }
    }
    
    @JacksonStdImpl
    public static final class LongArraySerializer extends ArraySerializerBase<long[]>
    {
        public LongArraySerializer() {
            this((TypeSerializer)null);
        }
        
        public LongArraySerializer(final TypeSerializer vts) {
            super(long[].class, vts, null);
        }
        
        @Override
        public ContainerSerializerBase<?> _withValueTypeSerializer(final TypeSerializer vts) {
            return new LongArraySerializer(vts);
        }
        
        public void serializeContents(final long[] value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            for (int i = 0, len = value.length; i < len; ++i) {
                jgen.writeNumber(value[i]);
            }
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            final ObjectNode o = this.createSchemaNode("array", true);
            o.put("items", this.createSchemaNode("number", true));
            return o;
        }
    }
    
    @JacksonStdImpl
    public static final class FloatArraySerializer extends ArraySerializerBase<float[]>
    {
        public FloatArraySerializer() {
            this((TypeSerializer)null);
        }
        
        public FloatArraySerializer(final TypeSerializer vts) {
            super(float[].class, vts, null);
        }
        
        @Override
        public ContainerSerializerBase<?> _withValueTypeSerializer(final TypeSerializer vts) {
            return new FloatArraySerializer(vts);
        }
        
        public void serializeContents(final float[] value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            for (int i = 0, len = value.length; i < len; ++i) {
                jgen.writeNumber(value[i]);
            }
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            final ObjectNode o = this.createSchemaNode("array", true);
            o.put("items", this.createSchemaNode("number"));
            return o;
        }
    }
    
    @JacksonStdImpl
    public static final class DoubleArraySerializer extends ArraySerializerBase<double[]>
    {
        public DoubleArraySerializer() {
            super(double[].class, null, null);
        }
        
        @Override
        public ContainerSerializerBase<?> _withValueTypeSerializer(final TypeSerializer vts) {
            return this;
        }
        
        public void serializeContents(final double[] value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            for (int i = 0, len = value.length; i < len; ++i) {
                jgen.writeNumber(value[i]);
            }
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            final ObjectNode o = this.createSchemaNode("array", true);
            o.put("items", this.createSchemaNode("number"));
            return o;
        }
    }
}
