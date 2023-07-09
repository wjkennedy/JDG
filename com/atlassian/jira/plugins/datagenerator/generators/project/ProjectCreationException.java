// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators.project;

import com.atlassian.jira.util.ErrorCollection;

public class ProjectCreationException extends Exception
{
    private final ErrorCollection errorCollection;
    
    public ProjectCreationException(final ErrorCollection errorCollection) {
        this.errorCollection = errorCollection;
    }
    
    public ProjectCreationException(final String message, final ErrorCollection errorCollection) {
        super(message);
        this.errorCollection = errorCollection;
    }
    
    public ProjectCreationException(final String message, final Throwable cause, final ErrorCollection errorCollection) {
        super(message, cause);
        this.errorCollection = errorCollection;
    }
    
    public ErrorCollection getErrorCollection() {
        return this.errorCollection;
    }
}
