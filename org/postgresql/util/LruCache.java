// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.util.LinkedHashMap;
import java.util.Iterator;
import java.sql.SQLException;
import java.util.Map;

public class LruCache<Key, Value extends CanEstimateSize> implements Gettable<Key, Value>
{
    private final EvictAction<Value> onEvict;
    private final CreateAction<Key, Value> createAction;
    private final int maxSizeEntries;
    private final long maxSizeBytes;
    private long currentSize;
    private final Map<Key, Value> cache;
    
    private void evictValue(final Value value) {
        try {
            if (this.onEvict != null) {
                this.onEvict.evict(value);
            }
        }
        catch (final SQLException ex) {}
    }
    
    public LruCache(final int maxSizeEntries, final long maxSizeBytes, final boolean accessOrder) {
        this(maxSizeEntries, maxSizeBytes, accessOrder, null, null);
    }
    
    public LruCache(final int maxSizeEntries, final long maxSizeBytes, final boolean accessOrder, final CreateAction<Key, Value> createAction, final EvictAction<Value> onEvict) {
        this.maxSizeEntries = maxSizeEntries;
        this.maxSizeBytes = maxSizeBytes;
        this.createAction = createAction;
        this.onEvict = onEvict;
        this.cache = new LimitedMap(16, 0.75f, accessOrder);
    }
    
    @Override
    public synchronized Value get(final Key key) {
        return this.cache.get(key);
    }
    
    public synchronized Value borrow(final Key key) throws SQLException {
        final Value value = this.cache.remove(key);
        if (value != null) {
            this.currentSize -= value.getSize();
            return value;
        }
        if (this.createAction == null) {
            throw new UnsupportedOperationException("createAction == null, so can't create object");
        }
        return this.createAction.create(key);
    }
    
    public synchronized void put(final Key key, final Value value) {
        final long valueSize = value.getSize();
        if (this.maxSizeBytes == 0L || this.maxSizeEntries == 0 || valueSize * 2L > this.maxSizeBytes) {
            this.evictValue(value);
            return;
        }
        this.currentSize += valueSize;
        final Value prev = this.cache.put(key, value);
        if (prev == null) {
            return;
        }
        this.currentSize -= prev.getSize();
        if (prev != value) {
            this.evictValue(prev);
        }
    }
    
    public synchronized void putAll(final Map<Key, Value> m) {
        for (final Map.Entry<Key, Value> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }
    
    private class LimitedMap extends LinkedHashMap<Key, Value>
    {
        LimitedMap(final int initialCapacity, final float loadFactor, final boolean accessOrder) {
            super(initialCapacity, loadFactor, accessOrder);
        }
        
        @Override
        protected boolean removeEldestEntry(final Map.Entry<Key, Value> eldest) {
            if (this.size() <= LruCache.this.maxSizeEntries && LruCache.this.currentSize <= LruCache.this.maxSizeBytes) {
                return false;
            }
            final Iterator<Map.Entry<Key, Value>> it = this.entrySet().iterator();
            while (it.hasNext()) {
                if (this.size() <= LruCache.this.maxSizeEntries && LruCache.this.currentSize <= LruCache.this.maxSizeBytes) {
                    return false;
                }
                final Map.Entry<Key, Value> entry = it.next();
                LruCache.this.evictValue(entry.getValue());
                final long valueSize = entry.getValue().getSize();
                if (valueSize > 0L) {
                    LruCache.this.currentSize -= valueSize;
                }
                it.remove();
            }
            return false;
        }
    }
    
    public interface CreateAction<Key, Value>
    {
        Value create(final Key p0) throws SQLException;
    }
    
    public interface EvictAction<Value>
    {
        void evict(final Value p0) throws SQLException;
    }
}
