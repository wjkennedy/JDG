// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.creators.project;

import com.google.common.collect.Maps;
import java.util.Map;
import com.atlassian.jira.security.roles.DefaultRoleActors;
import java.util.Iterator;
import com.atlassian.jira.security.roles.DefaultRoleActorsImpl;
import com.google.common.collect.Sets;
import java.util.Set;
import java.util.Collection;
import com.atlassian.jira.plugins.datagenerator.creators.retry.RetryFunction;
import com.atlassian.jira.security.roles.ProjectRoleActor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.security.roles.ProjectRole;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.security.roles.RoleActorFactory;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import org.springframework.stereotype.Component;

@Component
public class DefaultProjectRoleActorCreator
{
    private final ProjectRoleManager projectRoleManager;
    private final RoleActorFactory roleActorFactory;
    
    @Autowired
    public DefaultProjectRoleActorCreator(@ComponentImport final ProjectRoleManager projectRoleManager, @ComponentImport final RoleActorFactory roleActorFactory) {
        this.projectRoleManager = projectRoleManager;
        this.roleActorFactory = roleActorFactory;
    }
    
    public ProjectRoleActor generateDefaultProjectRoleActor(final ProjectRole projectRole, final ApplicationUser actor) {
        return new RetryFunction<ProjectRoleActor>().execute(() -> this.roleActorFactory.createRoleActor((Long)null, projectRole.getId(), (Long)null, "atlassian-user-role-actor", actor.getUsername()));
    }
    
    public Set<ProjectRoleActor> create(final ProjectRole projectRole, final Collection<ApplicationUser> actors) {
        return new RetryFunction<Set<ProjectRoleActor>>().execute(() -> {
            final Set<ProjectRoleActor> projectRoleActorsSet = Sets.newHashSetWithExpectedSize(actors.size());
            actors.iterator();
            final Iterator iterator;
            while (iterator.hasNext()) {
                final ApplicationUser user = iterator.next();
                final ProjectRoleActor roleActor = this.generateDefaultProjectRoleActor(projectRole, user);
                projectRoleActorsSet.add(roleActor);
            }
            final DefaultRoleActors defaultProjectRoleActors = (DefaultRoleActors)new DefaultRoleActorsImpl(projectRole.getId(), (Set)projectRoleActorsSet);
            this.projectRoleManager.updateDefaultRoleActors(defaultProjectRoleActors);
            return projectRoleActorsSet;
        });
    }
    
    public Map<ProjectRole, Set<ProjectRoleActor>> create(final Collection<ProjectRole> projectRoles, final Collection<ApplicationUser> actors) {
        final Map<ProjectRole, Set<ProjectRoleActor>> projectRoleActors = Maps.newHashMap();
        for (final ProjectRole projectRole : projectRoles) {
            projectRoleActors.put(projectRole, this.create(projectRole, actors));
        }
        return projectRoleActors;
    }
}
