// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ext;

import org.codehaus.jackson.JsonNode;
import java.lang.reflect.Type;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.ReadableInstant;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormatter;
import org.codehaus.jackson.map.ser.std.SerializerBase;
import org.codehaus.jackson.map.ser.std.ToStringSerializer;
import org.joda.time.Period;
import org.joda.time.DateMidnight;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.DateTime;
import java.util.Collection;
import java.util.HashMap;
import org.codehaus.jackson.map.JsonSerializer;
import java.util.Map;
import org.codehaus.jackson.map.util.Provider;

public class JodaSerializers implements Provider<Map.Entry<Class<?>, JsonSerializer<?>>>
{
    static final HashMap<Class<?>, JsonSerializer<?>> _serializers;
    
    public Collection<Map.Entry<Class<?>, JsonSerializer<?>>> provide() {
        return JodaSerializers._serializers.entrySet();
    }
    
    static {
        (_serializers = new HashMap<Class<?>, JsonSerializer<?>>()).put(DateTime.class, new DateTimeSerializer());
        JodaSerializers._serializers.put(LocalDateTime.class, new LocalDateTimeSerializer());
        JodaSerializers._serializers.put(LocalDate.class, new LocalDateSerializer());
        JodaSerializers._serializers.put(DateMidnight.class, new DateMidnightSerializer());
        JodaSerializers._serializers.put(Period.class, ToStringSerializer.instance);
    }
    
    protected abstract static class JodaSerializer<T> extends SerializerBase<T>
    {
        static final DateTimeFormatter _localDateTimeFormat;
        static final DateTimeFormatter _localDateFormat;
        
        protected JodaSerializer(final Class<T> cls) {
            super(cls);
        }
        
        protected String printLocalDateTime(final ReadablePartial dateValue) throws IOException, JsonProcessingException {
            return JodaSerializer._localDateTimeFormat.print(dateValue);
        }
        
        protected String printLocalDate(final ReadablePartial dateValue) throws IOException, JsonProcessingException {
            return JodaSerializer._localDateFormat.print(dateValue);
        }
        
        protected String printLocalDate(final ReadableInstant dateValue) throws IOException, JsonProcessingException {
            return JodaSerializer._localDateFormat.print(dateValue);
        }
        
        static {
            _localDateTimeFormat = ISODateTimeFormat.dateTime();
            _localDateFormat = ISODateTimeFormat.date();
        }
    }
    
    public static final class DateTimeSerializer extends JodaSerializer<DateTime>
    {
        public DateTimeSerializer() {
            super(DateTime.class);
        }
        
        @Override
        public void serialize(final DateTime value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            if (provider.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS)) {
                jgen.writeNumber(value.getMillis());
            }
            else {
                jgen.writeString(value.toString());
            }
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            return this.createSchemaNode(provider.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS) ? "number" : "string", true);
        }
    }
    
    public static final class LocalDateTimeSerializer extends JodaSerializer<LocalDateTime>
    {
        public LocalDateTimeSerializer() {
            super(LocalDateTime.class);
        }
        
        @Override
        public void serialize(final LocalDateTime dt, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            if (provider.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS)) {
                jgen.writeStartArray();
                jgen.writeNumber(dt.year().get());
                jgen.writeNumber(dt.monthOfYear().get());
                jgen.writeNumber(dt.dayOfMonth().get());
                jgen.writeNumber(dt.hourOfDay().get());
                jgen.writeNumber(dt.minuteOfHour().get());
                jgen.writeNumber(dt.secondOfMinute().get());
                jgen.writeNumber(dt.millisOfSecond().get());
                jgen.writeEndArray();
            }
            else {
                jgen.writeString(this.printLocalDateTime((ReadablePartial)dt));
            }
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            return this.createSchemaNode(provider.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS) ? "array" : "string", true);
        }
    }
    
    public static final class LocalDateSerializer extends JodaSerializer<LocalDate>
    {
        public LocalDateSerializer() {
            super(LocalDate.class);
        }
        
        @Override
        public void serialize(final LocalDate dt, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            if (provider.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS)) {
                jgen.writeStartArray();
                jgen.writeNumber(dt.year().get());
                jgen.writeNumber(dt.monthOfYear().get());
                jgen.writeNumber(dt.dayOfMonth().get());
                jgen.writeEndArray();
            }
            else {
                jgen.writeString(this.printLocalDate((ReadablePartial)dt));
            }
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            return this.createSchemaNode(provider.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS) ? "array" : "string", true);
        }
    }
    
    public static final class DateMidnightSerializer extends JodaSerializer<DateMidnight>
    {
        public DateMidnightSerializer() {
            super(DateMidnight.class);
        }
        
        @Override
        public void serialize(final DateMidnight dt, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            if (provider.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS)) {
                jgen.writeStartArray();
                jgen.writeNumber(dt.year().get());
                jgen.writeNumber(dt.monthOfYear().get());
                jgen.writeNumber(dt.dayOfMonth().get());
                jgen.writeEndArray();
            }
            else {
                jgen.writeString(this.printLocalDate((ReadableInstant)dt));
            }
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            return this.createSchemaNode(provider.isEnabled(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS) ? "array" : "string", true);
        }
    }
}
