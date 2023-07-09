// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.ser.impl;

import java.util.Iterator;
import org.codehaus.jackson.map.JsonSerializer;
import java.util.Map;

public class JsonSerializerMap
{
    private final Bucket[] _buckets;
    private final int _size;
    
    public JsonSerializerMap(final Map<SerializerCache.TypeKey, JsonSerializer<Object>> serializers) {
        final int size = findSize(serializers.size());
        this._size = size;
        final int hashMask = size - 1;
        final Bucket[] buckets = new Bucket[size];
        for (final Map.Entry<SerializerCache.TypeKey, JsonSerializer<Object>> entry : serializers.entrySet()) {
            final SerializerCache.TypeKey key = entry.getKey();
            final int index = key.hashCode() & hashMask;
            buckets[index] = new Bucket(buckets[index], key, entry.getValue());
        }
        this._buckets = buckets;
    }
    
    private static final int findSize(final int size) {
        int needed;
        int result;
        for (needed = ((size <= 64) ? (size + size) : (size + (size >> 2))), result = 8; result < needed; result += result) {}
        return result;
    }
    
    public int size() {
        return this._size;
    }
    
    public JsonSerializer<Object> find(final SerializerCache.TypeKey key) {
        final int index = key.hashCode() & this._buckets.length - 1;
        Bucket bucket = this._buckets[index];
        if (bucket == null) {
            return null;
        }
        if (key.equals(bucket.key)) {
            return bucket.value;
        }
        while ((bucket = bucket.next) != null) {
            if (key.equals(bucket.key)) {
                return bucket.value;
            }
        }
        return null;
    }
    
    private static final class Bucket
    {
        public final SerializerCache.TypeKey key;
        public final JsonSerializer<Object> value;
        public final Bucket next;
        
        public Bucket(final Bucket next, final SerializerCache.TypeKey key, final JsonSerializer<Object> value) {
            this.next = next;
            this.key = key;
            this.value = value;
        }
    }
}
