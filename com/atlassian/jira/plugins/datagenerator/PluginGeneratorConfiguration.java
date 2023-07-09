// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator;

import com.google.common.collect.Maps;
import java.util.Map;

public class PluginGeneratorConfiguration
{
    public Map<String, Object> moduleConfiguration;
    
    public PluginGeneratorConfiguration() {
        this.moduleConfiguration = Maps.newHashMap();
    }
}
