// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators;

import java.util.Map;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import com.atlassian.jira.issue.status.Status;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.config.ConstantsManager;
import org.springframework.stereotype.Component;

@Component
public class StatusConstantGeneratorHelper implements ConstantGeneratorHelper
{
    private final ConstantsManager constantsManager;
    
    @Autowired
    public StatusConstantGeneratorHelper(@ComponentImport final ConstantsManager constantsManager) {
        this.constantsManager = constantsManager;
    }
    
    @Override
    public Collection<Status> getConstants() {
        return this.constantsManager.getStatuses();
    }
    
    @Override
    public int constantCount(final GeneratorContext context) {
        return context.generatorConfiguration.additionalStatusesCount;
    }
    
    @Override
    public void refresh() {
        this.constantsManager.refreshStatuses();
    }
    
    @Override
    public String getPluralName() {
        return "statuses";
    }
    
    @Override
    public String getConstantName() {
        return ConstantsManager.STATUS_CONSTANT_TYPE;
    }
    
    @Override
    public void fillAdditionalData(final Map<String, Object> statusFields) {
        statusFields.put("iconurl", "/images/icons/status_generic.gif");
    }
}
