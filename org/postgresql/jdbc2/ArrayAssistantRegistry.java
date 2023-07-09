// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc2;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ArrayAssistantRegistry
{
    private static final ConcurrentMap<Integer, ArrayAssistant> ARRAY_ASSISTANT_MAP;
    
    public static ArrayAssistant getAssistant(final int oid) {
        return ArrayAssistantRegistry.ARRAY_ASSISTANT_MAP.get(oid);
    }
    
    public static void register(final int oid, final ArrayAssistant assistant) {
        ArrayAssistantRegistry.ARRAY_ASSISTANT_MAP.put(oid, assistant);
    }
    
    static {
        ARRAY_ASSISTANT_MAP = new ConcurrentHashMap<Integer, ArrayAssistant>();
    }
}
