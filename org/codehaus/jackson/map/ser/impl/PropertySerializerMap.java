// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.impl;

import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.JsonSerializer;

public abstract class PropertySerializerMap
{
    public abstract JsonSerializer<Object> serializerFor(final Class<?> p0);
    
    public final SerializerAndMapResult findAndAddSerializer(final Class<?> type, final SerializerProvider provider, final BeanProperty property) throws JsonMappingException {
        final JsonSerializer<Object> serializer = provider.findValueSerializer(type, property);
        return new SerializerAndMapResult(serializer, this.newWith(type, serializer));
    }
    
    public final SerializerAndMapResult findAndAddSerializer(final JavaType type, final SerializerProvider provider, final BeanProperty property) throws JsonMappingException {
        final JsonSerializer<Object> serializer = provider.findValueSerializer(type, property);
        return new SerializerAndMapResult(serializer, this.newWith(type.getRawClass(), serializer));
    }
    
    public abstract PropertySerializerMap newWith(final Class<?> p0, final JsonSerializer<Object> p1);
    
    public static PropertySerializerMap emptyMap() {
        return Empty.instance;
    }
    
    public static final class SerializerAndMapResult
    {
        public final JsonSerializer<Object> serializer;
        public final PropertySerializerMap map;
        
        public SerializerAndMapResult(final JsonSerializer<Object> serializer, final PropertySerializerMap map) {
            this.serializer = serializer;
            this.map = map;
        }
    }
    
    private static final class TypeAndSerializer
    {
        public final Class<?> type;
        public final JsonSerializer<Object> serializer;
        
        public TypeAndSerializer(final Class<?> type, final JsonSerializer<Object> serializer) {
            this.type = type;
            this.serializer = serializer;
        }
    }
    
    private static final class Empty extends PropertySerializerMap
    {
        protected static final Empty instance;
        
        @Override
        public JsonSerializer<Object> serializerFor(final Class<?> type) {
            return null;
        }
        
        @Override
        public PropertySerializerMap newWith(final Class<?> type, final JsonSerializer<Object> serializer) {
            return new Single(type, serializer);
        }
        
        static {
            instance = new Empty();
        }
    }
    
    private static final class Single extends PropertySerializerMap
    {
        private final Class<?> _type;
        private final JsonSerializer<Object> _serializer;
        
        public Single(final Class<?> type, final JsonSerializer<Object> serializer) {
            this._type = type;
            this._serializer = serializer;
        }
        
        @Override
        public JsonSerializer<Object> serializerFor(final Class<?> type) {
            if (type == this._type) {
                return this._serializer;
            }
            return null;
        }
        
        @Override
        public PropertySerializerMap newWith(final Class<?> type, final JsonSerializer<Object> serializer) {
            return new Double(this._type, this._serializer, type, serializer);
        }
    }
    
    private static final class Double extends PropertySerializerMap
    {
        private final Class<?> _type1;
        private final Class<?> _type2;
        private final JsonSerializer<Object> _serializer1;
        private final JsonSerializer<Object> _serializer2;
        
        public Double(final Class<?> type1, final JsonSerializer<Object> serializer1, final Class<?> type2, final JsonSerializer<Object> serializer2) {
            this._type1 = type1;
            this._serializer1 = serializer1;
            this._type2 = type2;
            this._serializer2 = serializer2;
        }
        
        @Override
        public JsonSerializer<Object> serializerFor(final Class<?> type) {
            if (type == this._type1) {
                return this._serializer1;
            }
            if (type == this._type2) {
                return this._serializer2;
            }
            return null;
        }
        
        @Override
        public PropertySerializerMap newWith(final Class<?> type, final JsonSerializer<Object> serializer) {
            final TypeAndSerializer[] ts = { new TypeAndSerializer(this._type1, this._serializer1), new TypeAndSerializer(this._type2, this._serializer2) };
            return new Multi(ts);
        }
    }
    
    private static final class Multi extends PropertySerializerMap
    {
        private static final int MAX_ENTRIES = 8;
        private final TypeAndSerializer[] _entries;
        
        public Multi(final TypeAndSerializer[] entries) {
            this._entries = entries;
        }
        
        @Override
        public JsonSerializer<Object> serializerFor(final Class<?> type) {
            for (int i = 0, len = this._entries.length; i < len; ++i) {
                final TypeAndSerializer entry = this._entries[i];
                if (entry.type == type) {
                    return entry.serializer;
                }
            }
            return null;
        }
        
        @Override
        public PropertySerializerMap newWith(final Class<?> type, final JsonSerializer<Object> serializer) {
            final int len = this._entries.length;
            if (len == 8) {
                return this;
            }
            final TypeAndSerializer[] entries = new TypeAndSerializer[len + 1];
            System.arraycopy(this._entries, 0, entries, 0, len);
            entries[len] = new TypeAndSerializer(type, serializer);
            return new Multi(entries);
        }
    }
}
