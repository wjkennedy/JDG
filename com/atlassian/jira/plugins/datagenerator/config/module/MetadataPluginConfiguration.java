// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.config.module;

import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import java.util.Map;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;
import java.util.Collection;
import com.atlassian.annotations.PublicSpi;

@PublicSpi
public interface MetadataPluginConfiguration
{
    String getPluginKey();
    
    String getPluginName();
    
    Collection<ConfigurationField> getConfigurationFields();
    
    ErrorCollection validate(final GeneratorConfiguration p0);
    
    Map<String, String> prepareUsersForExecution();
    
    void execute(final GeneratorContext p0);
}
