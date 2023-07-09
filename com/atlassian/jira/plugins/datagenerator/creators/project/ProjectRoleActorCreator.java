// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.creators.project;

import java.util.List;
import com.google.common.collect.Maps;
import java.util.Map;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import java.util.Iterator;
import com.atlassian.jira.security.roles.ProjectRoleActorsImpl;
import com.google.common.collect.Sets;
import java.util.Set;
import java.util.Collection;
import com.atlassian.jira.plugins.datagenerator.creators.retry.RetryFunction;
import com.atlassian.jira.security.roles.ProjectRoleActor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.jira.security.roles.RoleActorFactory;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import org.springframework.stereotype.Component;

@Component
public class ProjectRoleActorCreator
{
    private final ProjectRoleManager projectRoleManager;
    private final RoleActorFactory roleActorFactory;
    
    @Autowired
    public ProjectRoleActorCreator(final ProjectRoleManager projectRoleManager, final RoleActorFactory roleActorFactory) {
        this.projectRoleManager = projectRoleManager;
        this.roleActorFactory = roleActorFactory;
    }
    
    public ProjectRoleActor generateProjectRoleActor(final ProjectRole projectRole, final Project project, final ApplicationUser actor) {
        return new RetryFunction<ProjectRoleActor>().execute(() -> this.roleActorFactory.createRoleActor((Long)null, projectRole.getId(), project.getId(), "atlassian-user-role-actor", actor.getUsername()));
    }
    
    public Set<ProjectRoleActor> create(final ProjectRole projectRole, final Collection<ApplicationUser> actors, final Project project) {
        return new RetryFunction<Set<ProjectRoleActor>>().execute(() -> {
            final Set<ProjectRoleActor> projectRoleActorsSet = Sets.newHashSetWithExpectedSize(actors.size());
            actors.iterator();
            final Iterator iterator;
            while (iterator.hasNext()) {
                final ApplicationUser user = iterator.next();
                final ProjectRoleActor roleActor = this.generateProjectRoleActor(projectRole, project, user);
                projectRoleActorsSet.add(roleActor);
            }
            final ProjectRoleActors projectRoleActors = (ProjectRoleActors)new ProjectRoleActorsImpl(project.getId(), projectRole.getId(), (Set)projectRoleActorsSet);
            this.projectRoleManager.updateProjectRoleActors(projectRoleActors);
            return projectRoleActorsSet;
        });
    }
    
    public Map<Project, Set<ProjectRoleActor>> create(final ProjectRole projectRole, final Collection<ApplicationUser> actors, final Collection<Project> projects) {
        final Map<Project, Set<ProjectRoleActor>> projectRoleActors = Maps.newHashMap();
        for (final Project project : projects) {
            projectRoleActors.put(project, this.create(projectRole, actors, project));
        }
        return projectRoleActors;
    }
    
    public Map<ProjectRole, Set<ProjectRoleActor>> create(final Collection<ProjectRole> projectRoles, final Collection<ApplicationUser> actors, final Project project) {
        final Map<ProjectRole, Set<ProjectRoleActor>> projectRoleActors = Maps.newHashMap();
        for (final ProjectRole projectRole : projectRoles) {
            projectRoleActors.put(projectRole, this.create(projectRole, actors, project));
        }
        return projectRoleActors;
    }
    
    public Map<Project, Map<ProjectRole, Set<ProjectRoleActor>>> create(final Collection<ProjectRole> projectRoles, final Collection<ApplicationUser> actors, final List<Project> projects) {
        final Map<Project, Map<ProjectRole, Set<ProjectRoleActor>>> projectsProjectRoleActors = Maps.newHashMap();
        for (final Project project : projects) {
            projectsProjectRoleActors.put(project, this.create(projectRoles, actors, project));
        }
        return projectsProjectRoleActors;
    }
}
