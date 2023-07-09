// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import java.util.UUID;
import java.util.Calendar;
import java.util.Date;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.util.EnumResolver;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.io.NumberInput;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.KeyDeserializer;

public abstract class StdKeyDeserializer extends KeyDeserializer
{
    protected final Class<?> _keyClass;
    
    protected StdKeyDeserializer(final Class<?> cls) {
        this._keyClass = cls;
    }
    
    @Override
    public final Object deserializeKey(final String key, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (key == null) {
            return null;
        }
        try {
            final Object result = this._parse(key, ctxt);
            if (result != null) {
                return result;
            }
        }
        catch (final Exception re) {
            throw ctxt.weirdKeyException(this._keyClass, key, "not a valid representation: " + re.getMessage());
        }
        throw ctxt.weirdKeyException(this._keyClass, key, "not a valid representation");
    }
    
    public Class<?> getKeyClass() {
        return this._keyClass;
    }
    
    protected abstract Object _parse(final String p0, final DeserializationContext p1) throws Exception;
    
    protected int _parseInt(final String key) throws IllegalArgumentException {
        return Integer.parseInt(key);
    }
    
    protected long _parseLong(final String key) throws IllegalArgumentException {
        return Long.parseLong(key);
    }
    
    protected double _parseDouble(final String key) throws IllegalArgumentException {
        return NumberInput.parseDouble(key);
    }
    
    static final class StringKD extends StdKeyDeserializer
    {
        private static final StringKD sString;
        private static final StringKD sObject;
        
        private StringKD(final Class<?> nominalType) {
            super(nominalType);
        }
        
        public static StringKD forType(final Class<?> nominalType) {
            if (nominalType == String.class) {
                return StringKD.sString;
            }
            if (nominalType == Object.class) {
                return StringKD.sObject;
            }
            return new StringKD(nominalType);
        }
        
        public String _parse(final String key, final DeserializationContext ctxt) throws JsonMappingException {
            return key;
        }
        
        static {
            sString = new StringKD(String.class);
            sObject = new StringKD(Object.class);
        }
    }
    
    static final class BoolKD extends StdKeyDeserializer
    {
        BoolKD() {
            super(Boolean.class);
        }
        
        public Boolean _parse(final String key, final DeserializationContext ctxt) throws JsonMappingException {
            if ("true".equals(key)) {
                return Boolean.TRUE;
            }
            if ("false".equals(key)) {
                return Boolean.FALSE;
            }
            throw ctxt.weirdKeyException(this._keyClass, key, "value not 'true' or 'false'");
        }
    }
    
    static final class ByteKD extends StdKeyDeserializer
    {
        ByteKD() {
            super(Byte.class);
        }
        
        public Byte _parse(final String key, final DeserializationContext ctxt) throws JsonMappingException {
            final int value = this._parseInt(key);
            if (value < -128 || value > 255) {
                throw ctxt.weirdKeyException(this._keyClass, key, "overflow, value can not be represented as 8-bit value");
            }
            return (byte)value;
        }
    }
    
    static final class ShortKD extends StdKeyDeserializer
    {
        ShortKD() {
            super(Integer.class);
        }
        
        public Short _parse(final String key, final DeserializationContext ctxt) throws JsonMappingException {
            final int value = this._parseInt(key);
            if (value < -32768 || value > 32767) {
                throw ctxt.weirdKeyException(this._keyClass, key, "overflow, value can not be represented as 16-bit value");
            }
            return (short)value;
        }
    }
    
    static final class CharKD extends StdKeyDeserializer
    {
        CharKD() {
            super(Character.class);
        }
        
        public Character _parse(final String key, final DeserializationContext ctxt) throws JsonMappingException {
            if (key.length() == 1) {
                return key.charAt(0);
            }
            throw ctxt.weirdKeyException(this._keyClass, key, "can only convert 1-character Strings");
        }
    }
    
    static final class IntKD extends StdKeyDeserializer
    {
        IntKD() {
            super(Integer.class);
        }
        
        public Integer _parse(final String key, final DeserializationContext ctxt) throws JsonMappingException {
            return this._parseInt(key);
        }
    }
    
    static final class LongKD extends StdKeyDeserializer
    {
        LongKD() {
            super(Long.class);
        }
        
        public Long _parse(final String key, final DeserializationContext ctxt) throws JsonMappingException {
            return this._parseLong(key);
        }
    }
    
    static final class DoubleKD extends StdKeyDeserializer
    {
        DoubleKD() {
            super(Double.class);
        }
        
        public Double _parse(final String key, final DeserializationContext ctxt) throws JsonMappingException {
            return this._parseDouble(key);
        }
    }
    
    static final class FloatKD extends StdKeyDeserializer
    {
        FloatKD() {
            super(Float.class);
        }
        
        public Float _parse(final String key, final DeserializationContext ctxt) throws JsonMappingException {
            return (float)this._parseDouble(key);
        }
    }
    
    static final class EnumKD extends StdKeyDeserializer
    {
        protected final EnumResolver<?> _resolver;
        protected final AnnotatedMethod _factory;
        
        protected EnumKD(final EnumResolver<?> er, final AnnotatedMethod factory) {
            super(er.getEnumClass());
            this._resolver = er;
            this._factory = factory;
        }
        
        public Object _parse(final String key, final DeserializationContext ctxt) throws JsonMappingException {
            if (this._factory != null) {
                try {
                    return this._factory.call1(key);
                }
                catch (final Exception e) {
                    ClassUtil.unwrapAndThrowAsIAE(e);
                }
            }
            final Enum<?> e2 = (Enum<?>)this._resolver.findEnum(key);
            if (e2 == null) {
                throw ctxt.weirdKeyException(this._keyClass, key, "not one of values for Enum class");
            }
            return e2;
        }
    }
    
    static final class StringCtorKeyDeserializer extends StdKeyDeserializer
    {
        protected final Constructor<?> _ctor;
        
        public StringCtorKeyDeserializer(final Constructor<?> ctor) {
            super(ctor.getDeclaringClass());
            this._ctor = ctor;
        }
        
        public Object _parse(final String key, final DeserializationContext ctxt) throws Exception {
            return this._ctor.newInstance(key);
        }
    }
    
    static final class StringFactoryKeyDeserializer extends StdKeyDeserializer
    {
        final Method _factoryMethod;
        
        public StringFactoryKeyDeserializer(final Method fm) {
            super(fm.getDeclaringClass());
            this._factoryMethod = fm;
        }
        
        public Object _parse(final String key, final DeserializationContext ctxt) throws Exception {
            return this._factoryMethod.invoke(null, key);
        }
    }
    
    static final class DateKD extends StdKeyDeserializer
    {
        protected DateKD() {
            super(Date.class);
        }
        
        public Date _parse(final String key, final DeserializationContext ctxt) throws IllegalArgumentException, JsonMappingException {
            return ctxt.parseDate(key);
        }
    }
    
    static final class CalendarKD extends StdKeyDeserializer
    {
        protected CalendarKD() {
            super(Calendar.class);
        }
        
        public Calendar _parse(final String key, final DeserializationContext ctxt) throws IllegalArgumentException, JsonMappingException {
            final Date date = ctxt.parseDate(key);
            return (date == null) ? null : ctxt.constructCalendar(date);
        }
    }
    
    static final class UuidKD extends StdKeyDeserializer
    {
        protected UuidKD() {
            super(UUID.class);
        }
        
        public UUID _parse(final String key, final DeserializationContext ctxt) throws IllegalArgumentException, JsonMappingException {
            return UUID.fromString(key);
        }
    }
}
