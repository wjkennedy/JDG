// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ext;

import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
import org.joda.time.DateMidnight;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalDate;
import org.joda.time.DateTimeZone;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.joda.time.format.ISODateTimeFormat;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.JsonParser;
import org.joda.time.format.DateTimeFormatter;
import org.codehaus.jackson.map.deser.std.StdScalarDeserializer;
import java.util.Arrays;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadableDateTime;
import org.joda.time.DateTime;
import java.util.Collection;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.codehaus.jackson.map.util.Provider;

public class JodaDeserializers implements Provider<StdDeserializer<?>>
{
    public Collection<StdDeserializer<?>> provide() {
        return (Collection<StdDeserializer<?>>)Arrays.asList(new DateTimeDeserializer(DateTime.class), new DateTimeDeserializer(ReadableDateTime.class), new DateTimeDeserializer(ReadableInstant.class), new LocalDateDeserializer(), new LocalDateTimeDeserializer(), new DateMidnightDeserializer(), new PeriodDeserializer());
    }
    
    abstract static class JodaDeserializer<T> extends StdScalarDeserializer<T>
    {
        static final DateTimeFormatter _localDateTimeFormat;
        
        protected JodaDeserializer(final Class<T> cls) {
            super(cls);
        }
        
        protected DateTime parseLocal(final JsonParser jp) throws IOException, JsonProcessingException {
            final String str = jp.getText().trim();
            if (str.length() == 0) {
                return null;
            }
            return JodaDeserializer._localDateTimeFormat.parseDateTime(str);
        }
        
        static {
            _localDateTimeFormat = ISODateTimeFormat.localDateOptionalTimeParser();
        }
    }
    
    public static class DateTimeDeserializer<T extends ReadableInstant> extends JodaDeserializer<T>
    {
        public DateTimeDeserializer(final Class<T> cls) {
            super(cls);
        }
        
        @Override
        public T deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final JsonToken t = jp.getCurrentToken();
            if (t == JsonToken.VALUE_NUMBER_INT) {
                return (T)new DateTime(jp.getLongValue(), DateTimeZone.UTC);
            }
            if (t != JsonToken.VALUE_STRING) {
                throw ctxt.mappingException(this.getValueClass());
            }
            final String str = jp.getText().trim();
            if (str.length() == 0) {
                return null;
            }
            return (T)new DateTime((Object)str, DateTimeZone.UTC);
        }
    }
    
    public static class LocalDateDeserializer extends JodaDeserializer<LocalDate>
    {
        public LocalDateDeserializer() {
            super(LocalDate.class);
        }
        
        @Override
        public LocalDate deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (jp.isExpectedStartArrayToken()) {
                jp.nextToken();
                final int year = jp.getIntValue();
                jp.nextToken();
                final int month = jp.getIntValue();
                jp.nextToken();
                final int day = jp.getIntValue();
                if (jp.nextToken() != JsonToken.END_ARRAY) {
                    throw ctxt.wrongTokenException(jp, JsonToken.END_ARRAY, "after LocalDate ints");
                }
                return new LocalDate(year, month, day);
            }
            else {
                switch (jp.getCurrentToken()) {
                    case VALUE_NUMBER_INT: {
                        return new LocalDate(jp.getLongValue());
                    }
                    case VALUE_STRING: {
                        final DateTime local = this.parseLocal(jp);
                        if (local == null) {
                            return null;
                        }
                        return local.toLocalDate();
                    }
                    default: {
                        throw ctxt.wrongTokenException(jp, JsonToken.START_ARRAY, "expected JSON Array, String or Number");
                    }
                }
            }
        }
    }
    
    public static class LocalDateTimeDeserializer extends JodaDeserializer<LocalDateTime>
    {
        public LocalDateTimeDeserializer() {
            super(LocalDateTime.class);
        }
        
        @Override
        public LocalDateTime deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (jp.isExpectedStartArrayToken()) {
                jp.nextToken();
                final int year = jp.getIntValue();
                jp.nextToken();
                final int month = jp.getIntValue();
                jp.nextToken();
                final int day = jp.getIntValue();
                jp.nextToken();
                final int hour = jp.getIntValue();
                jp.nextToken();
                final int minute = jp.getIntValue();
                jp.nextToken();
                final int second = jp.getIntValue();
                int millisecond = 0;
                if (jp.nextToken() != JsonToken.END_ARRAY) {
                    millisecond = jp.getIntValue();
                    jp.nextToken();
                }
                if (jp.getCurrentToken() != JsonToken.END_ARRAY) {
                    throw ctxt.wrongTokenException(jp, JsonToken.END_ARRAY, "after LocalDateTime ints");
                }
                return new LocalDateTime(year, month, day, hour, minute, second, millisecond);
            }
            else {
                switch (jp.getCurrentToken()) {
                    case VALUE_NUMBER_INT: {
                        return new LocalDateTime(jp.getLongValue());
                    }
                    case VALUE_STRING: {
                        final DateTime local = this.parseLocal(jp);
                        if (local == null) {
                            return null;
                        }
                        return local.toLocalDateTime();
                    }
                    default: {
                        throw ctxt.wrongTokenException(jp, JsonToken.START_ARRAY, "expected JSON Array or Number");
                    }
                }
            }
        }
    }
    
    public static class DateMidnightDeserializer extends JodaDeserializer<DateMidnight>
    {
        public DateMidnightDeserializer() {
            super(DateMidnight.class);
        }
        
        @Override
        public DateMidnight deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (jp.isExpectedStartArrayToken()) {
                jp.nextToken();
                final int year = jp.getIntValue();
                jp.nextToken();
                final int month = jp.getIntValue();
                jp.nextToken();
                final int day = jp.getIntValue();
                if (jp.nextToken() != JsonToken.END_ARRAY) {
                    throw ctxt.wrongTokenException(jp, JsonToken.END_ARRAY, "after DateMidnight ints");
                }
                return new DateMidnight(year, month, day);
            }
            else {
                switch (jp.getCurrentToken()) {
                    case VALUE_NUMBER_INT: {
                        return new DateMidnight(jp.getLongValue());
                    }
                    case VALUE_STRING: {
                        final DateTime local = this.parseLocal(jp);
                        if (local == null) {
                            return null;
                        }
                        return local.toDateMidnight();
                    }
                    default: {
                        throw ctxt.wrongTokenException(jp, JsonToken.START_ARRAY, "expected JSON Array, Number or String");
                    }
                }
            }
        }
    }
    
    public static class PeriodDeserializer extends JodaDeserializer<ReadablePeriod>
    {
        public PeriodDeserializer() {
            super(ReadablePeriod.class);
        }
        
        @Override
        public ReadablePeriod deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            switch (jp.getCurrentToken()) {
                case VALUE_NUMBER_INT: {
                    return (ReadablePeriod)new Period(jp.getLongValue());
                }
                case VALUE_STRING: {
                    return (ReadablePeriod)new Period((Object)jp.getText());
                }
                default: {
                    throw ctxt.wrongTokenException(jp, JsonToken.START_ARRAY, "expected JSON Number or String");
                }
            }
        }
    }
}
