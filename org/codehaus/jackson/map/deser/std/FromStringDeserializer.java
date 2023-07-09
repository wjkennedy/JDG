// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.deser.std;

import java.util.TimeZone;
import java.nio.charset.Charset;
import java.net.InetAddress;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.Currency;
import java.net.URI;
import java.net.URL;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.util.UUID;
import org.codehaus.jackson.JsonProcessingException;
import java.io.IOException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.JsonParser;
import java.util.ArrayList;

public abstract class FromStringDeserializer<T> extends StdScalarDeserializer<T>
{
    protected FromStringDeserializer(final Class<?> vc) {
        super(vc);
    }
    
    public static Iterable<FromStringDeserializer<?>> all() {
        final ArrayList<FromStringDeserializer<?>> all = new ArrayList<FromStringDeserializer<?>>();
        all.add(new UUIDDeserializer());
        all.add(new URLDeserializer());
        all.add(new URIDeserializer());
        all.add(new CurrencyDeserializer());
        all.add(new PatternDeserializer());
        all.add(new LocaleDeserializer());
        all.add(new InetAddressDeserializer());
        all.add(new TimeZoneDeserializer());
        all.add(new CharsetDeserializer());
        return all;
    }
    
    @Override
    public final T deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (jp.getCurrentToken() == JsonToken.VALUE_STRING) {
            final String text = jp.getText().trim();
            if (text.length() == 0) {
                return null;
            }
            try {
                final T result = this._deserialize(text, ctxt);
                if (result != null) {
                    return result;
                }
            }
            catch (final IllegalArgumentException ex) {}
            throw ctxt.weirdStringException(this._valueClass, "not a valid textual representation");
        }
        else {
            if (jp.getCurrentToken() != JsonToken.VALUE_EMBEDDED_OBJECT) {
                throw ctxt.mappingException(this._valueClass);
            }
            final Object ob = jp.getEmbeddedObject();
            if (ob == null) {
                return null;
            }
            if (this._valueClass.isAssignableFrom(ob.getClass())) {
                return (T)ob;
            }
            return this._deserializeEmbedded(ob, ctxt);
        }
    }
    
    protected abstract T _deserialize(final String p0, final DeserializationContext p1) throws IOException, JsonProcessingException;
    
    protected T _deserializeEmbedded(final Object ob, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        throw ctxt.mappingException("Don't know how to convert embedded Object of type " + ob.getClass().getName() + " into " + this._valueClass.getName());
    }
    
    public static class UUIDDeserializer extends FromStringDeserializer<UUID>
    {
        public UUIDDeserializer() {
            super(UUID.class);
        }
        
        @Override
        protected UUID _deserialize(final String value, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return UUID.fromString(value);
        }
        
        @Override
        protected UUID _deserializeEmbedded(final Object ob, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (ob instanceof byte[]) {
                final byte[] bytes = (byte[])ob;
                if (bytes.length != 16) {
                    ctxt.mappingException("Can only construct UUIDs from 16 byte arrays; got " + bytes.length + " bytes");
                }
                final DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
                final long l1 = in.readLong();
                final long l2 = in.readLong();
                return new UUID(l1, l2);
            }
            super._deserializeEmbedded(ob, ctxt);
            return null;
        }
    }
    
    public static class URLDeserializer extends FromStringDeserializer<URL>
    {
        public URLDeserializer() {
            super(URL.class);
        }
        
        @Override
        protected URL _deserialize(final String value, final DeserializationContext ctxt) throws IOException {
            return new URL(value);
        }
    }
    
    public static class URIDeserializer extends FromStringDeserializer<URI>
    {
        public URIDeserializer() {
            super(URI.class);
        }
        
        @Override
        protected URI _deserialize(final String value, final DeserializationContext ctxt) throws IllegalArgumentException {
            return URI.create(value);
        }
    }
    
    public static class CurrencyDeserializer extends FromStringDeserializer<Currency>
    {
        public CurrencyDeserializer() {
            super(Currency.class);
        }
        
        @Override
        protected Currency _deserialize(final String value, final DeserializationContext ctxt) throws IllegalArgumentException {
            return Currency.getInstance(value);
        }
    }
    
    public static class PatternDeserializer extends FromStringDeserializer<Pattern>
    {
        public PatternDeserializer() {
            super(Pattern.class);
        }
        
        @Override
        protected Pattern _deserialize(final String value, final DeserializationContext ctxt) throws IllegalArgumentException {
            return Pattern.compile(value);
        }
    }
    
    protected static class LocaleDeserializer extends FromStringDeserializer<Locale>
    {
        public LocaleDeserializer() {
            super(Locale.class);
        }
        
        @Override
        protected Locale _deserialize(String value, final DeserializationContext ctxt) throws IOException {
            int ix = value.indexOf(95);
            if (ix < 0) {
                return new Locale(value);
            }
            final String first = value.substring(0, ix);
            value = value.substring(ix + 1);
            ix = value.indexOf(95);
            if (ix < 0) {
                return new Locale(first, value);
            }
            final String second = value.substring(0, ix);
            return new Locale(first, second, value.substring(ix + 1));
        }
    }
    
    protected static class InetAddressDeserializer extends FromStringDeserializer<InetAddress>
    {
        public InetAddressDeserializer() {
            super(InetAddress.class);
        }
        
        @Override
        protected InetAddress _deserialize(final String value, final DeserializationContext ctxt) throws IOException {
            return InetAddress.getByName(value);
        }
    }
    
    protected static class CharsetDeserializer extends FromStringDeserializer<Charset>
    {
        public CharsetDeserializer() {
            super(Charset.class);
        }
        
        @Override
        protected Charset _deserialize(final String value, final DeserializationContext ctxt) throws IOException {
            return Charset.forName(value);
        }
    }
    
    protected static class TimeZoneDeserializer extends FromStringDeserializer<TimeZone>
    {
        public TimeZoneDeserializer() {
            super(TimeZone.class);
        }
        
        @Override
        protected TimeZone _deserialize(final String value, final DeserializationContext ctxt) throws IOException {
            return TimeZone.getTimeZone(value);
        }
    }
}
