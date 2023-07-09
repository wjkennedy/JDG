// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.plugins.access;

import com.google.common.collect.ImmutableList;
import com.google.common.base.Preconditions;
import com.atlassian.jira.plugins.datagenerator.config.module.ConfigurationField;
import java.util.Collection;

public class MetadataPluginConfigurationFormData
{
    private final String pluginKey;
    private final String pluginName;
    private final Collection<ConfigurationField> configurationFields;
    
    public MetadataPluginConfigurationFormData(final String pluginKey, final String pluginName, final Collection<ConfigurationField> configurationFields) {
        Preconditions.checkArgument(configurationFields != null, (Object)"configurationFields is required");
        this.pluginKey = pluginKey;
        this.pluginName = pluginName;
        this.configurationFields = (Collection<ConfigurationField>)ImmutableList.copyOf((Collection)configurationFields);
    }
    
    public Collection<ConfigurationField> getConfigurationFields() {
        return this.configurationFields;
    }
    
    public String getPluginKey() {
        return this.pluginKey;
    }
    
    public String getPluginName() {
        return this.pluginName;
    }
}
