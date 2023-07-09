// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.descriptor;

import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.export.ModuleType;
import org.springframework.stereotype.Component;
import com.atlassian.jira.plugins.datagenerator.config.module.DataPluginConfiguration;
import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;

@Component
@ModuleType
public class DataFactoryModuleDescriptor extends AbstractJiraModuleDescriptor<DataPluginConfiguration>
{
    @Autowired
    public DataFactoryModuleDescriptor(@ComponentImport final JiraAuthenticationContext authenticationContext, @ComponentImport final ModuleFactory moduleFactory) {
        super(authenticationContext, moduleFactory);
    }
}
