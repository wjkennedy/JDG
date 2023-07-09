// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import java.math.BigInteger;
import java.math.BigDecimal;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import java.util.Date;
import org.codehaus.jackson.io.NumberInput;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.annotate.JacksonStdImpl;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.JsonDeserializer;

public abstract class StdDeserializer<T> extends JsonDeserializer<T>
{
    protected final Class<?> _valueClass;
    
    protected StdDeserializer(final Class<?> vc) {
        this._valueClass = vc;
    }
    
    protected StdDeserializer(final JavaType valueType) {
        this._valueClass = ((valueType == null) ? null : valueType.getRawClass());
    }
    
    public Class<?> getValueClass() {
        return this._valueClass;
    }
    
    public JavaType getValueType() {
        return null;
    }
    
    protected boolean isDefaultSerializer(final JsonDeserializer<?> deserializer) {
        return deserializer != null && deserializer.getClass().getAnnotation(JacksonStdImpl.class) != null;
    }
    
    @Override
    public Object deserializeWithType(final JsonParser jp, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
        return typeDeserializer.deserializeTypedFromAny(jp, ctxt);
    }
    
    protected final boolean _parseBooleanPrimitive(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_TRUE) {
            return true;
        }
        if (t == JsonToken.VALUE_FALSE) {
            return false;
        }
        if (t == JsonToken.VALUE_NULL) {
            return false;
        }
        if (t == JsonToken.VALUE_NUMBER_INT) {
            if (jp.getNumberType() == JsonParser.NumberType.INT) {
                return jp.getIntValue() != 0;
            }
            return this._parseBooleanFromNumber(jp, ctxt);
        }
        else {
            if (t != JsonToken.VALUE_STRING) {
                throw ctxt.mappingException(this._valueClass, t);
            }
            final String text = jp.getText().trim();
            if ("true".equals(text)) {
                return true;
            }
            if ("false".equals(text) || text.length() == 0) {
                return Boolean.FALSE;
            }
            throw ctxt.weirdStringException(this._valueClass, "only \"true\" or \"false\" recognized");
        }
    }
    
    protected final Boolean _parseBoolean(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_TRUE) {
            return Boolean.TRUE;
        }
        if (t == JsonToken.VALUE_FALSE) {
            return Boolean.FALSE;
        }
        if (t == JsonToken.VALUE_NUMBER_INT) {
            if (jp.getNumberType() == JsonParser.NumberType.INT) {
                return (jp.getIntValue() == 0) ? Boolean.FALSE : Boolean.TRUE;
            }
            return this._parseBooleanFromNumber(jp, ctxt);
        }
        else {
            if (t == JsonToken.VALUE_NULL) {
                return this.getNullValue();
            }
            if (t != JsonToken.VALUE_STRING) {
                throw ctxt.mappingException(this._valueClass, t);
            }
            final String text = jp.getText().trim();
            if ("true".equals(text)) {
                return Boolean.TRUE;
            }
            if ("false".equals(text)) {
                return Boolean.FALSE;
            }
            if (text.length() == 0) {
                return this.getEmptyValue();
            }
            throw ctxt.weirdStringException(this._valueClass, "only \"true\" or \"false\" recognized");
        }
    }
    
    protected final boolean _parseBooleanFromNumber(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (jp.getNumberType() == JsonParser.NumberType.LONG) {
            return (jp.getLongValue() == 0L) ? Boolean.FALSE : Boolean.TRUE;
        }
        final String str = jp.getText();
        if ("0.0".equals(str) || "0".equals(str)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
    
    protected Byte _parseByte(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
            return jp.getByteValue();
        }
        if (t == JsonToken.VALUE_STRING) {
            final String text = jp.getText().trim();
            int value;
            try {
                final int len = text.length();
                if (len == 0) {
                    return this.getEmptyValue();
                }
                value = NumberInput.parseInt(text);
            }
            catch (final IllegalArgumentException iae) {
                throw ctxt.weirdStringException(this._valueClass, "not a valid Byte value");
            }
            if (value < -128 || value > 255) {
                throw ctxt.weirdStringException(this._valueClass, "overflow, value can not be represented as 8-bit value");
            }
            return (byte)value;
        }
        else {
            if (t == JsonToken.VALUE_NULL) {
                return this.getNullValue();
            }
            throw ctxt.mappingException(this._valueClass, t);
        }
    }
    
    protected Short _parseShort(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
            return jp.getShortValue();
        }
        if (t == JsonToken.VALUE_STRING) {
            final String text = jp.getText().trim();
            int value;
            try {
                final int len = text.length();
                if (len == 0) {
                    return this.getEmptyValue();
                }
                value = NumberInput.parseInt(text);
            }
            catch (final IllegalArgumentException iae) {
                throw ctxt.weirdStringException(this._valueClass, "not a valid Short value");
            }
            if (value < -32768 || value > 32767) {
                throw ctxt.weirdStringException(this._valueClass, "overflow, value can not be represented as 16-bit value");
            }
            return (short)value;
        }
        else {
            if (t == JsonToken.VALUE_NULL) {
                return this.getNullValue();
            }
            throw ctxt.mappingException(this._valueClass, t);
        }
    }
    
    protected final short _parseShortPrimitive(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final int value = this._parseIntPrimitive(jp, ctxt);
        if (value < -32768 || value > 32767) {
            throw ctxt.weirdStringException(this._valueClass, "overflow, value can not be represented as 16-bit value");
        }
        return (short)value;
    }
    
    protected final int _parseIntPrimitive(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
            return jp.getIntValue();
        }
        if (t == JsonToken.VALUE_STRING) {
            final String text = jp.getText().trim();
            try {
                final int len = text.length();
                if (len > 9) {
                    final long l = Long.parseLong(text);
                    if (l < -2147483648L || l > 2147483647L) {
                        throw ctxt.weirdStringException(this._valueClass, "Overflow: numeric value (" + text + ") out of range of int (" + Integer.MIN_VALUE + " - " + Integer.MAX_VALUE + ")");
                    }
                    return (int)l;
                }
                else {
                    if (len == 0) {
                        return 0;
                    }
                    return NumberInput.parseInt(text);
                }
            }
            catch (final IllegalArgumentException iae) {
                throw ctxt.weirdStringException(this._valueClass, "not a valid int value");
            }
        }
        if (t == JsonToken.VALUE_NULL) {
            return 0;
        }
        throw ctxt.mappingException(this._valueClass, t);
    }
    
    protected final Integer _parseInteger(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
            return jp.getIntValue();
        }
        if (t == JsonToken.VALUE_STRING) {
            final String text = jp.getText().trim();
            try {
                final int len = text.length();
                if (len > 9) {
                    final long l = Long.parseLong(text);
                    if (l < -2147483648L || l > 2147483647L) {
                        throw ctxt.weirdStringException(this._valueClass, "Overflow: numeric value (" + text + ") out of range of Integer (" + Integer.MIN_VALUE + " - " + Integer.MAX_VALUE + ")");
                    }
                    return (int)l;
                }
                else {
                    if (len == 0) {
                        return this.getEmptyValue();
                    }
                    return NumberInput.parseInt(text);
                }
            }
            catch (final IllegalArgumentException iae) {
                throw ctxt.weirdStringException(this._valueClass, "not a valid Integer value");
            }
        }
        if (t == JsonToken.VALUE_NULL) {
            return this.getNullValue();
        }
        throw ctxt.mappingException(this._valueClass, t);
    }
    
    protected final Long _parseLong(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
            return jp.getLongValue();
        }
        if (t == JsonToken.VALUE_STRING) {
            final String text = jp.getText().trim();
            if (text.length() == 0) {
                return this.getEmptyValue();
            }
            try {
                return NumberInput.parseLong(text);
            }
            catch (final IllegalArgumentException ex) {
                throw ctxt.weirdStringException(this._valueClass, "not a valid Long value");
            }
        }
        if (t == JsonToken.VALUE_NULL) {
            return this.getNullValue();
        }
        throw ctxt.mappingException(this._valueClass, t);
    }
    
    protected final long _parseLongPrimitive(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
            return jp.getLongValue();
        }
        if (t == JsonToken.VALUE_STRING) {
            final String text = jp.getText().trim();
            if (text.length() == 0) {
                return 0L;
            }
            try {
                return NumberInput.parseLong(text);
            }
            catch (final IllegalArgumentException ex) {
                throw ctxt.weirdStringException(this._valueClass, "not a valid long value");
            }
        }
        if (t == JsonToken.VALUE_NULL) {
            return 0L;
        }
        throw ctxt.mappingException(this._valueClass, t);
    }
    
    protected final Float _parseFloat(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
            return jp.getFloatValue();
        }
        if (t == JsonToken.VALUE_STRING) {
            final String text = jp.getText().trim();
            if (text.length() == 0) {
                return this.getEmptyValue();
            }
            switch (text.charAt(0)) {
                case 'I': {
                    if ("Infinity".equals(text) || "INF".equals(text)) {
                        return Float.POSITIVE_INFINITY;
                    }
                    break;
                }
                case 'N': {
                    if ("NaN".equals(text)) {
                        return Float.NaN;
                    }
                    break;
                }
                case '-': {
                    if ("-Infinity".equals(text) || "-INF".equals(text)) {
                        return Float.NEGATIVE_INFINITY;
                    }
                    break;
                }
            }
            try {
                return Float.parseFloat(text);
            }
            catch (final IllegalArgumentException ex) {
                throw ctxt.weirdStringException(this._valueClass, "not a valid Float value");
            }
        }
        if (t == JsonToken.VALUE_NULL) {
            return this.getNullValue();
        }
        throw ctxt.mappingException(this._valueClass, t);
    }
    
    protected final float _parseFloatPrimitive(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
            return jp.getFloatValue();
        }
        if (t == JsonToken.VALUE_STRING) {
            final String text = jp.getText().trim();
            if (text.length() == 0) {
                return 0.0f;
            }
            switch (text.charAt(0)) {
                case 'I': {
                    if ("Infinity".equals(text) || "INF".equals(text)) {
                        return Float.POSITIVE_INFINITY;
                    }
                    break;
                }
                case 'N': {
                    if ("NaN".equals(text)) {
                        return Float.NaN;
                    }
                    break;
                }
                case '-': {
                    if ("-Infinity".equals(text) || "-INF".equals(text)) {
                        return Float.NEGATIVE_INFINITY;
                    }
                    break;
                }
            }
            try {
                return Float.parseFloat(text);
            }
            catch (final IllegalArgumentException ex) {
                throw ctxt.weirdStringException(this._valueClass, "not a valid float value");
            }
        }
        if (t == JsonToken.VALUE_NULL) {
            return 0.0f;
        }
        throw ctxt.mappingException(this._valueClass, t);
    }
    
    protected final Double _parseDouble(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
            return jp.getDoubleValue();
        }
        if (t == JsonToken.VALUE_STRING) {
            final String text = jp.getText().trim();
            if (text.length() == 0) {
                return this.getEmptyValue();
            }
            switch (text.charAt(0)) {
                case 'I': {
                    if ("Infinity".equals(text) || "INF".equals(text)) {
                        return Double.POSITIVE_INFINITY;
                    }
                    break;
                }
                case 'N': {
                    if ("NaN".equals(text)) {
                        return Double.NaN;
                    }
                    break;
                }
                case '-': {
                    if ("-Infinity".equals(text) || "-INF".equals(text)) {
                        return Double.NEGATIVE_INFINITY;
                    }
                    break;
                }
            }
            try {
                return parseDouble(text);
            }
            catch (final IllegalArgumentException ex) {
                throw ctxt.weirdStringException(this._valueClass, "not a valid Double value");
            }
        }
        if (t == JsonToken.VALUE_NULL) {
            return this.getNullValue();
        }
        throw ctxt.mappingException(this._valueClass, t);
    }
    
    protected final double _parseDoublePrimitive(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
            return jp.getDoubleValue();
        }
        if (t == JsonToken.VALUE_STRING) {
            final String text = jp.getText().trim();
            if (text.length() == 0) {
                return 0.0;
            }
            switch (text.charAt(0)) {
                case 'I': {
                    if ("Infinity".equals(text) || "INF".equals(text)) {
                        return Double.POSITIVE_INFINITY;
                    }
                    break;
                }
                case 'N': {
                    if ("NaN".equals(text)) {
                        return Double.NaN;
                    }
                    break;
                }
                case '-': {
                    if ("-Infinity".equals(text) || "-INF".equals(text)) {
                        return Double.NEGATIVE_INFINITY;
                    }
                    break;
                }
            }
            try {
                return parseDouble(text);
            }
            catch (final IllegalArgumentException ex) {
                throw ctxt.weirdStringException(this._valueClass, "not a valid double value");
            }
        }
        if (t == JsonToken.VALUE_NULL) {
            return 0.0;
        }
        throw ctxt.mappingException(this._valueClass, t);
    }
    
    protected Date _parseDate(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT) {
            return new Date(jp.getLongValue());
        }
        if (t == JsonToken.VALUE_NULL) {
            return this.getNullValue();
        }
        if (t == JsonToken.VALUE_STRING) {
            try {
                final String str = jp.getText().trim();
                if (str.length() == 0) {
                    return this.getEmptyValue();
                }
                return ctxt.parseDate(str);
            }
            catch (final IllegalArgumentException iae) {
                throw ctxt.weirdStringException(this._valueClass, "not a valid representation (error: " + iae.getMessage() + ")");
            }
        }
        throw ctxt.mappingException(this._valueClass, t);
    }
    
    protected static final double parseDouble(final String numStr) throws NumberFormatException {
        if ("2.2250738585072012e-308".equals(numStr)) {
            return Double.MIN_NORMAL;
        }
        return Double.parseDouble(numStr);
    }
    
    protected JsonDeserializer<Object> findDeserializer(final DeserializationConfig config, final DeserializerProvider provider, final JavaType type, final BeanProperty property) throws JsonMappingException {
        final JsonDeserializer<Object> deser = provider.findValueDeserializer(config, type, property);
        return deser;
    }
    
    protected void handleUnknownProperty(final JsonParser jp, final DeserializationContext ctxt, Object instanceOrClass, final String propName) throws IOException, JsonProcessingException {
        if (instanceOrClass == null) {
            instanceOrClass = this.getValueClass();
        }
        if (ctxt.handleUnknownProperty(jp, this, instanceOrClass, propName)) {
            return;
        }
        this.reportUnknownProperty(ctxt, instanceOrClass, propName);
        jp.skipChildren();
    }
    
    protected void reportUnknownProperty(final DeserializationContext ctxt, final Object instanceOrClass, final String fieldName) throws IOException, JsonProcessingException {
        if (ctxt.isEnabled(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES)) {
            throw ctxt.unknownFieldException(instanceOrClass, fieldName);
        }
    }
    
    protected abstract static class PrimitiveOrWrapperDeserializer<T> extends StdScalarDeserializer<T>
    {
        final T _nullValue;
        
        protected PrimitiveOrWrapperDeserializer(final Class<T> vc, final T nvl) {
            super(vc);
            this._nullValue = nvl;
        }
        
        @Override
        public final T getNullValue() {
            return this._nullValue;
        }
    }
    
    @JacksonStdImpl
    public static final class BooleanDeserializer extends PrimitiveOrWrapperDeserializer<Boolean>
    {
        public BooleanDeserializer(final Class<Boolean> cls, final Boolean nvl) {
            super(cls, nvl);
        }
        
        @Override
        public Boolean deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return this._parseBoolean(jp, ctxt);
        }
        
        @Override
        public Boolean deserializeWithType(final JsonParser jp, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
            return this._parseBoolean(jp, ctxt);
        }
    }
    
    @JacksonStdImpl
    public static final class ByteDeserializer extends PrimitiveOrWrapperDeserializer<Byte>
    {
        public ByteDeserializer(final Class<Byte> cls, final Byte nvl) {
            super(cls, nvl);
        }
        
        @Override
        public Byte deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return this._parseByte(jp, ctxt);
        }
    }
    
    @JacksonStdImpl
    public static final class ShortDeserializer extends PrimitiveOrWrapperDeserializer<Short>
    {
        public ShortDeserializer(final Class<Short> cls, final Short nvl) {
            super(cls, nvl);
        }
        
        @Override
        public Short deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return this._parseShort(jp, ctxt);
        }
    }
    
    @JacksonStdImpl
    public static final class CharacterDeserializer extends PrimitiveOrWrapperDeserializer<Character>
    {
        public CharacterDeserializer(final Class<Character> cls, final Character nvl) {
            super(cls, nvl);
        }
        
        @Override
        public Character deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final JsonToken t = jp.getCurrentToken();
            if (t == JsonToken.VALUE_NUMBER_INT) {
                final int value = jp.getIntValue();
                if (value >= 0 && value <= 65535) {
                    return (char)value;
                }
            }
            else if (t == JsonToken.VALUE_STRING) {
                final String text = jp.getText();
                if (text.length() == 1) {
                    return text.charAt(0);
                }
                if (text.length() == 0) {
                    return this.getEmptyValue();
                }
            }
            throw ctxt.mappingException(this._valueClass, t);
        }
    }
    
    @JacksonStdImpl
    public static final class IntegerDeserializer extends PrimitiveOrWrapperDeserializer<Integer>
    {
        public IntegerDeserializer(final Class<Integer> cls, final Integer nvl) {
            super(cls, nvl);
        }
        
        @Override
        public Integer deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return this._parseInteger(jp, ctxt);
        }
        
        @Override
        public Integer deserializeWithType(final JsonParser jp, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
            return this._parseInteger(jp, ctxt);
        }
    }
    
    @JacksonStdImpl
    public static final class LongDeserializer extends PrimitiveOrWrapperDeserializer<Long>
    {
        public LongDeserializer(final Class<Long> cls, final Long nvl) {
            super(cls, nvl);
        }
        
        @Override
        public Long deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return this._parseLong(jp, ctxt);
        }
    }
    
    @JacksonStdImpl
    public static final class FloatDeserializer extends PrimitiveOrWrapperDeserializer<Float>
    {
        public FloatDeserializer(final Class<Float> cls, final Float nvl) {
            super(cls, nvl);
        }
        
        @Override
        public Float deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return this._parseFloat(jp, ctxt);
        }
    }
    
    @JacksonStdImpl
    public static final class DoubleDeserializer extends PrimitiveOrWrapperDeserializer<Double>
    {
        public DoubleDeserializer(final Class<Double> cls, final Double nvl) {
            super(cls, nvl);
        }
        
        @Override
        public Double deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return this._parseDouble(jp, ctxt);
        }
        
        @Override
        public Double deserializeWithType(final JsonParser jp, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
            return this._parseDouble(jp, ctxt);
        }
    }
    
    @JacksonStdImpl
    public static final class NumberDeserializer extends StdScalarDeserializer<Number>
    {
        public NumberDeserializer() {
            super(Number.class);
        }
        
        @Override
        public Number deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final JsonToken t = jp.getCurrentToken();
            if (t == JsonToken.VALUE_NUMBER_INT) {
                if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_INTEGER_FOR_INTS)) {
                    return jp.getBigIntegerValue();
                }
                return jp.getNumberValue();
            }
            else {
                if (t != JsonToken.VALUE_NUMBER_FLOAT) {
                    if (t == JsonToken.VALUE_STRING) {
                        final String text = jp.getText().trim();
                        try {
                            if (text.indexOf(46) >= 0) {
                                if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS)) {
                                    return new BigDecimal(text);
                                }
                                return new Double(text);
                            }
                            else {
                                if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_INTEGER_FOR_INTS)) {
                                    return new BigInteger(text);
                                }
                                final long value = Long.parseLong(text);
                                if (value <= 2147483647L && value >= -2147483648L) {
                                    return (int)value;
                                }
                                return value;
                            }
                        }
                        catch (final IllegalArgumentException iae) {
                            throw ctxt.weirdStringException(this._valueClass, "not a valid number");
                        }
                    }
                    throw ctxt.mappingException(this._valueClass, t);
                }
                if (ctxt.isEnabled(DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS)) {
                    return jp.getDecimalValue();
                }
                return jp.getDoubleValue();
            }
        }
        
        @Override
        public Object deserializeWithType(final JsonParser jp, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
            switch (jp.getCurrentToken()) {
                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT:
                case VALUE_STRING: {
                    return this.deserialize(jp, ctxt);
                }
                default: {
                    return typeDeserializer.deserializeTypedFromScalar(jp, ctxt);
                }
            }
        }
    }
    
    @JacksonStdImpl
    public static class BigDecimalDeserializer extends StdScalarDeserializer<BigDecimal>
    {
        public BigDecimalDeserializer() {
            super(BigDecimal.class);
        }
        
        @Override
        public BigDecimal deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final JsonToken t = jp.getCurrentToken();
            if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
                return jp.getDecimalValue();
            }
            if (t == JsonToken.VALUE_STRING) {
                final String text = jp.getText().trim();
                if (text.length() == 0) {
                    return null;
                }
                try {
                    return new BigDecimal(text);
                }
                catch (final IllegalArgumentException iae) {
                    throw ctxt.weirdStringException(this._valueClass, "not a valid representation");
                }
            }
            throw ctxt.mappingException(this._valueClass, t);
        }
    }
    
    @JacksonStdImpl
    public static class BigIntegerDeserializer extends StdScalarDeserializer<BigInteger>
    {
        public BigIntegerDeserializer() {
            super(BigInteger.class);
        }
        
        @Override
        public BigInteger deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final JsonToken t = jp.getCurrentToken();
            if (t == JsonToken.VALUE_NUMBER_INT) {
                switch (jp.getNumberType()) {
                    case INT:
                    case LONG: {
                        return BigInteger.valueOf(jp.getLongValue());
                    }
                }
            }
            else {
                if (t == JsonToken.VALUE_NUMBER_FLOAT) {
                    return jp.getDecimalValue().toBigInteger();
                }
                if (t != JsonToken.VALUE_STRING) {
                    throw ctxt.mappingException(this._valueClass, t);
                }
            }
            final String text = jp.getText().trim();
            if (text.length() == 0) {
                return null;
            }
            try {
                return new BigInteger(text);
            }
            catch (final IllegalArgumentException iae) {
                throw ctxt.weirdStringException(this._valueClass, "not a valid representation");
            }
        }
    }
    
    public static class SqlDateDeserializer extends StdScalarDeserializer<java.sql.Date>
    {
        public SqlDateDeserializer() {
            super(java.sql.Date.class);
        }
        
        @Override
        public java.sql.Date deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final Date d = this._parseDate(jp, ctxt);
            return (d == null) ? null : new java.sql.Date(d.getTime());
        }
    }
    
    public static class StackTraceElementDeserializer extends StdScalarDeserializer<StackTraceElement>
    {
        public StackTraceElementDeserializer() {
            super(StackTraceElement.class);
        }
        
        @Override
        public StackTraceElement deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonToken t = jp.getCurrentToken();
            if (t == JsonToken.START_OBJECT) {
                String className = "";
                String methodName = "";
                String fileName = "";
                int lineNumber = -1;
                while ((t = jp.nextValue()) != JsonToken.END_OBJECT) {
                    final String propName = jp.getCurrentName();
                    if ("className".equals(propName)) {
                        className = jp.getText();
                    }
                    else if ("fileName".equals(propName)) {
                        fileName = jp.getText();
                    }
                    else if ("lineNumber".equals(propName)) {
                        if (!t.isNumeric()) {
                            throw JsonMappingException.from(jp, "Non-numeric token (" + t + ") for property 'lineNumber'");
                        }
                        lineNumber = jp.getIntValue();
                    }
                    else if ("methodName".equals(propName)) {
                        methodName = jp.getText();
                    }
                    else {
                        if ("nativeMethod".equals(propName)) {
                            continue;
                        }
                        this.handleUnknownProperty(jp, ctxt, this._valueClass, propName);
                    }
                }
                return new StackTraceElement(className, methodName, fileName, lineNumber);
            }
            throw ctxt.mappingException(this._valueClass, t);
        }
    }
}
