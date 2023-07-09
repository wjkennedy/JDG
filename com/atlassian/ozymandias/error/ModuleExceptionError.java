// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.ozymandias.error;

public class ModuleExceptionError extends ModuleAccessError
{
    private final Throwable exception;
    
    public ModuleExceptionError(final Throwable exception) {
        super("An error occurred accessing the a plugin module");
        this.exception = exception;
    }
    
    public Throwable getException() {
        return this.exception;
    }
}
