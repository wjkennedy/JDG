// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.drivers;

import java.sql.SQLException;
import org.ofbiz.core.entity.GenericEntityException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import org.springframework.stereotype.Component;

@Component
public class CreateComponentsDriver
{
    private final ProjectComponentManager componentManager;
    
    @Autowired
    public CreateComponentsDriver(@ComponentImport final ProjectComponentManager componentManager) {
        this.componentManager = componentManager;
    }
    
    public void generate(final GeneratorContext context, final Project project, final int versionsCount) throws GenericEntityException, SQLException {
        for (int version = 1; version <= versionsCount; ++version) {
            try {
                final String componentName = String.format("Component %d", version);
                this.componentManager.create(componentName, (String)null, (String)null, 0L, project.getId());
                context.incProgress();
            }
            catch (final IllegalArgumentException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
