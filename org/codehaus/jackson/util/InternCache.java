// 
// Decompiled by Procyon v0.6.0
// 

package org.codehaus.jackson.util;

import java.util.Map;
import java.util.LinkedHashMap;

public final class InternCache extends LinkedHashMap<String, String>
{
    private static final int MAX_ENTRIES = 192;
    public static final InternCache instance;
    
    private InternCache() {
        super(192, 0.8f, true);
    }
    
    @Override
    protected boolean removeEldestEntry(final Map.Entry<String, String> eldest) {
        return this.size() > 192;
    }
    
    public synchronized String intern(final String input) {
        String result = ((LinkedHashMap<K, String>)this).get(input);
        if (result == null) {
            result = input.intern();
            this.put(result, result);
        }
        return result;
    }
    
    static {
        instance = new InternCache();
    }
}
