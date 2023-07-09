// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.ozymandias.error;

import org.slf4j.Logger;
import javax.annotation.Nonnull;

public final class ErrorUtils
{
    public static final String LINKAGE_ERROR_MESSAGE = "A LinkageError indicates that plugin code was compiled with outdated versions.";
    
    public static void handleThrowable(@Nonnull final Throwable throwable, @Nonnull final String errorMessage, @Nonnull final Logger log) {
        if (throwable instanceof Error) {
            handleError((Error)throwable, errorMessage, log);
        }
        else {
            ThrowableLogger.logThrowable(errorMessage, throwable, log);
        }
    }
    
    private static void handleError(final Error error, final String errorMessage, final Logger log) {
        if (error instanceof LinkageError) {
            ThrowableLogger.logThrowable("A LinkageError indicates that plugin code was compiled with outdated versions. " + errorMessage, error, log);
            return;
        }
        throw error;
    }
    
    private ErrorUtils() {
    }
}
