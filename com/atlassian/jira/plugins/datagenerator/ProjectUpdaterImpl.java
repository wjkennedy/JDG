// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator;

import com.atlassian.jira.project.Project;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.project.ProjectManager;
import org.springframework.stereotype.Component;

@Component
public class ProjectUpdaterImpl implements ProjectUpdater
{
    private final ProjectManager projectManager;
    
    @Autowired
    public ProjectUpdaterImpl(@ComponentImport final ProjectManager projectManager) {
        this.projectManager = projectManager;
    }
    
    @Override
    public void updateProjectCounters(final Project project, final long sequenceNumber) {
        this.projectManager.setCurrentCounterForProject(project, sequenceNumber);
    }
    
    @Override
    public void updateCreatedProject(final Project project) {
    }
}
