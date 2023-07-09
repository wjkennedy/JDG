// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import org.codehaus.jackson.map.util.ArrayBuilders;
import org.codehaus.jackson.Base64Variants;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.util.ObjectBuffer;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import java.lang.reflect.Type;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.type.JavaType;
import java.util.HashMap;

public class PrimitiveArrayDeserializers
{
    HashMap<JavaType, JsonDeserializer<Object>> _allDeserializers;
    static final PrimitiveArrayDeserializers instance;
    
    protected PrimitiveArrayDeserializers() {
        this._allDeserializers = new HashMap<JavaType, JsonDeserializer<Object>>();
        this.add(Boolean.TYPE, new BooleanDeser());
        this.add(Byte.TYPE, new ByteDeser());
        this.add(Short.TYPE, new ShortDeser());
        this.add(Integer.TYPE, new IntDeser());
        this.add(Long.TYPE, new LongDeser());
        this.add(Float.TYPE, new FloatDeser());
        this.add(Double.TYPE, new DoubleDeser());
        this.add(String.class, new StringDeser());
        this.add(Character.TYPE, new CharDeser());
    }
    
    public static HashMap<JavaType, JsonDeserializer<Object>> getAll() {
        return PrimitiveArrayDeserializers.instance._allDeserializers;
    }
    
    private void add(final Class<?> cls, final JsonDeserializer<?> deser) {
        this._allDeserializers.put(TypeFactory.defaultInstance().constructType(cls), (JsonDeserializer<Object>)deser);
    }
    
    public Object deserializeWithType(final JsonParser jp, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
        return typeDeserializer.deserializeTypedFromArray(jp, ctxt);
    }
    
    static {
        instance = new PrimitiveArrayDeserializers();
    }
    
    abstract static class Base<T> extends StdDeserializer<T>
    {
        protected Base(final Class<T> cls) {
            super(cls);
        }
        
        @Override
        public Object deserializeWithType(final JsonParser jp, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
            return typeDeserializer.deserializeTypedFromArray(jp, ctxt);
        }
    }
    
    @JacksonStdImpl
    static final class StringDeser extends Base<String[]>
    {
        public StringDeser() {
            super(String[].class);
        }
        
        @Override
        public String[] deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (!jp.isExpectedStartArrayToken()) {
                return this.handleNonArray(jp, ctxt);
            }
            final ObjectBuffer buffer = ctxt.leaseObjectBuffer();
            Object[] chunk = buffer.resetAndStart();
            int ix = 0;
            JsonToken t;
            while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
                final String value = (t == JsonToken.VALUE_NULL) ? null : jp.getText();
                if (ix >= chunk.length) {
                    chunk = buffer.appendCompletedChunk(chunk);
                    ix = 0;
                }
                chunk[ix++] = value;
            }
            final String[] result = buffer.completeAndClearBuffer(chunk, ix, String.class);
            ctxt.returnObjectBuffer(buffer);
            return result;
        }
        
        private final String[] handleNonArray(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
                if (jp.getCurrentToken() == JsonToken.VALUE_STRING && ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)) {
                    final String str = jp.getText();
                    if (str.length() == 0) {
                        return null;
                    }
                }
                throw ctxt.mappingException(this._valueClass);
            }
            return new String[] { (jp.getCurrentToken() == JsonToken.VALUE_NULL) ? null : jp.getText() };
        }
    }
    
    @JacksonStdImpl
    static final class CharDeser extends Base<char[]>
    {
        public CharDeser() {
            super(char[].class);
        }
        
        @Override
        public char[] deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonToken t = jp.getCurrentToken();
            if (t == JsonToken.VALUE_STRING) {
                final char[] buffer = jp.getTextCharacters();
                final int offset = jp.getTextOffset();
                final int len = jp.getTextLength();
                final char[] result = new char[len];
                System.arraycopy(buffer, offset, result, 0, len);
                return result;
            }
            if (jp.isExpectedStartArrayToken()) {
                final StringBuilder sb = new StringBuilder(64);
                while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
                    if (t != JsonToken.VALUE_STRING) {
                        throw ctxt.mappingException(Character.TYPE);
                    }
                    final String str = jp.getText();
                    if (str.length() != 1) {
                        throw JsonMappingException.from(jp, "Can not convert a JSON String of length " + str.length() + " into a char element of char array");
                    }
                    sb.append(str.charAt(0));
                }
                return sb.toString().toCharArray();
            }
            if (t == JsonToken.VALUE_EMBEDDED_OBJECT) {
                final Object ob = jp.getEmbeddedObject();
                if (ob == null) {
                    return null;
                }
                if (ob instanceof char[]) {
                    return (char[])ob;
                }
                if (ob instanceof String) {
                    return ((String)ob).toCharArray();
                }
                if (ob instanceof byte[]) {
                    return Base64Variants.getDefaultVariant().encode((byte[])ob, false).toCharArray();
                }
            }
            throw ctxt.mappingException(this._valueClass);
        }
    }
    
    @JacksonStdImpl
    static final class BooleanDeser extends Base<boolean[]>
    {
        public BooleanDeser() {
            super(boolean[].class);
        }
        
        @Override
        public boolean[] deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (!jp.isExpectedStartArrayToken()) {
                return this.handleNonArray(jp, ctxt);
            }
            final ArrayBuilders.BooleanBuilder builder = ctxt.getArrayBuilders().getBooleanBuilder();
            boolean[] chunk = builder.resetAndStart();
            int ix = 0;
            while (jp.nextToken() != JsonToken.END_ARRAY) {
                final boolean value = this._parseBooleanPrimitive(jp, ctxt);
                if (ix >= chunk.length) {
                    chunk = builder.appendCompletedChunk(chunk, ix);
                    ix = 0;
                }
                chunk[ix++] = value;
            }
            return builder.completeAndClearBuffer(chunk, ix);
        }
        
        private final boolean[] handleNonArray(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (jp.getCurrentToken() == JsonToken.VALUE_STRING && ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT) && jp.getText().length() == 0) {
                return null;
            }
            if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
                throw ctxt.mappingException(this._valueClass);
            }
            return new boolean[] { this._parseBooleanPrimitive(jp, ctxt) };
        }
    }
    
    @JacksonStdImpl
    static final class ByteDeser extends Base<byte[]>
    {
        public ByteDeser() {
            super(byte[].class);
        }
        
        @Override
        public byte[] deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonToken t = jp.getCurrentToken();
            if (t == JsonToken.VALUE_STRING) {
                return jp.getBinaryValue(ctxt.getBase64Variant());
            }
            if (t == JsonToken.VALUE_EMBEDDED_OBJECT) {
                final Object ob = jp.getEmbeddedObject();
                if (ob == null) {
                    return null;
                }
                if (ob instanceof byte[]) {
                    return (byte[])ob;
                }
            }
            if (!jp.isExpectedStartArrayToken()) {
                return this.handleNonArray(jp, ctxt);
            }
            final ArrayBuilders.ByteBuilder builder = ctxt.getArrayBuilders().getByteBuilder();
            byte[] chunk = builder.resetAndStart();
            int ix = 0;
            while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
                byte value;
                if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
                    value = jp.getByteValue();
                }
                else {
                    if (t != JsonToken.VALUE_NULL) {
                        throw ctxt.mappingException(this._valueClass.getComponentType());
                    }
                    value = 0;
                }
                if (ix >= chunk.length) {
                    chunk = builder.appendCompletedChunk(chunk, ix);
                    ix = 0;
                }
                chunk[ix++] = value;
            }
            return builder.completeAndClearBuffer(chunk, ix);
        }
        
        private final byte[] handleNonArray(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (jp.getCurrentToken() == JsonToken.VALUE_STRING && ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT) && jp.getText().length() == 0) {
                return null;
            }
            if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
                throw ctxt.mappingException(this._valueClass);
            }
            final JsonToken t = jp.getCurrentToken();
            byte value;
            if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
                value = jp.getByteValue();
            }
            else {
                if (t != JsonToken.VALUE_NULL) {
                    throw ctxt.mappingException(this._valueClass.getComponentType());
                }
                value = 0;
            }
            return new byte[] { value };
        }
    }
    
    @JacksonStdImpl
    static final class ShortDeser extends Base<short[]>
    {
        public ShortDeser() {
            super(short[].class);
        }
        
        @Override
        public short[] deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (!jp.isExpectedStartArrayToken()) {
                return this.handleNonArray(jp, ctxt);
            }
            final ArrayBuilders.ShortBuilder builder = ctxt.getArrayBuilders().getShortBuilder();
            short[] chunk = builder.resetAndStart();
            int ix = 0;
            while (jp.nextToken() != JsonToken.END_ARRAY) {
                final short value = this._parseShortPrimitive(jp, ctxt);
                if (ix >= chunk.length) {
                    chunk = builder.appendCompletedChunk(chunk, ix);
                    ix = 0;
                }
                chunk[ix++] = value;
            }
            return builder.completeAndClearBuffer(chunk, ix);
        }
        
        private final short[] handleNonArray(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (jp.getCurrentToken() == JsonToken.VALUE_STRING && ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT) && jp.getText().length() == 0) {
                return null;
            }
            if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
                throw ctxt.mappingException(this._valueClass);
            }
            return new short[] { this._parseShortPrimitive(jp, ctxt) };
        }
    }
    
    @JacksonStdImpl
    static final class IntDeser extends Base<int[]>
    {
        public IntDeser() {
            super(int[].class);
        }
        
        @Override
        public int[] deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (!jp.isExpectedStartArrayToken()) {
                return this.handleNonArray(jp, ctxt);
            }
            final ArrayBuilders.IntBuilder builder = ctxt.getArrayBuilders().getIntBuilder();
            int[] chunk = builder.resetAndStart();
            int ix = 0;
            while (jp.nextToken() != JsonToken.END_ARRAY) {
                final int value = this._parseIntPrimitive(jp, ctxt);
                if (ix >= chunk.length) {
                    chunk = builder.appendCompletedChunk(chunk, ix);
                    ix = 0;
                }
                chunk[ix++] = value;
            }
            return builder.completeAndClearBuffer(chunk, ix);
        }
        
        private final int[] handleNonArray(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (jp.getCurrentToken() == JsonToken.VALUE_STRING && ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT) && jp.getText().length() == 0) {
                return null;
            }
            if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
                throw ctxt.mappingException(this._valueClass);
            }
            return new int[] { this._parseIntPrimitive(jp, ctxt) };
        }
    }
    
    @JacksonStdImpl
    static final class LongDeser extends Base<long[]>
    {
        public LongDeser() {
            super(long[].class);
        }
        
        @Override
        public long[] deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (!jp.isExpectedStartArrayToken()) {
                return this.handleNonArray(jp, ctxt);
            }
            final ArrayBuilders.LongBuilder builder = ctxt.getArrayBuilders().getLongBuilder();
            long[] chunk = builder.resetAndStart();
            int ix = 0;
            while (jp.nextToken() != JsonToken.END_ARRAY) {
                final long value = this._parseLongPrimitive(jp, ctxt);
                if (ix >= chunk.length) {
                    chunk = builder.appendCompletedChunk(chunk, ix);
                    ix = 0;
                }
                chunk[ix++] = value;
            }
            return builder.completeAndClearBuffer(chunk, ix);
        }
        
        private final long[] handleNonArray(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (jp.getCurrentToken() == JsonToken.VALUE_STRING && ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT) && jp.getText().length() == 0) {
                return null;
            }
            if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
                throw ctxt.mappingException(this._valueClass);
            }
            return new long[] { this._parseLongPrimitive(jp, ctxt) };
        }
    }
    
    @JacksonStdImpl
    static final class FloatDeser extends Base<float[]>
    {
        public FloatDeser() {
            super(float[].class);
        }
        
        @Override
        public float[] deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (!jp.isExpectedStartArrayToken()) {
                return this.handleNonArray(jp, ctxt);
            }
            final ArrayBuilders.FloatBuilder builder = ctxt.getArrayBuilders().getFloatBuilder();
            float[] chunk = builder.resetAndStart();
            int ix = 0;
            while (jp.nextToken() != JsonToken.END_ARRAY) {
                final float value = this._parseFloatPrimitive(jp, ctxt);
                if (ix >= chunk.length) {
                    chunk = builder.appendCompletedChunk(chunk, ix);
                    ix = 0;
                }
                chunk[ix++] = value;
            }
            return builder.completeAndClearBuffer(chunk, ix);
        }
        
        private final float[] handleNonArray(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (jp.getCurrentToken() == JsonToken.VALUE_STRING && ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT) && jp.getText().length() == 0) {
                return null;
            }
            if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
                throw ctxt.mappingException(this._valueClass);
            }
            return new float[] { this._parseFloatPrimitive(jp, ctxt) };
        }
    }
    
    @JacksonStdImpl
    static final class DoubleDeser extends Base<double[]>
    {
        public DoubleDeser() {
            super(double[].class);
        }
        
        @Override
        public double[] deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (!jp.isExpectedStartArrayToken()) {
                return this.handleNonArray(jp, ctxt);
            }
            final ArrayBuilders.DoubleBuilder builder = ctxt.getArrayBuilders().getDoubleBuilder();
            double[] chunk = builder.resetAndStart();
            int ix = 0;
            while (jp.nextToken() != JsonToken.END_ARRAY) {
                final double value = this._parseDoublePrimitive(jp, ctxt);
                if (ix >= chunk.length) {
                    chunk = builder.appendCompletedChunk(chunk, ix);
                    ix = 0;
                }
                chunk[ix++] = value;
            }
            return builder.completeAndClearBuffer(chunk, ix);
        }
        
        private final double[] handleNonArray(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (jp.getCurrentToken() == JsonToken.VALUE_STRING && ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT) && jp.getText().length() == 0) {
                return null;
            }
            if (!ctxt.isEnabled(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)) {
                throw ctxt.mappingException(this._valueClass);
            }
            return new double[] { this._parseDoublePrimitive(jp, ctxt) };
        }
    }
}
