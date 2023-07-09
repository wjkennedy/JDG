// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator;

import java.util.Map;
import com.atlassian.jira.issue.resolution.Resolution;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.config.ConstantsManager;
import org.springframework.stereotype.Component;
import com.atlassian.jira.plugins.datagenerator.generators.ConstantGeneratorHelper;

@Component
public class ResolutionConstantGeneratorHelper implements ConstantGeneratorHelper
{
    private final ConstantsManager constantsManager;
    
    @Autowired
    public ResolutionConstantGeneratorHelper(@ComponentImport final ConstantsManager constantsManager) {
        this.constantsManager = constantsManager;
    }
    
    @Override
    public Collection<Resolution> getConstants() {
        return this.constantsManager.getResolutions();
    }
    
    @Override
    public int constantCount(final GeneratorContext context) {
        return context.generatorConfiguration.additionalResolutionsCount;
    }
    
    @Override
    public void refresh() {
        this.constantsManager.refreshResolutions();
    }
    
    @Override
    public String getPluralName() {
        return "resolutions";
    }
    
    @Override
    public String getConstantName() {
        return ConstantsManager.RESOLUTION_CONSTANT_TYPE;
    }
    
    @Override
    public void fillAdditionalData(final Map<String, Object> statusFields) {
    }
}
