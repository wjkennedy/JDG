// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators.project;

import com.atlassian.jira.project.type.ProjectTypeKey;
import com.atlassian.jira.bc.project.ProjectCreationData;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.plugins.datagenerator.ProjectUpdater;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.bc.project.ProjectService;

public class EmptyProjectCreator implements ProjectCreator
{
    private final ProjectService projectService;
    private final ApplicationUser user;
    private final ProjectUpdater projectUpdater;
    
    public EmptyProjectCreator(final ProjectService projectService, final ApplicationUser user, final ProjectUpdater projectUpdater) {
        this.projectService = projectService;
        this.user = user;
        this.projectUpdater = projectUpdater;
    }
    
    @Override
    public Project createProject(final String projectName, final String key) throws ProjectCreationException {
        final ProjectCreationData.Builder projectCreateBuilder = new ProjectCreationData.Builder();
        projectCreateBuilder.withName(projectName).withKey(key).withDescription("Project for performance testing").withType(new ProjectTypeKey("business")).withLead(this.user).withAssigneeType(Long.valueOf(2L));
        final ProjectService.CreateProjectValidationResult createProjectValidationResult = this.projectService.validateCreateProject(this.user, projectCreateBuilder.build());
        if (createProjectValidationResult.isValid()) {
            final Project project = this.projectService.createProject(createProjectValidationResult);
            final ProjectService.UpdateProjectSchemesValidationResult schemesResult = this.projectService.validateUpdateProjectSchemes(this.user, Long.valueOf(0L), (Long)null, (Long)null);
            this.projectService.updateProjectSchemes(schemesResult, project);
            this.projectUpdater.updateCreatedProject(project);
            return project;
        }
        throw new ProjectCreationException(createProjectValidationResult.getErrorCollection());
    }
}
