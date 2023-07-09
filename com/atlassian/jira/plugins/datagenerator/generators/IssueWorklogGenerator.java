// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators;

import java.util.Iterator;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.plugins.datagenerator.IssueInfo;
import com.atlassian.crowd.embedded.api.Group;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.function.Function;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.plugins.datagenerator.util.PhasedTimer;
import java.util.Collection;
import com.atlassian.jira.user.ApplicationUser;
import java.util.List;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.plugins.datagenerator.creators.issue.WorklogCreator;
import com.atlassian.jira.plugins.datagenerator.creators.issue.IssueCreator;
import com.atlassian.jira.plugins.datagenerator.UserUtils;
import com.atlassian.jira.plugins.datagenerator.creators.group.GroupCreator;
import com.atlassian.jira.plugins.datagenerator.creators.project.ProjectRoleCreator;
import com.atlassian.jira.plugins.datagenerator.plugins.access.DataPluginConfigurationAccessor;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.plugins.datagenerator.generators.factory.ProjectConfigurationsFactory;
import org.springframework.stereotype.Component;

@Component
public class IssueWorklogGenerator implements DataGenerator
{
    private final ProjectConfigurationsFactory projectConfigurationsFactory;
    private final IssueManager issueManager;
    private final IssueIndexManager issueIndexManager;
    private final DataPluginConfigurationAccessor dataPluginConfigurationAccessor;
    private final ProjectRoleCreator projectRoleCreator;
    private final GroupCreator groupCreator;
    private final UserUtils userUtils;
    private final IssueCreator issueCreator;
    private final WorklogCreator worklogCreator;
    
    @Autowired
    public IssueWorklogGenerator(final ProjectConfigurationsFactory projectConfigurationsFactory, @ComponentImport final IssueManager issueManager, @ComponentImport final IssueIndexManager issueIndexManager, final DataPluginConfigurationAccessor dataPluginConfigurationAccessor, final ProjectRoleCreator projectRoleCreator, final GroupCreator groupCreator, final UserUtils userUtils, final IssueCreator issueCreator, final WorklogCreator worklogCreator) {
        this.projectConfigurationsFactory = projectConfigurationsFactory;
        this.issueManager = issueManager;
        this.issueIndexManager = issueIndexManager;
        this.dataPluginConfigurationAccessor = dataPluginConfigurationAccessor;
        this.projectRoleCreator = projectRoleCreator;
        this.groupCreator = groupCreator;
        this.userUtils = userUtils;
        this.issueCreator = issueCreator;
        this.worklogCreator = worklogCreator;
    }
    
    private List<ProjectRole> generateProjectRoles(final GeneratorContext context, final GeneratorConfiguration configuration, final List<ApplicationUser> users, final Collection<ProjectConfigurationsFactory.ProjectConfiguration> projectConfigurations, final PhasedTimer phasedTimer) {
        final List<Project> projects = projectConfigurations.stream().map((Function<? super ProjectConfigurationsFactory.ProjectConfiguration, ?>)ProjectConfigurationsFactory.ProjectConfiguration::getProject).collect((Collector<? super Object, ?, List<Project>>)Collectors.toList());
        final String generatingProjectRolesMessage = String.format("generating %d project roles and assigning all users:", configuration.uniqueProjectRoles);
        return this.generate(generatingProjectRolesMessage, configuration.uniqueProjectRoles, context, phasedTimer, t -> this.projectRoleCreator.withDefaultActors(users).withActors(projects, users).onCreated(context.progress::addAndGet).create(configuration.uniqueProjectRoles));
    }
    
    private List<Group> generateGroups(final GeneratorContext context, final GeneratorConfiguration configuration, final List<ApplicationUser> users, final PhasedTimer phasedTimer) {
        final String generatingGroupsMessage = String.format("generating %d groups and assigning all users:", configuration.uniqueGroups);
        return this.generate(generatingGroupsMessage, configuration.uniqueGroups, context, phasedTimer, t -> this.groupCreator.onCreated(context.progress::addAndGet).createWithMembers(configuration.uniqueGroups, users));
    }
    
    private List<IssueInfo> generateIssues(final GeneratorContext context, final Collection<ProjectConfigurationsFactory.ProjectConfiguration> projectConfigurations, final int issues, final PhasedTimer phasedTimer) {
        final int projectIssues = issues / projectConfigurations.size();
        final String generatingIssuesMessage = String.format("generating %d issues for each and every selected project", issues);
        return this.generate(generatingIssuesMessage, issues, context, phasedTimer, t -> this.issueCreator.onCreated((Function<Integer, ?>)context.progress::addAndGet).withComments().withCustomFields().create(projectIssues, projectConfigurations, context));
    }
    
    private void generateWorklogs(final GeneratorContext context, final GeneratorConfiguration configuration, final List<ApplicationUser> users, final List<ProjectRole> projectRoles, final List<Group> groups, final List<IssueInfo> generatedIssues, final PhasedTimer phasedTimer) {
        final String generatingWorklogsForGroupsMessage = String.format("generating %d work logs visible to unique group:", configuration.worklogsWithGroup);
        this.generate(generatingWorklogsForGroupsMessage, configuration.worklogsWithGroup, context, phasedTimer, t -> {
            this.worklogCreator.onCreated((Function<Integer, ?>)context.progress::addAndGet).createVisibleToGroup(configuration.worklogsWithGroup, generatedIssues, users, groups, configuration);
            return null;
        });
        final String generatingWorklogsForProjectRolesMessage = String.format("generating %d work logs visible to unique project role:", configuration.worklogsWithProjectRole);
        this.generate(generatingWorklogsForProjectRolesMessage, configuration.worklogsWithProjectRole, context, phasedTimer, t -> {
            this.worklogCreator.onCreated((Function<Integer, ?>)context.progress::addAndGet).createVisibleToProjectRole(configuration.worklogsWithProjectRole, generatedIssues, users, projectRoles, configuration);
            return null;
        });
        final int visibleToAllWorklogs = configuration.totalWorklogs - configuration.worklogsWithGroup - configuration.worklogsWithProjectRole;
        final String generatingWorklogsForEveryoneMessage = String.format("generating %d work logs visible to everyone:", visibleToAllWorklogs);
        this.generate(generatingWorklogsForEveryoneMessage, visibleToAllWorklogs, context, phasedTimer, t -> {
            this.worklogCreator.onCreated((Function<Integer, ?>)context.progress::addAndGet).createVisibleToEveryone(visibleToAllWorklogs, generatedIssues, users, configuration);
            return null;
        });
    }
    
    public <T> T generate(final String message, final int counter, final GeneratorContext context, final PhasedTimer phasedTimer, final Function<Void, T> function) {
        context.resetProgress(message, counter);
        phasedTimer.startPhase(message);
        return function.apply(null);
    }
    
    @Override
    public void generate(final GeneratorContext context) {
        final PhasedTimer phasedTimer = new PhasedTimer();
        final GeneratorConfiguration configuration = context.generatorConfiguration;
        final List<ApplicationUser> users = this.userUtils.getApplicationUsers();
        final Collection<ProjectConfigurationsFactory.ProjectConfiguration> projectConfigurations = this.projectConfigurationsFactory.build(configuration);
        final int issues = configuration.totalWorklogs / configuration.worklogsPerIssue;
        final List<ProjectRole> projectRoles = this.generateProjectRoles(context, configuration, users, projectConfigurations, phasedTimer);
        final List<Group> groups = this.generateGroups(context, configuration, users, phasedTimer);
        final List<IssueInfo> generatedIssues = this.generateIssues(context, projectConfigurations, issues, phasedTimer);
        this.generateWorklogs(context, configuration, users, projectRoles, groups, generatedIssues, phasedTimer);
        this.reindexIssues(generatedIssues, context, projectConfigurations, phasedTimer);
        context.messages.addAll(phasedTimer.toStrings());
    }
    
    private void reindexIssues(final Collection<IssueInfo> issues, final GeneratorContext context, final Collection<ProjectConfigurationsFactory.ProjectConfiguration> projectConfigurations, final PhasedTimer phasedTimer) {
        final String reindexingIssuesMessage = String.format("reindexing %d issues:", issues.size());
        context.resetProgress(reindexingIssuesMessage, 1);
        phasedTimer.startPhase(reindexingIssuesMessage);
        final List<Long> ids = issues.stream().map((Function<? super IssueInfo, ?>)IssueInfo::getId).collect((Collector<? super Object, ?, List<Long>>)Collectors.toList());
        final List<GenericValue> issueObjects = this.issueManager.getIssues((Collection)ids);
        try {
            this.issueIndexManager.reIndexIssues((Collection)issueObjects);
        }
        catch (final IndexException e) {
            throw new RuntimeException((Throwable)e);
        }
        context.incProgress();
        context.resetProgress(String.format("Running post reindex actions for %d projects", projectConfigurations.size()), projectConfigurations.size());
        for (final ProjectConfigurationsFactory.ProjectConfiguration projectConfiguration : projectConfigurations) {
            for (final String pluginKey : projectConfiguration.getPluginKeys()) {
                this.dataPluginConfigurationAccessor.performPostReindexUpdates(pluginKey, projectConfiguration.getProject());
            }
            context.incProgress();
        }
    }
}
