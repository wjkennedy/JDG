// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.drivers;

import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.task.ProvidesTaskProgress;
import org.slf4j.LoggerFactory;
import java.lang.invoke.SerializedLambda;
import java.util.Collections;
import com.google.common.collect.Iterables;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.function.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.atlassian.jira.plugins.datagenerator.CustomFieldInfo;
import com.atlassian.jira.project.Project;
import java.util.List;
import java.util.Map;
import com.atlassian.jira.plugins.datagenerator.FieldScreenDistribution;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.task.TaskDescriptor;
import java.util.concurrent.Callable;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.task.TaskContext;
import com.atlassian.jira.plugins.datagenerator.generators.IssueWorklogGenerator;
import com.atlassian.jira.plugins.datagenerator.generators.AdditionalCommentGenerator;
import com.atlassian.jira.plugins.datagenerator.generators.IssueWatcherGenerator;
import com.atlassian.jira.plugins.datagenerator.generators.AttachmentGenerator;
import com.atlassian.jira.plugins.datagenerator.plugins.access.MetadataPluginConfigurationAccessor;
import com.atlassian.jira.plugins.datagenerator.generators.IssueSecurityGenerator;
import com.atlassian.jira.plugins.datagenerator.generators.ConstantGeneratorFactory;
import com.atlassian.jira.plugins.datagenerator.generators.SubtaskTypeGenerator;
import com.atlassian.jira.plugins.datagenerator.generators.WorkflowGenerator;
import com.atlassian.jira.plugins.datagenerator.PermissionSchemeGenerator;
import com.atlassian.jira.plugins.datagenerator.generators.IssueTypeGenerator;
import com.atlassian.jira.plugins.datagenerator.generators.ProjectGenerator;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import java.util.concurrent.atomic.AtomicReference;
import com.atlassian.jira.plugins.datagenerator.UserUtils;
import com.atlassian.jira.plugins.datagenerator.generators.FieldConfigurationGenerator;
import com.atlassian.jira.plugins.datagenerator.generators.CustomFieldGenerator;
import com.atlassian.jira.plugins.datagenerator.generators.RoleGenerator;
import com.atlassian.jira.plugins.datagenerator.generators.GroupGenerator;
import com.atlassian.jira.plugins.datagenerator.generators.UserGenerator;
import com.atlassian.jira.plugins.datagenerator.generators.RapidViewsGenerator;
import com.atlassian.jira.plugins.datagenerator.generators.ComponentGenerator;
import com.atlassian.jira.plugins.datagenerator.generators.VersionGenerator;
import com.atlassian.jira.plugins.datagenerator.generators.IssueGenerator;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class DataGeneratorDriver
{
    private static final Logger log;
    private static final String ERROR_GENERATING_DATA_SEE_JIRA_LOG_FOR_DETAILS = "Error generating data, see Jira log for details.";
    private final IssueGenerator issueGenerator;
    private final VersionGenerator versionGenerator;
    private final ComponentGenerator componentGenerator;
    private final RapidViewsGenerator rapidViewsGenerator;
    private final UserGenerator userGenerator;
    private final GroupGenerator groupGenerator;
    private final RoleGenerator roleGenerator;
    private final CustomFieldGenerator customFieldGenerator;
    private final FieldConfigurationGenerator fieldConfigurationGenerator;
    private final UserUtils userUtils;
    private final AtomicReference<GeneratorContext> lastContext;
    private final TaskManager taskManager;
    private final ProjectGenerator projectGenerator;
    private final IssueTypeGenerator issueTypeGenerator;
    private final PermissionSchemeGenerator permissionSchemeGenerator;
    private final WorkflowGenerator workflowGenerator;
    private final SubtaskTypeGenerator subtaskTypeGenerator;
    private final ConstantGeneratorFactory constantGeneratorFactory;
    private final IssueSecurityGenerator issueSecurityGenerator;
    private final MetadataPluginConfigurationAccessor metadataPluginConfigurationAccessor;
    private final AttachmentGenerator attachmentGenerator;
    private final IssueWatcherGenerator issueWatcherGenerator;
    private final AdditionalCommentGenerator additionalCommentGenerator;
    private final IssueWorklogGenerator issueWorklogGenerator;
    private final IssueHistoryGeneratorDriver issueHistoryGeneratorDriver;
    private static final TaskContext TASK_CONTEXT;
    
    @Autowired
    public DataGeneratorDriver(final ConstantGeneratorFactory constantGeneratorFactory, final SubtaskTypeGenerator subtaskTypeGenerator, final WorkflowGenerator workflowGenerator, final PermissionSchemeGenerator permissionSchemeGenerator, final IssueTypeGenerator issueTypeGenerator, final ProjectGenerator projectGenerator, @ComponentImport final TaskManager taskManager, final UserUtils userUtils, final CustomFieldGenerator customFieldGenerator, final FieldConfigurationGenerator fieldConfigurationGenerator, final UserGenerator userGenerator, final IssueGenerator issueGenerator, final VersionGenerator versionGenerator, final RapidViewsGenerator rapidViewsGenerator, final GroupGenerator groupGenerator, final RoleGenerator roleGenerator, final IssueSecurityGenerator issueSecurityGenerator, final MetadataPluginConfigurationAccessor metadataPluginConfigurationAccessor, final AttachmentGenerator attachmentGenerator, final AdditionalCommentGenerator additionalCommentGenerator, final ComponentGenerator componentGenerator, final IssueWatcherGenerator issueWatcherGenerator, final IssueWorklogGenerator issueWorklogGenerator, final IssueHistoryGeneratorDriver issueHistoryGeneratorDriver) {
        this.lastContext = new AtomicReference<GeneratorContext>();
        this.constantGeneratorFactory = constantGeneratorFactory;
        this.subtaskTypeGenerator = subtaskTypeGenerator;
        this.workflowGenerator = workflowGenerator;
        this.permissionSchemeGenerator = permissionSchemeGenerator;
        this.issueTypeGenerator = issueTypeGenerator;
        this.projectGenerator = projectGenerator;
        this.taskManager = taskManager;
        this.userUtils = userUtils;
        this.customFieldGenerator = customFieldGenerator;
        this.fieldConfigurationGenerator = fieldConfigurationGenerator;
        this.userGenerator = userGenerator;
        this.groupGenerator = groupGenerator;
        this.issueGenerator = issueGenerator;
        this.versionGenerator = versionGenerator;
        this.componentGenerator = componentGenerator;
        this.rapidViewsGenerator = rapidViewsGenerator;
        this.roleGenerator = roleGenerator;
        this.issueSecurityGenerator = issueSecurityGenerator;
        this.metadataPluginConfigurationAccessor = metadataPluginConfigurationAccessor;
        this.attachmentGenerator = attachmentGenerator;
        this.additionalCommentGenerator = additionalCommentGenerator;
        this.issueWatcherGenerator = issueWatcherGenerator;
        this.issueWorklogGenerator = issueWorklogGenerator;
        this.issueHistoryGeneratorDriver = issueHistoryGeneratorDriver;
    }
    
    public synchronized boolean scheduleMetadataGeneration(final GeneratorContext context) {
        if (this.taskManager.hasLiveTaskWithContext(DataGeneratorDriver.TASK_CONTEXT)) {
            return false;
        }
        context.phase = "Jira metadata generation";
        context.currentTask.set("Preparing metadata generation");
        this.lastContext.set(context);
        this.taskManager.submitTask((Callable)GeneratorTask.withErrorHandling(context, this::generateMetadata), "Generating Jira metadata", DataGeneratorDriver.TASK_CONTEXT);
        return true;
    }
    
    public synchronized boolean scheduleDataGeneration(final GeneratorContext context) {
        if (this.taskManager.hasLiveTaskWithContext(DataGeneratorDriver.TASK_CONTEXT)) {
            return false;
        }
        context.phase = "Jira data generation";
        context.currentTask.set("Preparing data generation");
        this.lastContext.set(context);
        this.taskManager.submitTask((Callable)GeneratorTask.withErrorHandling(context, this::generateData), "Generating Jira data", DataGeneratorDriver.TASK_CONTEXT);
        return true;
    }
    
    public synchronized TaskDescriptor<String> scheduleBothPhases(final GeneratorContext context) {
        if (this.taskManager.hasLiveTaskWithContext(DataGeneratorDriver.TASK_CONTEXT)) {
            return null;
        }
        context.phase = "Jira metadata and data generation";
        this.lastContext.set(context);
        return (TaskDescriptor<String>)this.taskManager.submitTask((Callable)GeneratorTask.withErrorHandling(context, this::generateMetadata, this::generateData, this::generateAdditionalData), "Generating Jira metadata and data", DataGeneratorDriver.TASK_CONTEXT);
    }
    
    public synchronized boolean scheduleAdditionalDataGeneration(final GeneratorContext context) {
        if (this.taskManager.hasLiveTaskWithContext(DataGeneratorDriver.TASK_CONTEXT)) {
            return false;
        }
        context.phase = "Jira additional data generation";
        context.currentTask.set("Preparing data generation");
        this.lastContext.set(context);
        this.taskManager.submitTask((Callable)GeneratorTask.withErrorHandling(context, this::generateAdditionalData), "Generating Jira attachment data", DataGeneratorDriver.TASK_CONTEXT);
        return true;
    }
    
    public synchronized boolean scheduleIssueHistoryGeneration(final GeneratorContext context) {
        if (this.taskManager.hasLiveTaskWithContext(DataGeneratorDriver.TASK_CONTEXT)) {
            return false;
        }
        context.phase = "Jira issue history generation";
        context.currentTask.set("Preparing issue history generation");
        this.lastContext.set(context);
        this.taskManager.submitTask((Callable)GeneratorTask.withErrorHandling(context, this::generateIssueHistory), "Generating Jira issue history", DataGeneratorDriver.TASK_CONTEXT);
        return true;
    }
    
    public synchronized boolean scheduleWorklogDataGeneration(final GeneratorContext context) {
        if (this.taskManager.hasLiveTaskWithContext(DataGeneratorDriver.TASK_CONTEXT)) {
            return false;
        }
        context.phase = "Jira worklog data generation";
        context.currentTask.set("Preparing data generation");
        this.lastContext.set(context);
        this.taskManager.submitTask((Callable)GeneratorTask.withErrorHandling(context, this::generateWorklogData), "Generating Jira worklog data", DataGeneratorDriver.TASK_CONTEXT);
        return true;
    }
    
    public GeneratorContext getLastContext() {
        return this.lastContext.get();
    }
    
    public void clearLastContext() {
        this.lastContext.set(null);
    }
    
    private void generateMetadata(final GeneratorContext context) throws Exception {
        final Map<String, String> moduleUsers = this.metadataPluginConfigurationAccessor.prepareModuleUsersForExecution();
        this.constantGeneratorFactory.createGenerator(ConstantsManager.CONSTANT_TYPE.STATUS.getType()).generate(context);
        this.issueTypeGenerator.generate(context);
        this.subtaskTypeGenerator.generate(context);
        this.constantGeneratorFactory.createGenerator(ConstantsManager.CONSTANT_TYPE.RESOLUTION.getType()).generate(context);
        final List<Project> generatedProjects = this.projectGenerator.generate(context);
        this.workflowGenerator.generate(context);
        this.userGenerator.prepare(context);
        this.userGenerator.generate();
        this.groupGenerator.generate(context);
        this.roleGenerator.prepare(context);
        this.roleGenerator.generate();
        final FieldScreenDistribution fieldScreenDistribution = FieldScreenDistribution.calculateRandomDistribution(context.generatorConfiguration.customFields, generatedProjects);
        final List<CustomFieldInfo> generatedCustomFields = this.customFieldGenerator.generate(context, fieldScreenDistribution);
        this.fieldConfigurationGenerator.generate(context, fieldScreenDistribution, generatedCustomFields);
        this.permissionSchemeGenerator.generate(context, moduleUsers);
        this.issueSecurityGenerator.generate(context);
        this.metadataPluginConfigurationAccessor.executeModules(context);
        context.resetProgress("Finished.", 0);
    }
    
    private void generateData(final GeneratorContext context) throws Exception {
        this.fillPhase2Configuration(context);
        this.versionGenerator.generate(context);
        this.componentGenerator.generate(context);
        this.issueGenerator.generate(context);
        this.issueGenerator.reindex(context);
        context.resetProgress("Finished.", 0);
    }
    
    private void generateIssueHistory(final GeneratorContext context) throws Exception {
        this.populateAssignees(context);
        this.populateReporters(context);
        this.issueHistoryGeneratorDriver.generate(context);
        context.resetProgress("Finished.", 0);
    }
    
    private void generateAdditionalData(final GeneratorContext context) throws Exception {
        this.attachmentGenerator.prepare(context);
        this.additionalCommentGenerator.prepare(context);
        try {
            this.attachmentGenerator.generate();
            this.rapidViewsGenerator.generate(context);
            this.issueWatcherGenerator.generate(context);
            this.additionalCommentGenerator.generate();
            context.resetProgress("Finished.", 0);
        }
        finally {
            this.attachmentGenerator.cleanup();
        }
    }
    
    private void generateWorklogData(final GeneratorContext context) {
        this.issueWorklogGenerator.generate(context);
        context.resetProgress("Finished.", 0);
    }
    
    private void fillPhase2Configuration(final GeneratorContext context) {
        this.populateAssignees(context);
        this.populateReporters(context);
        final Map<String, String> moduleUsers = this.metadataPluginConfigurationAccessor.prepareModuleUsersForExecution();
        final Predicate<String> moduleUserFilter = (Predicate<String>)(key -> !moduleUsers.containsKey(key));
        context.assignees = Maps.filterKeys((Map)Maps.newHashMap((Map)context.assignees), (Predicate)moduleUserFilter);
        context.reporters = Maps.filterKeys((Map)Maps.newHashMap((Map)context.reporters), (Predicate)moduleUserFilter);
        if (context.createdProjects != null && context.generatorConfiguration.projectIds == null) {
            final Iterable<Long> projectIds = context.createdProjects.stream().map((Function<? super Object, ?>)Project::getId).collect((Collector<? super Object, ?, Iterable<Long>>)Collectors.toList());
            context.generatorConfiguration.projectIds = (Long[])Iterables.toArray((Iterable)projectIds, (Class)Long.class);
        }
    }
    
    private void populateAssignees(final GeneratorContext context) {
        if (context.assignees.isEmpty()) {
            final Map<String, String> developers = this.userUtils.getDevelopers();
            if (developers.isEmpty()) {
                final String displayName = this.userUtils.getDisplayName(context.userName);
                DataGeneratorDriver.log.warn(String.format("No developers found, using %s (%s)", context.userName, displayName));
                context.assignees = Collections.singletonMap(context.userName, displayName);
            }
            else {
                context.assignees = developers;
            }
        }
    }
    
    private void populateReporters(final GeneratorContext context) {
        if (context.reporters.isEmpty()) {
            final Map<String, String> users = this.userUtils.getUsers();
            context.reporters = (users.isEmpty() ? context.assignees : users);
        }
    }
    
    public boolean isCreatingFromTemplatesAvailable() {
        return this.projectGenerator.isCreatingFromTemplatesAvailable();
    }
    
    static {
        log = LoggerFactory.getLogger((Class)DataGeneratorDriver.class);
        TASK_CONTEXT = (taskId -> "/secure/admin/JiraMetadataGenerator.jspa");
    }
    
    private abstract static class GeneratorTask implements Callable<String>, ProvidesTaskProgress
    {
        TaskProgressSink taskProgressSink;
        
        static GeneratorTask withErrorHandling(final GeneratorContext context, final Generator... generators) {
            return new GeneratorTask() {
                @Override
                public String call() {
                    try {
                        for (final Generator generator : generators) {
                            generator.generate(context);
                        }
                    }
                    catch (final Exception e) {
                        context.messages.add("Error generating data, see Jira log for details.");
                        DataGeneratorDriver.log.error("Error generating data", (Throwable)e);
                    }
                    catch (final Error e2) {
                        context.messages.add("Error generating data, see Jira log for details.");
                        DataGeneratorDriver.log.error("Error generating data", (Throwable)e2);
                        throw e2;
                    }
                    finally {
                        context.finished.set(true);
                    }
                    return null;
                }
            };
        }
        
        public void setTaskProgressSink(final TaskProgressSink taskProgressSink) {
            this.taskProgressSink = taskProgressSink;
        }
    }
    
    @FunctionalInterface
    private interface Generator
    {
        void generate(final GeneratorContext p0) throws Exception;
    }
}
