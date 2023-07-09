// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.ozymandias.error;

import org.slf4j.Logger;

public final class ThrowableLogger
{
    public static void logThrowable(final String message, final Throwable t, final Logger log) {
        log.warn(message);
        if (log.isDebugEnabled()) {
            log.debug(message, t);
        }
    }
    
    public static String getClassName(final Object o) {
        if (o == null) {
            return "NULL";
        }
        if (o instanceof Class) {
            return ((Class)o).getName();
        }
        return o.getClass().getName();
    }
    
    private ThrowableLogger() {
    }
}
