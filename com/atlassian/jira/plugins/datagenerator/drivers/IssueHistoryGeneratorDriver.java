// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.drivers;

import org.slf4j.LoggerFactory;
import org.ofbiz.core.entity.GenericEntityException;
import java.util.Collections;
import com.atlassian.jira.project.Project;
import java.sql.Timestamp;
import com.google.common.collect.Lists;
import com.atlassian.jira.plugins.datagenerator.db.ChangeGroupEntity;
import java.sql.SQLException;
import com.atlassian.jira.plugins.datagenerator.db.ChangeItemEntity;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.plugins.datagenerator.util.Randomizer;
import org.apache.commons.lang.time.StopWatch;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Collection;
import java.util.function.Function;
import com.atlassian.jira.plugins.datagenerator.GeneratorConfigurationUtil;
import java.util.List;
import com.atlassian.jira.plugins.datagenerator.generators.issuehistory.IssueHistoryPersistStrategy;
import com.atlassian.jira.plugins.datagenerator.db.EntityManager;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.plugins.datagenerator.generators.issuehistory.DescriptionChangeGenerator;
import com.atlassian.jira.plugins.datagenerator.generators.issuehistory.ReporterChangeGenerator;
import com.atlassian.jira.plugins.datagenerator.generators.issuehistory.AssigneeChangeGenerator;
import com.atlassian.jira.plugins.datagenerator.db.postgres.JiraSequenceIdGenerator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.user.UserKeyService;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class IssueHistoryGeneratorDriver implements IDataGeneratorDriver
{
    private static final Logger log;
    private static final int ONE_YEAR = 31536000;
    private final UserKeyService userKeyService;
    private final IssueManager issueManager;
    private final ProjectManager projectManager;
    private final JiraSequenceIdGenerator sequenceIdGenerator;
    private final AssigneeChangeGenerator assigneeChangeGenerator;
    private final ReporterChangeGenerator reporterChangeGenerator;
    private final DescriptionChangeGenerator descriptionChangeGenerator;
    
    @Autowired
    public IssueHistoryGeneratorDriver(@ComponentImport final UserKeyService userKeyService, @ComponentImport final IssueManager issueManager, @ComponentImport final ProjectManager projectManager, final JiraSequenceIdGenerator sequenceIdGenerator, final AssigneeChangeGenerator assigneeChangeGenerator, final ReporterChangeGenerator reporterChangeGenerator, final DescriptionChangeGenerator descriptionChangeGenerator) {
        this.userKeyService = userKeyService;
        this.issueManager = issueManager;
        this.projectManager = projectManager;
        this.sequenceIdGenerator = sequenceIdGenerator;
        this.assigneeChangeGenerator = assigneeChangeGenerator;
        this.reporterChangeGenerator = reporterChangeGenerator;
        this.descriptionChangeGenerator = descriptionChangeGenerator;
    }
    
    @Override
    public void generate(final GeneratorContext context) throws Exception {
        try (final EntityManager entityManager = new EntityManager(this.sequenceIdGenerator, context.generatorConfiguration);
             final IssueHistoryPersistStrategy persistStrategy = new IssueHistoryPersistStrategy(entityManager)) {
            final List<Long> issueIds = GeneratorConfigurationUtil.getProjects(context.generatorConfiguration, this.projectManager).stream().map((Function<? super Object, ?>)this::getIssueIdsForProject).flatMap((Function<? super Object, ? extends Stream<?>>)Collection::stream).collect((Collector<? super Object, ?, List<Long>>)Collectors.toList());
            if (issueIds.isEmpty()) {
                context.resetProgress("Skipping generation, as there are no issues in the selected projects", 0);
                return;
            }
            this.generateAssigneeChanges(context, persistStrategy, issueIds);
            this.generateReportersChanges(context, persistStrategy, issueIds);
            this.generateDescriptionChanges(context, persistStrategy, issueIds);
        }
    }
    
    private void generateAssigneeChanges(final GeneratorContext context, final IssueHistoryPersistStrategy persistStrategy, final List<Long> issueIds) {
        this.generateIssueHistoryChanges(context, issueIds, "assignee", context.generatorConfiguration.assigneesCount, issueId -> this.assigneeChangeGenerator.generate(context, issueId).ifPresent(changeItem -> this.persistHistoryChange(context, persistStrategy, issueId, changeItem)));
    }
    
    private void generateReportersChanges(final GeneratorContext context, final IssueHistoryPersistStrategy persistStrategy, final List<Long> issueIds) {
        this.generateIssueHistoryChanges(context, issueIds, "reporter", context.generatorConfiguration.reportersCount, issueId -> this.reporterChangeGenerator.generate(context, issueId).ifPresent(changeItem -> this.persistHistoryChange(context, persistStrategy, issueId, changeItem)));
    }
    
    private void generateDescriptionChanges(final GeneratorContext context, final IssueHistoryPersistStrategy persistStrategy, final List<Long> issueIds) {
        this.generateIssueHistoryChanges(context, issueIds, "description", context.generatorConfiguration.descriptionsCount, issueId -> this.descriptionChangeGenerator.generate(context, issueId).ifPresent(changeItem -> this.persistHistoryChange(context, persistStrategy, issueId, changeItem)));
    }
    
    private void generateIssueHistoryChanges(final GeneratorContext context, final List<Long> issueIds, final String entityName, final int changesCount, final IssueHistoryGenerator issueHistoryGenerator) {
        if (changesCount == 0) {
            context.messages.add(String.format("No %s generation requested, skipping", entityName));
            return;
        }
        final StopWatch stopWatch = new StopWatch();
        context.resetProgress(String.format("Generating %d %s changes", changesCount, entityName), changesCount);
        stopWatch.start();
        for (int i = 0; i < changesCount; ++i) {
            final Long issueId = Randomizer.randomItem(issueIds);
            issueHistoryGenerator.generate(issueId);
            context.progress.addAndGet(1);
        }
        stopWatch.stop();
        context.messages.add(String.format("Generated %d %s changes in %s", changesCount, entityName, stopWatch.toString()));
    }
    
    private void persistHistoryChange(final GeneratorContext context, final IssueHistoryPersistStrategy persistStrategy, final Long issueId, final ChangeItemBean changeItemBean) {
        final ChangeGroupEntity changeGroupEntity = this.createChangeGroupEntity(context, issueId);
        try {
            persistStrategy.persist(changeGroupEntity, new ChangeItemEntity(changeGroupEntity, changeItemBean));
        }
        catch (final SQLException e) {
            IssueHistoryGeneratorDriver.log.error(String.format("Exception when persisting issue history items '%s' and '%s'", changeGroupEntity, changeItemBean), (Throwable)e);
        }
    }
    
    private ChangeGroupEntity createChangeGroupEntity(final GeneratorContext context, final Long issueId) {
        return new ChangeGroupEntity().setAuthor(this.getChangeAuthor(context)).setIssueId(issueId).setCreated(this.getTimestampFromPast());
    }
    
    private String getChangeAuthor(final GeneratorContext context) {
        String authorUsername = null;
        switch (context.generatorConfiguration.changesAuthor) {
            case CURRENT_USER: {
                authorUsername = context.userName;
                break;
            }
            case DEVELOPERS: {
                authorUsername = Randomizer.randomItem((List<String>)Lists.newArrayList((Iterable)context.assignees.keySet()));
                break;
            }
            case ALL_USERS: {
                authorUsername = Randomizer.randomItem((List<String>)Lists.newArrayList((Iterable)context.reporters.keySet()));
                break;
            }
            default: {
                throw new IllegalArgumentException(String.format("Invalid changes author value - %s", context.generatorConfiguration.changesAuthor));
            }
        }
        return this.userKeyService.getKeyForUsername(authorUsername);
    }
    
    private Timestamp getTimestampFromPast() {
        final long howLongAgo = Randomizer.randomIntInRange(1, 31536000) * 1000L;
        return new Timestamp(System.currentTimeMillis() - howLongAgo);
    }
    
    private Collection<Long> getIssueIdsForProject(final Project project) {
        try {
            return this.issueManager.getIssueIdsForProject(project.getId());
        }
        catch (final GenericEntityException e) {
            IssueHistoryGeneratorDriver.log.error(String.format("Exception when getting issue ids for project %s", project.getId()), (Throwable)e);
            return (Collection<Long>)Collections.emptyList();
        }
    }
    
    static {
        log = LoggerFactory.getLogger((Class)IssueHistoryGeneratorDriver.class);
    }
    
    @FunctionalInterface
    private interface IssueHistoryGenerator
    {
        void generate(final Long p0);
    }
}
