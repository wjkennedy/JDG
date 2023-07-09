// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.plugins.access;

import java.util.Collection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.base.Preconditions;
import java.util.Set;
import java.util.List;
import java.util.Map;
import com.atlassian.jira.util.ErrorCollection;

class ImmutableErrorCollection implements ErrorCollection
{
    private final Map<String, String> errors;
    private final List<String> errorMessages;
    private final Set<ErrorCollection.Reason> reasons;
    
    public ImmutableErrorCollection(final ErrorCollection errorCollection) {
        Preconditions.checkArgument(errorCollection != null, (Object)"ErrorCollection is required to make it immutable");
        final Map<String, String> fromErrors = errorCollection.getErrors();
        final Collection<String> fromErrorMessages = errorCollection.getErrorMessages();
        final Set<ErrorCollection.Reason> fromReasons = errorCollection.getReasons();
        final ImmutableMap.Builder<String, String> errorsBuilder = (ImmutableMap.Builder<String, String>)ImmutableMap.builder();
        if (fromErrors != null) {
            errorsBuilder.putAll((Map)fromErrors);
        }
        final ImmutableList.Builder<String> errorMessagesBuilder = (ImmutableList.Builder<String>)ImmutableList.builder();
        if (fromErrorMessages != null) {
            errorMessagesBuilder.addAll((Iterable)fromErrorMessages);
        }
        final ImmutableSet.Builder<ErrorCollection.Reason> reasonsBuilder = (ImmutableSet.Builder<ErrorCollection.Reason>)ImmutableSet.builder();
        if (fromReasons != null) {
            reasonsBuilder.addAll((Iterable)fromReasons);
        }
        this.errors = (Map<String, String>)errorsBuilder.build();
        this.errorMessages = (List<String>)errorMessagesBuilder.build();
        this.reasons = (Set<ErrorCollection.Reason>)reasonsBuilder.build();
    }
    
    public void addError(final String field, final String message) {
        throw new UnsupportedOperationException("Not supported in Immutable Error Collection");
    }
    
    public void addError(final String field, final String message, final ErrorCollection.Reason reason) {
        throw new UnsupportedOperationException("Not supported in Immutable Error Collection");
    }
    
    public void addErrorMessage(final String message) {
        throw new UnsupportedOperationException("Not supported in Immutable Error Collection");
    }
    
    public void addErrorMessage(final String message, final ErrorCollection.Reason reason) {
        throw new UnsupportedOperationException("Not supported in Immutable Error Collection");
    }
    
    public Collection<String> getErrorMessages() {
        return this.errorMessages;
    }
    
    public void setErrorMessages(final Collection<String> errorMessages) {
        throw new UnsupportedOperationException("Not supported in Immutable Error Collection");
    }
    
    public Collection<String> getFlushedErrorMessages() {
        throw new UnsupportedOperationException("Not supported in Immutable Error Collection");
    }
    
    public Map<String, String> getErrors() {
        return this.errors;
    }
    
    public void addErrorCollection(final ErrorCollection errors) {
        throw new UnsupportedOperationException("Not supported in Immutable Error Collection");
    }
    
    public void addErrorMessages(final Collection<String> errorMessages) {
        throw new UnsupportedOperationException("Not supported in Immutable Error Collection");
    }
    
    public void addErrors(final Map<String, String> errors) {
        throw new UnsupportedOperationException("Not supported in Immutable Error Collection");
    }
    
    public boolean hasAnyErrors() {
        return !this.errors.isEmpty() || !this.errorMessages.isEmpty();
    }
    
    public void addReasons(final Set<ErrorCollection.Reason> reasons) {
        throw new UnsupportedOperationException("Not supported in Immutable Error Collection");
    }
    
    public void addReason(final ErrorCollection.Reason reason) {
        throw new UnsupportedOperationException("Not supported in Immutable Error Collection");
    }
    
    public void setReasons(final Set<ErrorCollection.Reason> reasons) {
        throw new UnsupportedOperationException("Not supported in Immutable Error Collection");
    }
    
    public Set<ErrorCollection.Reason> getReasons() {
        return this.reasons;
    }
}
