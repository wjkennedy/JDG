// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators;

import org.slf4j.LoggerFactory;
import com.atlassian.jira.plugins.datagenerator.distribution.Distributor;
import com.atlassian.jira.plugins.datagenerator.distribution.MaxPercentageDistributor;
import com.atlassian.jira.plugins.datagenerator.distribution.EqualValueDistributor;
import com.atlassian.jira.issue.index.IndexException;
import org.ofbiz.core.entity.GenericValue;
import java.sql.SQLException;
import org.ofbiz.core.entity.GenericEntityException;
import java.util.Set;
import com.atlassian.jira.plugins.datagenerator.drivers.CreateIssuesDriver;
import java.util.Iterator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.function.Function;
import com.atlassian.jira.plugins.datagenerator.IssueInfo;
import java.util.List;
import com.atlassian.jira.plugins.datagenerator.config.module.IssueCreationParameters;
import com.google.common.collect.ImmutableList;
import com.atlassian.jira.issue.fields.CustomField;
import com.google.common.collect.Sets;
import com.atlassian.jira.plugins.datagenerator.drivers.CustomFieldDriver;
import com.atlassian.jira.plugins.datagenerator.util.PhasedTimer;
import com.google.common.collect.Lists;
import org.apache.commons.lang.time.StopWatch;
import java.util.Collections;
import com.atlassian.jira.plugins.datagenerator.GeneratorConfigurationUtil;
import com.atlassian.jira.plugins.datagenerator.drivers.WorklogDriver;
import com.atlassian.jira.plugins.datagenerator.drivers.CommentDriver;
import com.atlassian.jira.plugins.datagenerator.db.DbIndexUtils;
import com.atlassian.jira.plugins.datagenerator.db.EntityManager;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.springframework.beans.factory.annotation.Autowired;
import com.google.common.collect.Maps;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.plugins.datagenerator.db.postgres.JiraSequenceIdGenerator;
import java.util.Collection;
import com.atlassian.jira.project.Project;
import java.util.Map;
import com.atlassian.jira.plugins.datagenerator.plugins.access.DataPluginConfigurationAccessor;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.plugins.datagenerator.fields.FieldLayoutHelper;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.plugins.datagenerator.drivers.CreateIssuesDriverFactory;
import com.atlassian.jira.plugins.datagenerator.fields.FieldMutators;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.project.ProjectManager;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class IssueGenerator implements DataGenerator
{
    private static final Logger LOG;
    protected final ProjectManager projectManager;
    private final IssueManager issueManager;
    private final IssueIndexManager issueIndexManager;
    private GeneratorConfiguration generatorConfiguration;
    private final FieldMutators fieldMutators;
    private final CreateIssuesDriverFactory createIssueDriverFactory;
    private final CustomFieldGenerator customFieldGenerator;
    private final CustomFieldManager customFieldManager;
    private final FieldLayoutHelper fieldLayoutHelper;
    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final DataPluginConfigurationAccessor dataPluginConfigurationAccessor;
    private final Map<Project, Collection<String>> dataPluginPointProjects;
    private final JiraSequenceIdGenerator sequenceIdGenerator;
    
    @Autowired
    public IssueGenerator(@ComponentImport final ProjectManager projectManager, @ComponentImport final IssueManager issueManager, @ComponentImport final IssueIndexManager issueIndexManager, @ComponentImport final FieldLayoutManager fieldLayoutManager, final FieldMutators fieldMutators, final CustomFieldGenerator customFieldGenerator, @ComponentImport final CustomFieldManager customFieldManager, @ComponentImport final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, final CreateIssuesDriverFactory createIssueDriverFactory, final DataPluginConfigurationAccessor dataPluginConfigurationAccessor, final JiraSequenceIdGenerator sequenceIdGenerator) {
        this.projectManager = projectManager;
        this.issueManager = issueManager;
        this.issueIndexManager = issueIndexManager;
        this.fieldMutators = fieldMutators;
        this.createIssueDriverFactory = createIssueDriverFactory;
        this.customFieldGenerator = customFieldGenerator;
        this.customFieldManager = customFieldManager;
        this.fieldLayoutHelper = new FieldLayoutHelper(fieldLayoutManager);
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        this.dataPluginConfigurationAccessor = dataPluginConfigurationAccessor;
        this.sequenceIdGenerator = sequenceIdGenerator;
        this.dataPluginPointProjects = Maps.newHashMap();
    }
    
    @Override
    public void generate(final GeneratorContext context) throws GenericEntityException, SQLException {
        final boolean indexingPreviouslyEnabled = ImportUtils.isIndexIssues();
        if (indexingPreviouslyEnabled) {
            IssueGenerator.LOG.info("Disabling indexes temporarily");
            ImportUtils.setIndexIssues(false);
        }
        this.generatorConfiguration = context.generatorConfiguration;
        final Iterator<Integer> issuesInProjectIterator = this.getIssueDistribution().iterator();
        final EntityManager entityManager = new EntityManager(this.sequenceIdGenerator, this.generatorConfiguration);
        DbIndexUtils.dropIndexes();
        final CreateIssuesDriver createIssuesDriver = this.createIssueDriverFactory.create(context, entityManager);
        final CommentDriver commentDriver = new CommentDriver(context, entityManager, this.dataPluginConfigurationAccessor);
        final WorklogDriver worklogDriver = new WorklogDriver(context, entityManager);
        final List<Project> allProjectsToGenerate = GeneratorConfigurationUtil.getProjects(this.generatorConfiguration, this.projectManager);
        final Map<Project, Collection<String>> pluginProjects = this.dataPluginConfigurationAccessor.getProjectsWithPlugins();
        this.dataPluginPointProjects.clear();
        for (final Project project : allProjectsToGenerate) {
            final Collection<String> pluginKeys = pluginProjects.get(project);
            if (pluginKeys != null) {
                this.dataPluginPointProjects.put(project, pluginKeys);
            }
            else {
                this.dataPluginPointProjects.put(project, (Collection<String>)Collections.emptyList());
            }
        }
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int totalIssueCount = this.generatorConfiguration.issues.count;
        context.createdIssuesIds = Lists.newArrayListWithCapacity(totalIssueCount);
        final Iterator<Map.Entry<Project, Collection<String>>> projectIterator = this.dataPluginPointProjects.entrySet().iterator();
        final PhasedTimer phasedTimer = new PhasedTimer();
        context.resetProgress(String.format("Generating %d issues", totalIssueCount), totalIssueCount);
        final CustomFieldDriver customFieldDriver = new CustomFieldDriver(context, entityManager, this.customFieldGenerator, this.customFieldManager, this.issueTypeScreenSchemeManager, this.fieldLayoutHelper);
        int counter = 0;
        int createdIssues = 0;
        while (projectIterator.hasNext()) {
            final Map.Entry<Project, Collection<String>> projectEntry = projectIterator.next();
            final Project project2 = projectEntry.getKey();
            final Set<String> assigneeUsernamesSet = Sets.newHashSet();
            final Set<String> reporterUsernamesSet = Sets.newHashSet();
            final Set<CustomField> customFieldsToPopulateSet = Sets.newHashSet();
            for (final String completeKey : projectEntry.getValue()) {
                final IssueCreationParameters parameters = this.dataPluginConfigurationAccessor.getIssueCreationParameters(completeKey, project2);
                assigneeUsernamesSet.addAll(parameters.getAssigneeUsernames());
                reporterUsernamesSet.addAll(parameters.getReporterUsernames());
                customFieldsToPopulateSet.addAll(parameters.getCustomFieldsToPopulate());
            }
            final IssueCreationParameters issueCreationParameters = new IssueCreationParameters((List<String>)ImmutableList.copyOf((Collection)assigneeUsernamesSet), (List<String>)ImmutableList.copyOf((Collection)reporterUsernamesSet), (List<CustomField>)ImmutableList.copyOf((Collection)customFieldsToPopulateSet));
            this.fieldMutators.init(context, issueCreationParameters);
            phasedTimer.startPhase("generating issues:");
            final List<IssueInfo> issuesInProject = createIssuesDriver.generate(project2, issueCreationParameters, issuesInProjectIterator.next());
            createdIssues += issuesInProject.size();
            phasedTimer.startPhase("generating comments:");
            commentDriver.generate(issuesInProject, this.dataPluginPointProjects);
            phasedTimer.startPhase("generating worklogs:");
            worklogDriver.generate(issuesInProject);
            phasedTimer.startPhase("generating custom field values:");
            customFieldDriver.generate(project2, issuesInProject, issueCreationParameters);
            IssueGenerator.LOG.info("created issues: " + createdIssues);
            if (createdIssues >= 10000) {
                IssueGenerator.LOG.info("Flushing");
                createIssuesDriver.flush();
                customFieldDriver.flush();
                createdIssues = 0;
            }
            final List<Long> issueIds = issuesInProject.stream().map((Function<? super Object, ?>)IssueInfo::getId).collect((Collector<? super Object, ?, List<Long>>)Collectors.toList());
            for (final String completeKey2 : projectEntry.getValue()) {
                phasedTimer.startPhase("updating issues with plugin: " + completeKey2);
                this.dataPluginConfigurationAccessor.performIssueUpdates(completeKey2, project2, issueIds);
            }
            phasedTimer.stop();
            context.createdIssuesIds.addAll(issueIds);
            IssueGenerator.LOG.warn(String.format("Created issues for %d/%d projects", counter + 1, allProjectsToGenerate.size()));
            ++counter;
        }
        createIssuesDriver.close();
        customFieldDriver.close();
        entityManager.shutdown();
        DbIndexUtils.createIndexes();
        if (indexingPreviouslyEnabled) {
            IssueGenerator.LOG.info("Re-enabling indexes");
            ImportUtils.setIndexIssues(indexingPreviouslyEnabled);
        }
        stopWatch.stop();
        final String message = String.format("Created %d issues in %s", context.createdIssuesIds.size(), stopWatch.toString());
        context.messages.add(message);
        context.messages.addAll(phasedTimer.toStrings());
        IssueGenerator.LOG.info(message);
    }
    
    public void reindex(final GeneratorContext context) throws IndexException {
        if (this.generatorConfiguration.performReindex && !context.createdIssuesIds.isEmpty()) {
            final List<Long> createdIssuesIds = context.createdIssuesIds;
            IssueGenerator.LOG.info("Reindexing {} issues.", (Object)createdIssuesIds.size());
            context.resetProgress(String.format("Reindexing %d issues", createdIssuesIds.size()), 1);
            final StopWatch reindexStopWatch = new StopWatch();
            reindexStopWatch.start();
            final List<GenericValue> issueObjects = this.issueManager.getIssues((Collection)createdIssuesIds);
            this.issueIndexManager.reIndexIssues((Collection)issueObjects);
            reindexStopWatch.stop();
            final String message = String.format("Reindexed %d issues in %s", createdIssuesIds.size(), reindexStopWatch.toString());
            context.messages.add(message);
            IssueGenerator.LOG.info(message);
            IssueGenerator.LOG.info("Running post reindex actions.");
            final StopWatch postReindexStopWatch = new StopWatch();
            postReindexStopWatch.start();
            context.resetProgress(String.format("Running post reindex actions for %d projects", this.dataPluginPointProjects.size()), this.dataPluginPointProjects.size());
            for (final Map.Entry<Project, Collection<String>> dataPluginPointProject : this.dataPluginPointProjects.entrySet()) {
                final Project project = dataPluginPointProject.getKey();
                for (final String completeKey : dataPluginPointProject.getValue()) {
                    this.dataPluginConfigurationAccessor.performPostReindexUpdates(completeKey, project);
                }
                context.incProgress();
            }
            postReindexStopWatch.stop();
            final String msg = String.format("Run post reindex actions on %d projects in %s", this.dataPluginPointProjects.size(), postReindexStopWatch.toString());
            context.messages.add(msg);
            IssueGenerator.LOG.info(msg);
        }
    }
    
    private List<Integer> getIssueDistribution() {
        Distributor issuesInProjectDistributor = new EqualValueDistributor();
        if (this.generatorConfiguration.distribution.equals("max.percentage.distribution")) {
            issuesInProjectDistributor = new MaxPercentageDistributor(this.generatorConfiguration.maxPercentagePerProject, this.generatorConfiguration.distributionTailTreshold);
        }
        return issuesInProjectDistributor.distribute(this.generatorConfiguration.issues.count, this.generatorConfiguration.projectIds.length);
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)IssueGenerator.class);
    }
}
