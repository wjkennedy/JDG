// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators.project;

import com.atlassian.jira.bc.project.ProjectCreationData;
import java.util.Random;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.project.Project;
import java.util.List;
import com.atlassian.jira.bc.project.ProjectService;

public class SharedConfigProjectCreator implements ProjectCreator
{
    private final ProjectService projectService;
    private final List<Project> existingProjects;
    private final ApplicationUser user;
    private final Random rand;
    
    public SharedConfigProjectCreator(final ProjectService projectService, final ApplicationUser user) {
        this.rand = new Random();
        this.projectService = projectService;
        this.user = user;
        this.existingProjects = (List)projectService.getAllProjects(user).get();
    }
    
    @Override
    public Project createProject(final String name, final String key) throws ProjectCreationException {
        System.out.println("Creating a project key=" + key);
        final ProjectCreationData data = new ProjectCreationData.Builder().withName(name).withKey(key).withLead(this.user).build();
        final long existingProjectId = this.existingProjects.get(this.rand.nextInt(this.existingProjects.size())).getId();
        final ProjectService.CreateProjectValidationResult validationResult = this.projectService.validateCreateProjectBasedOnExistingProject(this.user, Long.valueOf(existingProjectId), data);
        if (!validationResult.isValid()) {
            throw new ProjectCreationException(validationResult.getErrorCollection());
        }
        return this.projectService.createProject(validationResult);
    }
}
