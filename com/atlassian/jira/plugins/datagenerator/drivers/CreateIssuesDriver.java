// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.drivers;

import org.slf4j.LoggerFactory;
import com.atlassian.jira.scheme.Scheme;
import java.util.Collections;
import com.google.common.collect.Iterables;
import com.atlassian.jira.plugins.datagenerator.db.Entity;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import java.util.Iterator;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.plugins.datagenerator.timestamp.TimestampGenerator;
import com.atlassian.jira.plugins.datagenerator.util.Randomizer;
import org.apache.commons.lang.math.RandomUtils;
import java.sql.Timestamp;
import com.atlassian.jira.issue.link.IssueLinkType;
import java.util.ArrayList;
import com.google.common.collect.Lists;
import com.atlassian.jira.plugins.datagenerator.timestamp.EqualIntervalGenerator;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.issue.issuetype.IssueType;
import java.util.concurrent.ConcurrentHashMap;
import com.atlassian.jira.plugins.datagenerator.IssueInfo;
import com.atlassian.jira.plugins.datagenerator.config.module.IssueCreationParameters;
import com.atlassian.jira.project.Project;
import org.ofbiz.core.entity.GenericEntityException;
import java.sql.SQLException;
import com.atlassian.jira.plugins.datagenerator.config.Issues;
import com.atlassian.jira.plugins.datagenerator.text.DictionaryTextGenerator;
import java.util.Collection;
import com.google.common.collect.ImmutableList;
import com.atlassian.jira.plugins.datagenerator.IssueLinkPersistStrategy;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.plugins.datagenerator.db.FieldDetector;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.plugins.datagenerator.workflow.WorkflowPostFunctionEmulator;
import java.util.List;
import com.atlassian.jira.plugins.datagenerator.fields.FieldMutators;
import com.atlassian.jira.plugins.datagenerator.text.TextGenerator;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.plugins.datagenerator.ProjectUpdater;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.plugins.datagenerator.db.EntityManager;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.slf4j.Logger;

public class CreateIssuesDriver
{
    private static final Logger log;
    private final GeneratorContext context;
    private final EntityManager entityManager;
    private final WorkflowManager workflowManager;
    private final ProjectManager projectManager;
    private final ConstantsManager constantsManager;
    private final ProjectUpdater projectUpdater;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final TextGenerator summaryGenerator;
    private final TextGenerator descriptionGenerator;
    private final TextGenerator environmentGenerator;
    private final FieldMutators fieldMutators;
    private final List<String> assigneeNames;
    private final List<String> reporterNames;
    private final WorkflowPostFunctionEmulator workflowPostFunctionEmulator;
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final FieldDetector fieldDetector;
    private final IssueSecuritySchemeManager issueSecuritySchemeManager;
    private final IssueSecurityLevelManager issueSecurityLevelManager;
    private IssueWorkflowPersistStrategy persistStrategy;
    private IssueLinkPersistStrategy issueLinkPersistStrategy;
    
    public CreateIssuesDriver(final GeneratorContext context, final EntityManager entityManager, final WorkflowManager workflowManager, final ProjectManager projectManager, final ConstantsManager constantsManager, final IssueTypeSchemeManager issueTypeSchemeManager, final ProjectUpdater projectUpdater, final FieldMutators fieldMutators, final WorkflowPostFunctionEmulator workflowPostFunctionEmulator, final IssueLinkTypeManager issueLinkTypeManager, final FieldDetector fieldDetector, final IssueSecuritySchemeManager issueSecuritySchemeManager, final IssueSecurityLevelManager issueSecurityLevelManager) throws SQLException, GenericEntityException {
        this.context = context;
        this.entityManager = entityManager;
        this.workflowManager = workflowManager;
        this.projectManager = projectManager;
        this.constantsManager = constantsManager;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.projectUpdater = projectUpdater;
        this.fieldMutators = fieldMutators;
        this.workflowPostFunctionEmulator = workflowPostFunctionEmulator;
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.fieldDetector = fieldDetector;
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
        this.issueSecurityLevelManager = issueSecurityLevelManager;
        final Issues issuesCfg = context.generatorConfiguration.issues;
        this.persistStrategy = new IssueWorkflowPersistStrategy(entityManager);
        this.issueLinkPersistStrategy = new IssueLinkPersistStrategy(entityManager);
        this.assigneeNames = (List<String>)ImmutableList.copyOf((Collection)context.assignees.keySet());
        this.reporterNames = (List<String>)ImmutableList.copyOf((Collection)context.reporters.keySet());
        this.summaryGenerator = new DictionaryTextGenerator(issuesCfg.summaryMinLength, issuesCfg.summaryMaxLength);
        this.environmentGenerator = new DictionaryTextGenerator(issuesCfg.environmentPc, issuesCfg.environmentMinLength, issuesCfg.environmentMaxLength);
        this.descriptionGenerator = TextGenerator.getTextGenerator(context.generatorConfiguration, this.reporterNames);
    }
    
    public List<IssueInfo> generate(final Project project, final IssueCreationParameters issueCreationParameters, final int issueCount) throws GenericEntityException, SQLException {
        final Issues issuesCfg = this.context.generatorConfiguration.issues;
        final ConcurrentHashMap<IssueType, JiraWorkflow> workflows = new ConcurrentHashMap<IssueType, JiraWorkflow>();
        long sequenceNumber = 1L;
        final String keyPrefix = project.getKey() + "-";
        CreateIssuesDriver.log.info(String.format("Creating %s issues in project %s", issueCount, project.getKey()));
        final TimestampGenerator timestampGenerator = new EqualIntervalGenerator(this.context.generatorConfiguration, issueCount);
        final List<Priority> priorities = (List<Priority>)ImmutableList.copyOf(this.constantsManager.getPriorities());
        final List<IssueType> standardIssueTypes = (List<IssueType>)ImmutableList.copyOf(this.issueTypeSchemeManager.getNonSubTaskIssueTypesForProject(project));
        final List<IssueType> subTaskIssueTypes = (List<IssueType>)ImmutableList.copyOf(this.issueTypeSchemeManager.getSubTaskIssueTypesForProject(project));
        final Iterator<IssueSecurityLevel> levelsIterator = this.getIssueSecurityLevelIterator(project);
        final List<IssueInfo> issuesInProject = Lists.newArrayListWithCapacity(issueCount);
        final Collection<IssueLinkType> subTaskTypes = this.issueLinkTypeManager.getIssueLinkTypesByName("jira_subtask_link");
        final List<IssueLinkType> issueLinkTypes = new ArrayList<IssueLinkType>(this.issueLinkTypeManager.getIssueLinkTypes());
        if (issueLinkTypes.isEmpty() && issuesCfg.issueLinkProbability != 0.0f) {
            throw new RuntimeException("Issue Link types are not found - either they are not enabled or you have deleted all issue link types");
        }
        if (subTaskTypes == null || subTaskTypes.isEmpty()) {
            throw new RuntimeException("Sub Tasks are not found - either they are not enabled or you have deleted all subtask types");
        }
        final long subTaskTypeId = subTaskTypes.iterator().next().getId();
        int generateSubTaskCount = 0;
        Entity parentIssue = null;
        Entity previousIssue = null;
        for (final Timestamp created : timestampGenerator) {
            if (generateSubTaskCount == 0 && !subTaskIssueTypes.isEmpty() && parentIssue != null && RandomUtils.nextInt(100) < issuesCfg.subTasksProbability * 100.0f) {
                generateSubTaskCount = RandomUtils.nextInt(issuesCfg.subTasksMaxLength - issuesCfg.subTasksMinLength) + issuesCfg.subTasksMinLength;
            }
            final List<IssueType> issueTypes = (generateSubTaskCount > 0) ? subTaskIssueTypes : standardIssueTypes;
            final IssueType issueType = Randomizer.randomItem(issueTypes);
            final JiraWorkflow workflow = workflows.computeIfAbsent(issueType, key -> this.workflowManager.getWorkflow(project.getId(), issueType.getId()));
            final Entity issue = this.createIssue(project, workflow, sequenceNumber++, keyPrefix, priorities, issueType, this.persistStrategy, created, this.getLevelForIssue(levelsIterator, this.context), issueCreationParameters);
            if (generateSubTaskCount > 0 && parentIssue != null) {
                this.issueLinkPersistStrategy.createIssueLink(parentIssue.getId(), issue.getId(), subTaskTypeId, 0L);
                --generateSubTaskCount;
            }
            else {
                parentIssue = issue;
            }
            final IssueLinkType issueLinkType = Randomizer.randomItem(issueLinkTypes);
            if (Math.random() < issuesCfg.issueLinkProbability && previousIssue != null) {
                this.issueLinkPersistStrategy.createIssueLink(previousIssue.getId(), issue.getId(), issueLinkType.getId(), 0L);
            }
            final String assignee = (String)issue.get("assignee");
            final String reporter = (String)issue.get("reporter");
            issuesInProject.add(new IssueInfo(project, issue.getId(), assignee, reporter, issueType, created.getTime()));
            this.context.incProgress();
            previousIssue = issue;
            if (issuesInProject.size() % 10000 == 0) {
                CreateIssuesDriver.log.warn(String.format("Created %d / %d issues", issuesInProject.size(), issueCount));
            }
        }
        this.projectUpdater.updateProjectCounters(project, sequenceNumber);
        return issuesInProject;
    }
    
    public void flush() throws SQLException, GenericEntityException {
        try {
            this.persistStrategy.close();
        }
        catch (final SQLException | GenericEntityException e) {
            CreateIssuesDriver.log.warn("Could not close persistStrategy", (Throwable)e);
        }
        finally {
            this.persistStrategy = new IssueWorkflowPersistStrategy(this.entityManager);
        }
        try {
            this.issueLinkPersistStrategy.close();
        }
        catch (final SQLException e2) {
            CreateIssuesDriver.log.warn("Could not close issueLinkPersistStrategy", (Throwable)e2);
        }
        finally {
            this.issueLinkPersistStrategy = new IssueLinkPersistStrategy(this.entityManager);
        }
    }
    
    public void close() throws SQLException, GenericEntityException {
        this.persistStrategy.close();
        this.issueLinkPersistStrategy.close();
    }
    
    private Iterator<IssueSecurityLevel> getIssueSecurityLevelIterator(final Project project) {
        final Scheme scheme = this.issueSecuritySchemeManager.getSchemeFor(project);
        if (scheme != null) {
            final List<IssueSecurityLevel> issueSecurityLevels = this.issueSecurityLevelManager.getIssueSecurityLevels((long)scheme.getId());
            return Iterables.cycle((Iterable)issueSecurityLevels).iterator();
        }
        return Iterables.cycle((Iterable)Collections.emptyList()).iterator();
    }
    
    private IssueSecurityLevel getLevelForIssue(final Iterator<IssueSecurityLevel> issueSecurityLevels, final GeneratorContext context) {
        if (issueSecurityLevels.hasNext()) {
            return issueSecurityLevels.next();
        }
        return null;
    }
    
    private Entity createIssue(final Project project, final JiraWorkflow jiraWorkflow, final long sequenceNumber, final String keyPrefix, final List<Priority> priorities, final IssueType issueType, final IssueWorkflowPersistStrategy persistStrategy, final Timestamp created, final IssueSecurityLevel level, final IssueCreationParameters issueCreationParameters) throws SQLException, GenericEntityException {
        final Entity issue = new Entity();
        final List<String> potentialAssignees = (issueCreationParameters == null || issueCreationParameters.getAssigneeUsernames().isEmpty()) ? this.assigneeNames : issueCreationParameters.getAssigneeUsernames();
        final List<String> potentialReporters = (issueCreationParameters == null || issueCreationParameters.getReporterUsernames().isEmpty()) ? this.reporterNames : issueCreationParameters.getReporterUsernames();
        issue.put("summary", this.summaryGenerator.generateText()).put("assignee", Randomizer.randomItem(potentialAssignees)).put("reporter", Randomizer.randomItem(potentialReporters)).put("created", created).put("updated", created).put("description", this.descriptionGenerator.generateText()).put("project", project.getId()).put("votes", 0).put("watches", 0).put("type", issueType.getId()).put("environment", this.environmentGenerator.generateText());
        if (this.fieldDetector.isFieldInEntity("Issue", "number")) {
            issue.put("number", sequenceNumber);
        }
        else {
            issue.put("key", keyPrefix + sequenceNumber);
        }
        if (priorities.size() > 0) {
            issue.put("priority", Randomizer.randomItem(priorities).getId());
        }
        final IssueWorkflowDriver issueWorkflowDriver = new IssueWorkflowDriver(this.context, this.constantsManager, this.fieldMutators, this.workflowPostFunctionEmulator, jiraWorkflow, issue, potentialAssignees);
        issueWorkflowDriver.initialAction();
        for (int i = 0; i < this.context.generatorConfiguration.transitions.avgCount; ++i) {
            issueWorkflowDriver.randomAction(issueCreationParameters);
        }
        if (this.context.generatorConfiguration.assignSecurityLevelsToIssues && level != null) {
            issue.put("security", level.getId());
        }
        persistStrategy.persist(issueWorkflowDriver);
        return issue;
    }
    
    static {
        log = LoggerFactory.getLogger((Class)CreateIssuesDriver.class);
    }
}
