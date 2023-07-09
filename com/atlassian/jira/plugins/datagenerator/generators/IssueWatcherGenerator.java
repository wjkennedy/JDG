// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators;

import org.slf4j.LoggerFactory;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.Locale;
import java.sql.SQLException;
import org.ofbiz.core.entity.GenericEntityException;
import com.atlassian.jira.issue.Issue;
import java.util.Iterator;
import com.atlassian.jira.user.ApplicationUser;
import java.util.List;
import java.util.Collection;
import com.atlassian.jira.plugins.datagenerator.util.Randomizer;
import com.atlassian.jira.plugins.datagenerator.util.PhasedTimer;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.plugins.datagenerator.GeneratorConfigurationUtil;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.plugins.datagenerator.UserUtils;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.issue.IssueManager;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class IssueWatcherGenerator implements DataGenerator
{
    private static final Logger LOG;
    private IssueManager issueManager;
    private ProjectManager projectManager;
    private UserUtils userUtils;
    private WatcherManager watcherManager;
    
    @Autowired
    public IssueWatcherGenerator(@ComponentImport final IssueManager issueManager, @ComponentImport final ProjectManager projectManager, final UserUtils userUtils, @ComponentImport final WatcherManager watcherManager) {
        this.issueManager = issueManager;
        this.projectManager = projectManager;
        this.userUtils = userUtils;
        this.watcherManager = watcherManager;
    }
    
    @Override
    public void generate(final GeneratorContext context) throws GenericEntityException, SQLException {
        int watcherCount = context.generatorConfiguration.issueWatchers;
        final List<ApplicationUser> users = this.userUtils.getApplicationUsers();
        final List<Project> allProjectsToGenerate = GeneratorConfigurationUtil.getProjects(context.generatorConfiguration, this.projectManager);
        int issuesCount = 0;
        for (final Project p : allProjectsToGenerate) {
            issuesCount += (int)this.issueManager.getIssueCountForProject(p.getId());
        }
        double meanWatchersPerIssue = watcherCount / (double)issuesCount;
        final PhasedTimer phasedTimer = new PhasedTimer();
        context.resetProgress(String.format("Adding %d watchers", watcherCount), watcherCount);
        phasedTimer.startPhase("adding watchers:");
        for (final Project p2 : allProjectsToGenerate) {
            final Collection<Long> issueIds = this.issueManager.getIssueIdsForProject(p2.getId());
            for (final Long issueId : issueIds) {
                if (watcherCount == 0) {
                    break;
                }
                final Issue issue = (Issue)this.issueManager.getIssueObject(issueId);
                int watchersForIssue;
                if (issuesCount == 1) {
                    watchersForIssue = watcherCount;
                }
                else {
                    watchersForIssue = Math.min(watcherCount, Randomizer.randomLimitedGaussian(meanWatchersPerIssue));
                    watcherCount -= watchersForIssue;
                    --issuesCount;
                    meanWatchersPerIssue = watcherCount / (double)issuesCount;
                }
                this.addWatchersToIssue(issue, users, watchersForIssue);
                context.progress.addAndGet(watchersForIssue);
            }
        }
        phasedTimer.stop();
        context.messages.addAll(phasedTimer.toStrings());
    }
    
    private void addWatchersToIssue(final Issue issue, final List<ApplicationUser> users, final int watchersForIssue) {
        final List<ApplicationUser> alreadyWatching = this.watcherManager.getWatchers(issue, Locale.ENGLISH);
        ApplicationUser user = null;
        final List<ApplicationUser> availableToWatch = users.stream().filter(user -> !alreadyWatching.contains(user)).collect((Collector<? super Object, ?, List<ApplicationUser>>)Collectors.toList());
        for (int i = 0; i < watchersForIssue; ++i) {
            user = Randomizer.randomItem(availableToWatch);
            this.watcherManager.startWatching(user, issue);
        }
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)IssueWatcherGenerator.class);
    }
}
