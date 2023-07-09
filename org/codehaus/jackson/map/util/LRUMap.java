// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.map.util;

import java.util.Map;
import java.util.LinkedHashMap;

public class LRUMap<K, V> extends LinkedHashMap<K, V>
{
    protected final int _maxEntries;
    
    public LRUMap(final int initialEntries, final int maxEntries) {
        super(initialEntries, 0.8f, true);
        this._maxEntries = maxEntries;
    }
    
    @Override
    protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
        return this.size() > this._maxEntries;
    }
}
