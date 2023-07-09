// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.impl;

import org.codehaus.jackson.type.JavaType;
import java.util.Map;
import org.codehaus.jackson.map.JsonSerializer;
import java.util.HashMap;

public final class ReadOnlyClassToSerializerMap
{
    protected final JsonSerializerMap _map;
    protected final SerializerCache.TypeKey _cacheKey;
    
    private ReadOnlyClassToSerializerMap(final JsonSerializerMap map) {
        this._cacheKey = new SerializerCache.TypeKey(this.getClass(), false);
        this._map = map;
    }
    
    public ReadOnlyClassToSerializerMap instance() {
        return new ReadOnlyClassToSerializerMap(this._map);
    }
    
    public static ReadOnlyClassToSerializerMap from(final HashMap<SerializerCache.TypeKey, JsonSerializer<Object>> src) {
        return new ReadOnlyClassToSerializerMap(new JsonSerializerMap(src));
    }
    
    public JsonSerializer<Object> typedValueSerializer(final JavaType type) {
        this._cacheKey.resetTyped(type);
        return this._map.find(this._cacheKey);
    }
    
    public JsonSerializer<Object> typedValueSerializer(final Class<?> cls) {
        this._cacheKey.resetTyped(cls);
        return this._map.find(this._cacheKey);
    }
    
    public JsonSerializer<Object> untypedValueSerializer(final Class<?> cls) {
        this._cacheKey.resetUntyped(cls);
        return this._map.find(this._cacheKey);
    }
    
    public JsonSerializer<Object> untypedValueSerializer(final JavaType type) {
        this._cacheKey.resetUntyped(type);
        return this._map.find(this._cacheKey);
    }
}
