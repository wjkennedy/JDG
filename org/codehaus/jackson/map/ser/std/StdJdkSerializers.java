// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.std;

import org.codehaus.jackson.JsonNode;
import java.lang.reflect.Type;
import org.codehaus.jackson.JsonGenerationException;
import java.io.IOException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.JsonGenerator;
import java.io.File;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.UUID;
import java.util.Currency;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Collection;
import java.util.Map;
import org.codehaus.jackson.map.util.Provider;

public class StdJdkSerializers implements Provider<Map.Entry<Class<?>, Object>>
{
    public Collection<Map.Entry<Class<?>, Object>> provide() {
        final HashMap<Class<?>, Object> sers = new HashMap<Class<?>, Object>();
        final ToStringSerializer sls = ToStringSerializer.instance;
        sers.put(URL.class, sls);
        sers.put(URI.class, sls);
        sers.put(Currency.class, sls);
        sers.put(UUID.class, sls);
        sers.put(Pattern.class, sls);
        sers.put(Locale.class, sls);
        sers.put(Locale.class, sls);
        sers.put(AtomicReference.class, AtomicReferenceSerializer.class);
        sers.put(AtomicBoolean.class, AtomicBooleanSerializer.class);
        sers.put(AtomicInteger.class, AtomicIntegerSerializer.class);
        sers.put(AtomicLong.class, AtomicLongSerializer.class);
        sers.put(File.class, FileSerializer.class);
        sers.put(Class.class, ClassSerializer.class);
        sers.put(Void.TYPE, NullSerializer.class);
        return sers.entrySet();
    }
    
    public static final class AtomicBooleanSerializer extends ScalarSerializerBase<AtomicBoolean>
    {
        public AtomicBooleanSerializer() {
            super(AtomicBoolean.class, false);
        }
        
        @Override
        public void serialize(final AtomicBoolean value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeBoolean(value.get());
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            return this.createSchemaNode("boolean", true);
        }
    }
    
    public static final class AtomicIntegerSerializer extends ScalarSerializerBase<AtomicInteger>
    {
        public AtomicIntegerSerializer() {
            super(AtomicInteger.class, false);
        }
        
        @Override
        public void serialize(final AtomicInteger value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeNumber(value.get());
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            return this.createSchemaNode("integer", true);
        }
    }
    
    public static final class AtomicLongSerializer extends ScalarSerializerBase<AtomicLong>
    {
        public AtomicLongSerializer() {
            super(AtomicLong.class, false);
        }
        
        @Override
        public void serialize(final AtomicLong value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeNumber(value.get());
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            return this.createSchemaNode("integer", true);
        }
    }
    
    public static final class AtomicReferenceSerializer extends SerializerBase<AtomicReference<?>>
    {
        public AtomicReferenceSerializer() {
            super(AtomicReference.class, false);
        }
        
        @Override
        public void serialize(final AtomicReference<?> value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            provider.defaultSerializeValue(value.get(), jgen);
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            return this.createSchemaNode("any", true);
        }
    }
    
    public static final class FileSerializer extends ScalarSerializerBase<File>
    {
        public FileSerializer() {
            super(File.class);
        }
        
        @Override
        public void serialize(final File value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeString(value.getAbsolutePath());
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            return this.createSchemaNode("string", true);
        }
    }
    
    public static final class ClassSerializer extends ScalarSerializerBase<Class<?>>
    {
        public ClassSerializer() {
            super(Class.class, false);
        }
        
        @Override
        public void serialize(final Class<?> value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeString(value.getName());
        }
        
        @Override
        public JsonNode getSchema(final SerializerProvider provider, final Type typeHint) {
            return this.createSchemaNode("string", true);
        }
    }
}
