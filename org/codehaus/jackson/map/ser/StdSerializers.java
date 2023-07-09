// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser;

import org.codehaus.jackson.map.ser.std.SerializableWithTypeSerializer;
import org.codehaus.jackson.map.ser.std.SerializableSerializer;
import java.sql.Time;
import java.sql.Date;
import org.codehaus.jackson.map.ser.std.DateSerializer;
import org.codehaus.jackson.map.ser.std.CalendarSerializer;
import java.math.BigInteger;
import java.math.BigDecimal;
import org.codehaus.jackson.map.ser.std.ScalarSerializerBase;
import org.codehaus.jackson.JsonNode;
import java.lang.reflect.Type;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.map.ser.std.NonTypedScalarSerializerBase;

public class StdSerializers
{
    protected StdSerializers() {
    }
    
    @JacksonStdImpl
    public static final class BooleanSerializer extends NonTypedScalarSerializerBase<Boolean>
    {
        final boolean _forPrimitive;
        
        public BooleanSerializer(final boolean forPrimitive) {
            super(Boolean.class);
            this._forPrimitive = forPrimitive;
        }
        
        @Override
        public void serialize(final Boolean value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeBoolean(value);
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            return this.createSchemaNode("boolean", !this._forPrimitive);
        }
    }
    
    @Deprecated
    @JacksonStdImpl
    public static final class StringSerializer extends NonTypedScalarSerializerBase<String>
    {
        public StringSerializer() {
            super(String.class);
        }
        
        @Override
        public void serialize(final String value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeString(value);
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            return this.createSchemaNode("string", true);
        }
    }
    
    @JacksonStdImpl
    public static final class IntegerSerializer extends NonTypedScalarSerializerBase<Integer>
    {
        public IntegerSerializer() {
            super(Integer.class);
        }
        
        @Override
        public void serialize(final Integer value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeNumber(value);
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            return this.createSchemaNode("integer", true);
        }
    }
    
    @JacksonStdImpl
    public static final class IntLikeSerializer extends ScalarSerializerBase<Number>
    {
        static final IntLikeSerializer instance;
        
        public IntLikeSerializer() {
            super(Number.class);
        }
        
        @Override
        public void serialize(final Number value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeNumber(value.intValue());
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            return this.createSchemaNode("integer", true);
        }
        
        static {
            instance = new IntLikeSerializer();
        }
    }
    
    @JacksonStdImpl
    public static final class LongSerializer extends ScalarSerializerBase<Long>
    {
        static final LongSerializer instance;
        
        public LongSerializer() {
            super(Long.class);
        }
        
        @Override
        public void serialize(final Long value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeNumber(value);
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            return this.createSchemaNode("number", true);
        }
        
        static {
            instance = new LongSerializer();
        }
    }
    
    @JacksonStdImpl
    public static final class FloatSerializer extends ScalarSerializerBase<Float>
    {
        static final FloatSerializer instance;
        
        public FloatSerializer() {
            super(Float.class);
        }
        
        @Override
        public void serialize(final Float value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeNumber(value);
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            return this.createSchemaNode("number", true);
        }
        
        static {
            instance = new FloatSerializer();
        }
    }
    
    @JacksonStdImpl
    public static final class DoubleSerializer extends NonTypedScalarSerializerBase<Double>
    {
        static final DoubleSerializer instance;
        
        public DoubleSerializer() {
            super(Double.class);
        }
        
        @Override
        public void serialize(final Double value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeNumber(value);
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            return this.createSchemaNode("number", true);
        }
        
        static {
            instance = new DoubleSerializer();
        }
    }
    
    @JacksonStdImpl
    public static final class NumberSerializer extends ScalarSerializerBase<Number>
    {
        public static final NumberSerializer instance;
        
        public NumberSerializer() {
            super(Number.class);
        }
        
        @Override
        public void serialize(final Number value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            if (value instanceof BigDecimal) {
                jgen.writeNumber((BigDecimal)value);
            }
            else if (value instanceof BigInteger) {
                jgen.writeNumber((BigInteger)value);
            }
            else if (value instanceof Integer) {
                jgen.writeNumber(value.intValue());
            }
            else if (value instanceof Long) {
                jgen.writeNumber(value.longValue());
            }
            else if (value instanceof Double) {
                jgen.writeNumber(value.doubleValue());
            }
            else if (value instanceof Float) {
                jgen.writeNumber(value.floatValue());
            }
            else if (value instanceof Byte || value instanceof Short) {
                jgen.writeNumber(value.intValue());
            }
            else {
                jgen.writeNumber(value.toString());
            }
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            return this.createSchemaNode("number", true);
        }
        
        static {
            instance = new NumberSerializer();
        }
    }
    
    @JacksonStdImpl
    @Deprecated
    public static final class CalendarSerializer extends org.codehaus.jackson.map.ser.std.CalendarSerializer
    {
    }
    
    @Deprecated
    @JacksonStdImpl
    public static final class UtilDateSerializer extends DateSerializer
    {
    }
    
    @JacksonStdImpl
    public static final class SqlDateSerializer extends ScalarSerializerBase<Date>
    {
        public SqlDateSerializer() {
            super(Date.class);
        }
        
        @Override
        public void serialize(final Date value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeString(value.toString());
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            return this.createSchemaNode("string", true);
        }
    }
    
    @JacksonStdImpl
    public static final class SqlTimeSerializer extends ScalarSerializerBase<Time>
    {
        public SqlTimeSerializer() {
            super(Time.class);
        }
        
        @Override
        public void serialize(final Time value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeString(value.toString());
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            return this.createSchemaNode("string", true);
        }
    }
    
    @Deprecated
    @JacksonStdImpl
    public static final class SerializableSerializer extends org.codehaus.jackson.map.ser.std.SerializableSerializer
    {
    }
    
    @Deprecated
    @JacksonStdImpl
    public static final class SerializableWithTypeSerializer extends org.codehaus.jackson.map.ser.std.SerializableWithTypeSerializer
    {
    }
}
