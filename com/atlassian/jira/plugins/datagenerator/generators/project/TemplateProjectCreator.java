// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators.project;

import com.atlassian.jira.projecttemplates.model.ProjectCreationResult;
import com.atlassian.jira.util.ValidationFailureException;
import java.util.Map;
import com.atlassian.jira.projecttemplates.model.ApplyTemplateParam;
import com.google.common.collect.Maps;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.projecttemplates.service.ProjectTemplateService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.bc.project.ProjectService;

public class TemplateProjectCreator implements ProjectCreator
{
    private final ProjectService projectService;
    private final String projectTemplate;
    private final ApplicationUser user;
    private final ProjectTemplateService projectTemplateService;
    
    public TemplateProjectCreator(final ProjectService projectService, final String projectTemplate, final ApplicationUser user) {
        this.projectService = projectService;
        this.projectTemplate = projectTemplate;
        this.user = user;
        this.projectTemplateService = (ProjectTemplateService)ComponentAccessor.getOSGiComponentInstanceOfType((Class)ProjectTemplateService.class);
    }
    
    @Override
    public Project createProject(final String name, final String key) throws ProjectCreationException {
        final Map<String, String[]> requestParams = Maps.newHashMap();
        final ApplyTemplateParam applyTemplateParam = ApplyTemplateParam.create().setCurrentUser(this.user).setProjectName(name).setProjectKey(key.toUpperCase()).setProjectDescription("Generated project description").setLeadName(this.user.getName()).setProjectTemplateWebItemKey(this.projectTemplate).setTemplateConfigurationParams((Map)requestParams);
        try {
            final ProjectCreationResult projectCreationResult = this.projectTemplateService.applyProjectTemplate(applyTemplateParam);
            return this.projectService.getProjectById(this.user, Long.valueOf(projectCreationResult.getProjectId())).getProject();
        }
        catch (final ValidationFailureException e) {
            throw new ProjectCreationException(e.errors());
        }
    }
}
