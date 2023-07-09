// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.creators.issue;

import org.apache.commons.lang3.tuple.ImmutablePair;
import java.util.Iterator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.function.Function;
import com.atlassian.jira.plugins.datagenerator.db.EntityManager;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;
import com.atlassian.jira.plugins.datagenerator.timestamp.FixedIntervalEndlessGenerator;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.tuple.Pair;
import com.atlassian.jira.plugins.datagenerator.util.Randomizer;
import java.util.HashMap;
import java.util.Map;
import com.google.common.collect.Maps;
import java.sql.Timestamp;
import com.atlassian.jira.plugins.datagenerator.creators.retry.RetryFunction;
import com.atlassian.jira.plugins.datagenerator.timestamp.TimestampGenerator;
import com.atlassian.jira.plugins.datagenerator.text.TextGenerator;
import com.atlassian.jira.plugins.datagenerator.db.EntityHandler;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.plugins.datagenerator.IssueInfo;
import java.util.Collection;
import java.util.ArrayList;
import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.LoggerFactory;
import java.util.Random;
import org.slf4j.Logger;
import com.atlassian.jira.plugins.datagenerator.db.postgres.JiraSequenceIdGenerator;
import com.atlassian.jira.plugins.datagenerator.creators.listeners.CreationEmitter;
import org.springframework.stereotype.Component;
import com.atlassian.jira.plugins.datagenerator.creators.CreatedEvent;

@Component
public class WorklogCreator implements CreatedEvent<WorklogCreator>
{
    private final CreationEmitter emitter;
    private final JiraSequenceIdGenerator sequenceIdGenerator;
    private final Logger LOG;
    private final Random random;
    
    @Autowired
    public WorklogCreator(final JiraSequenceIdGenerator sequenceIdGenerator) {
        this.emitter = new CreationEmitter();
        this.LOG = LoggerFactory.getLogger((Class)WorklogCreator.class);
        this.random = new Random();
        this.sequenceIdGenerator = sequenceIdGenerator;
    }
    
    private <T> List<T> list(final int desiredSize, final List<T> baseList) {
        if (baseList == null) {
            return null;
        }
        if (baseList.size() == desiredSize) {
            return baseList;
        }
        final List<T> desiredList = Lists.newArrayListWithExpectedSize(desiredSize);
        if (baseList.size() > desiredSize) {
            while (desiredList.size() < desiredSize) {
                final int id = this.random.nextInt(baseList.size() - 1);
                desiredList.add(baseList.get(id));
            }
        }
        else {
            while (desiredList.size() < desiredSize) {
                desiredList.addAll((Collection<? extends T>)new ArrayList<T>((Collection<? extends T>)baseList));
            }
        }
        return desiredList;
    }
    
    private void create(final IssueInfo issue, final ApplicationUser author, final Group group, final ProjectRole projectRole, final EntityHandler entityHandler, final TextGenerator textGenerator, final TimestampGenerator timestampGenerator) {
        new RetryFunction<Void>("create work logs batch").execute(() -> {
            final Timestamp timestamp = timestampGenerator.next();
            final HashMap worklog = Maps.newHashMap();
            worklog.put("id", entityHandler.getNextSequenceId());
            worklog.put("issue", issue.id);
            worklog.put("author", author.getUsername());
            worklog.put("updateauthor", author.getUsername());
            worklog.put("type", "worklog");
            worklog.put("body", textGenerator.generateText());
            worklog.put("created", timestamp);
            worklog.put("updated", timestamp);
            worklog.put("startdate", timestamp);
            worklog.put("timeworked", timestamp.getTime());
            if (group != null) {
                worklog.put("grouplevel", group.getName());
            }
            if (projectRole != null) {
                worklog.put("rolelevel", projectRole.getId());
            }
            entityHandler.store(worklog);
            return null;
        });
    }
    
    private void createWithGroup(final int worklogs, final IssueInfo issueInfo, final TimestampGenerator timestampGenerator, final List<ApplicationUser> authors, final List<Group> groups, final EntityHandler entityHandler, final TextGenerator textGenerator) {
        final List<Group> associatedGroups = this.list(worklogs, groups);
        for (int i = 0; i < worklogs; ++i) {
            final ApplicationUser author = Randomizer.randomItem(authors);
            final Group group = associatedGroups.get(i);
            this.create(issueInfo, author, group, null, entityHandler, textGenerator, timestampGenerator);
        }
    }
    
    private void createWithProjectRole(final int worklogs, final IssueInfo issueInfo, final TimestampGenerator timestampGenerator, final List<ApplicationUser> authors, final List<ProjectRole> projectRoles, final EntityHandler entityHandler, final TextGenerator textGenerator) {
        final List<ProjectRole> associatedProjectRoles = this.list(worklogs, projectRoles);
        for (int i = 0; i < worklogs; ++i) {
            final ApplicationUser author = Randomizer.randomItem(authors);
            final ProjectRole projectRole = associatedProjectRoles.get(i);
            this.create(issueInfo, author, null, projectRole, entityHandler, textGenerator, timestampGenerator);
        }
    }
    
    private void createPlain(final int worklogs, final IssueInfo issueInfo, final TimestampGenerator timestampGenerator, final List<ApplicationUser> authors, final EntityHandler entityHandler, final TextGenerator textGenerator) {
        for (int i = 0; i < worklogs; ++i) {
            final ApplicationUser author = Randomizer.randomItem(authors);
            this.create(issueInfo, author, null, null, entityHandler, textGenerator, timestampGenerator);
        }
    }
    
    private void create(final Pair<IssueInfo, Integer> issue, final List<ApplicationUser> authors, final List<Group> groups, final List<ProjectRole> projectRoles, final EntityHandler entityHandler, final TextGenerator textGenerator) {
        final IssueInfo issueInfo = (IssueInfo)issue.getLeft();
        final int worklogs = (int)issue.getRight();
        final TimestampGenerator timestampGenerator = new FixedIntervalEndlessGenerator(issueInfo.created, TimeUnit.HOURS.toMillis(8L));
        if (groups != null) {
            this.createWithGroup(worklogs, issueInfo, timestampGenerator, authors, groups, entityHandler, textGenerator);
        }
        else if (projectRoles != null) {
            this.createWithProjectRole(worklogs, issueInfo, timestampGenerator, authors, projectRoles, entityHandler, textGenerator);
        }
        else {
            this.createPlain(worklogs, issueInfo, timestampGenerator, authors, entityHandler, textGenerator);
        }
    }
    
    private void create(final List<Pair<IssueInfo, Integer>> issues, final List<ApplicationUser> authors, final List<Group> groups, final List<ProjectRole> projectRoles, final GeneratorConfiguration configuration) {
        new RetryFunction<Void>("create work logs").execute(() -> {
            EntityManager entityManager = null;
            EntityHandler entityHandler = null;
            try {
                entityManager = new EntityManager(this.sequenceIdGenerator, configuration);
                entityHandler = entityManager.getEntityHandler("Worklog");
                final List<String> authorsUsernames = (List<String>)authors.stream().map(ApplicationUser::getUsername).collect(Collectors.toList());
                final TextGenerator textGenerator = TextGenerator.getTextGenerator(configuration, authorsUsernames);
                issues.iterator();
                final Iterator iterator;
                while (iterator.hasNext()) {
                    final Pair<IssueInfo, Integer> issue = (Pair<IssueInfo, Integer>)iterator.next();
                    this.create(issue, authors, groups, projectRoles, entityHandler, textGenerator);
                }
                return;
            }
            finally {
                if (entityHandler != null) {
                    try {
                        entityHandler.close();
                    }
                    catch (final Exception ignored) {
                        this.LOG.warn("Could not close enitity handler", (Throwable)ignored);
                    }
                }
                if (entityManager != null) {
                    try {
                        entityManager.shutdown();
                    }
                    catch (final Exception ignored2) {
                        this.LOG.warn("Could not shut down enitity manager", (Throwable)ignored2);
                    }
                }
            }
        });
    }
    
    private List<List<Pair<IssueInfo, Integer>>> batches(final int worklogs, final List<IssueInfo> issues, final int batchSize) {
        final int worklogsPerIssue = worklogs / issues.size();
        final List<Pair<IssueInfo, Integer>> worklogsIssues = Lists.newArrayListWithExpectedSize(issues.size());
        for (final IssueInfo issue : issues) {
            final Pair<IssueInfo, Integer> worklogsIssue = (Pair<IssueInfo, Integer>)new ImmutablePair((Object)issue, (Object)worklogsPerIssue);
            worklogsIssues.add(worklogsIssue);
        }
        return Lists.partition((List)worklogsIssues, batchSize);
    }
    
    private void createWithVisibility(final int worklogs, final List<IssueInfo> issues, final List<ApplicationUser> authors, final List<Group> groups, final List<ProjectRole> projectRoles, final GeneratorConfiguration configuration) {
        final int batchSize = 1000;
        final List<List<Pair<IssueInfo, Integer>>> batches = this.batches(worklogs, issues, 1000);
        for (final List<Pair<IssueInfo, Integer>> batch : batches) {
            this.create(batch, authors, groups, projectRoles, configuration);
            this.emitter.emit(1000);
        }
    }
    
    public void createVisibleToEveryone(final int worklogs, final List<IssueInfo> issues, final List<ApplicationUser> authors, final GeneratorConfiguration configuration) {
        this.createWithVisibility(worklogs, issues, authors, null, null, configuration);
    }
    
    public void createVisibleToGroup(final int worklogs, final List<IssueInfo> issues, final List<ApplicationUser> authors, final List<Group> groups, final GeneratorConfiguration configuration) {
        this.createWithVisibility(worklogs, issues, authors, groups, null, configuration);
    }
    
    public void createVisibleToProjectRole(final int worklogs, final List<IssueInfo> issues, final List<ApplicationUser> authors, final List<ProjectRole> projectRoles, final GeneratorConfiguration configuration) {
        this.createWithVisibility(worklogs, issues, authors, null, projectRoles, configuration);
    }
    
    @Override
    public WorklogCreator onCreated(final Function<Integer, ?> lambda) {
        this.emitter.register(lambda);
        return this;
    }
}
