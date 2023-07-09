// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.impl;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ResolvableSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.map.JsonSerializer;
import java.util.HashMap;

public final class SerializerCache
{
    private HashMap<TypeKey, JsonSerializer<Object>> _sharedMap;
    private ReadOnlyClassToSerializerMap _readOnlyMap;
    
    public SerializerCache() {
        this._sharedMap = new HashMap<TypeKey, JsonSerializer<Object>>(64);
        this._readOnlyMap = null;
    }
    
    public ReadOnlyClassToSerializerMap getReadOnlyLookupMap() {
        ReadOnlyClassToSerializerMap m;
        synchronized (this) {
            m = this._readOnlyMap;
            if (m == null) {
                m = (this._readOnlyMap = ReadOnlyClassToSerializerMap.from(this._sharedMap));
            }
        }
        return m.instance();
    }
    
    public synchronized int size() {
        return this._sharedMap.size();
    }
    
    public JsonSerializer<Object> untypedValueSerializer(final Class<?> type) {
        synchronized (this) {
            return this._sharedMap.get(new TypeKey(type, false));
        }
    }
    
    public JsonSerializer<Object> untypedValueSerializer(final JavaType type) {
        synchronized (this) {
            return this._sharedMap.get(new TypeKey(type, false));
        }
    }
    
    public JsonSerializer<Object> typedValueSerializer(final JavaType type) {
        synchronized (this) {
            return this._sharedMap.get(new TypeKey(type, true));
        }
    }
    
    public JsonSerializer<Object> typedValueSerializer(final Class<?> cls) {
        synchronized (this) {
            return this._sharedMap.get(new TypeKey(cls, true));
        }
    }
    
    public void addTypedSerializer(final JavaType type, final JsonSerializer<Object> ser) {
        synchronized (this) {
            if (this._sharedMap.put(new TypeKey(type, true), ser) == null) {
                this._readOnlyMap = null;
            }
        }
    }
    
    public void addTypedSerializer(final Class<?> cls, final JsonSerializer<Object> ser) {
        synchronized (this) {
            if (this._sharedMap.put(new TypeKey(cls, true), ser) == null) {
                this._readOnlyMap = null;
            }
        }
    }
    
    public void addAndResolveNonTypedSerializer(final Class<?> type, final JsonSerializer<Object> ser, final SerializerProvider provider) throws JsonMappingException {
        synchronized (this) {
            if (this._sharedMap.put(new TypeKey(type, false), ser) == null) {
                this._readOnlyMap = null;
            }
            if (ser instanceof ResolvableSerializer) {
                ((ResolvableSerializer)ser).resolve(provider);
            }
        }
    }
    
    public void addAndResolveNonTypedSerializer(final JavaType type, final JsonSerializer<Object> ser, final SerializerProvider provider) throws JsonMappingException {
        synchronized (this) {
            if (this._sharedMap.put(new TypeKey(type, false), ser) == null) {
                this._readOnlyMap = null;
            }
            if (ser instanceof ResolvableSerializer) {
                ((ResolvableSerializer)ser).resolve(provider);
            }
        }
    }
    
    public synchronized void flush() {
        this._sharedMap.clear();
    }
    
    public static final class TypeKey
    {
        protected int _hashCode;
        protected Class<?> _class;
        protected JavaType _type;
        protected boolean _isTyped;
        
        public TypeKey(final Class<?> key, final boolean typed) {
            this._class = key;
            this._type = null;
            this._isTyped = typed;
            this._hashCode = hash(key, typed);
        }
        
        public TypeKey(final JavaType key, final boolean typed) {
            this._type = key;
            this._class = null;
            this._isTyped = typed;
            this._hashCode = hash(key, typed);
        }
        
        private static final int hash(final Class<?> cls, final boolean typed) {
            int hash = cls.getName().hashCode();
            if (typed) {
                ++hash;
            }
            return hash;
        }
        
        private static final int hash(final JavaType type, final boolean typed) {
            int hash = type.hashCode() - 1;
            if (typed) {
                --hash;
            }
            return hash;
        }
        
        public void resetTyped(final Class<?> cls) {
            this._type = null;
            this._class = cls;
            this._isTyped = true;
            this._hashCode = hash(cls, true);
        }
        
        public void resetUntyped(final Class<?> cls) {
            this._type = null;
            this._class = cls;
            this._isTyped = false;
            this._hashCode = hash(cls, false);
        }
        
        public void resetTyped(final JavaType type) {
            this._type = type;
            this._class = null;
            this._isTyped = true;
            this._hashCode = hash(type, true);
        }
        
        public void resetUntyped(final JavaType type) {
            this._type = type;
            this._class = null;
            this._isTyped = false;
            this._hashCode = hash(type, false);
        }
        
        @Override
        public final int hashCode() {
            return this._hashCode;
        }
        
        @Override
        public final String toString() {
            if (this._class != null) {
                return "{class: " + this._class.getName() + ", typed? " + this._isTyped + "}";
            }
            return "{type: " + this._type + ", typed? " + this._isTyped + "}";
        }
        
        @Override
        public final boolean equals(final Object o) {
            if (o == this) {
                return true;
            }
            final TypeKey other = (TypeKey)o;
            if (other._isTyped != this._isTyped) {
                return false;
            }
            if (this._class != null) {
                return other._class == this._class;
            }
            return this._type.equals(other._type);
        }
    }
}
