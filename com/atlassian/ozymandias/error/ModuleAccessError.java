// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.ozymandias.error;

public class ModuleAccessError
{
    private final String errorMessage;
    
    public ModuleAccessError(final String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getErrorMessage() {
        return this.errorMessage;
    }
}
