// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.plugins.access;

import com.atlassian.jira.util.ErrorCollection;

public class MetadataPluginConfigurationValidationData
{
    private final String pluginKey;
    private final String pluginName;
    private final ErrorCollection errorCollection;
    
    public MetadataPluginConfigurationValidationData(final String pluginKey, final String pluginName, final ErrorCollection errorCollection) {
        this.errorCollection = errorCollection;
        this.pluginKey = pluginKey;
        this.pluginName = pluginName;
    }
    
    public ErrorCollection getErrorCollection() {
        return this.errorCollection;
    }
    
    public String getPluginKey() {
        return this.pluginKey;
    }
    
    public String getPluginName() {
        return this.pluginName;
    }
}
