// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators;

import org.slf4j.LoggerFactory;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.google.common.collect.Iterables;
import com.atlassian.jira.scheme.Scheme;
import org.ofbiz.core.entity.GenericValue;
import java.util.Iterator;
import org.ofbiz.core.entity.GenericEntityException;
import com.atlassian.jira.project.Project;
import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import org.xml.sax.SAXException;
import java.io.IOException;
import org.jdom.JDOMException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import java.util.Collection;
import com.google.common.collect.ImmutableList;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.opensymphony.workflow.loader.WorkflowLoader;
import org.apache.commons.io.IOUtils;
import com.google.common.collect.Lists;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import com.atlassian.jira.plugins.datagenerator.StatusBean;
import java.util.List;
import org.apache.commons.lang.time.StopWatch;
import java.util.Date;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.plugins.datagenerator.workflow.RandomWorkflowGenerator;
import com.atlassian.jira.config.ConstantsManager;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class WorkflowGenerator
{
    private static final Logger LOG;
    private final ConstantsManager constantsManager;
    private final RandomWorkflowGenerator randomWorkflowGenerator;
    private final WorkflowManager workflowManager;
    private final WorkflowSchemeManager workflowSchemeManager;
    private final ProjectManager projectManager;
    
    @Autowired
    public WorkflowGenerator(@ComponentImport final WorkflowSchemeManager workflowSchemeManager, @ComponentImport final WorkflowManager workflowManager, final RandomWorkflowGenerator randomWorkflowGenerator, @ComponentImport final ConstantsManager constantsManager, @ComponentImport final ProjectManager projectManager) {
        this.workflowSchemeManager = workflowSchemeManager;
        this.workflowManager = workflowManager;
        this.randomWorkflowGenerator = randomWorkflowGenerator;
        this.constantsManager = constantsManager;
        this.projectManager = projectManager;
    }
    
    public void generate(final GeneratorContext context) {
        if (context.generatorConfiguration.workflows.count > 0) {
            try {
                final long timestamp = new Date().getTime();
                final StopWatch stopWatch = new StopWatch();
                stopWatch.reset();
                stopWatch.start();
                final List<String> jiraWorkflowsNames = this.generateWorkflows(context, timestamp);
                stopWatch.stop();
                context.messages.add(String.format("Generated %d random workflows in %s", jiraWorkflowsNames.size(), stopWatch.toString()));
                stopWatch.reset();
                stopWatch.start();
                this.assignSchemeToProjects(context, jiraWorkflowsNames);
                stopWatch.stop();
                context.messages.add(String.format("Assigned %d random workflows in %s", jiraWorkflowsNames.size(), stopWatch.toString()));
            }
            catch (final Exception e) {
                WorkflowGenerator.LOG.error("Error generating workflow scheme", (Throwable)e);
                context.messages.add("Error generating workflow scheme, see log for details");
            }
        }
        else {
            context.messages.add("No workflows generation requested, skipping.");
        }
    }
    
    private List<String> generateWorkflows(final GeneratorContext context, final long timestamp) throws JDOMException, IOException, SAXException, InvalidWorkflowDescriptorException {
        final List<StatusBean> statuses = this.constantsManager.getStatuses().stream().map(status -> new StatusBean(status.getName(), status.getId())).collect((Collector<? super Object, ?, List<StatusBean>>)Collectors.toList());
        final int workflowCount = context.generatorConfiguration.workflows.count;
        final List<String> generatedWorkflowsNames = Lists.newArrayListWithCapacity(workflowCount);
        final String msg = String.format("Generating %d random workflows", workflowCount);
        WorkflowGenerator.LOG.info(msg);
        context.currentTask.set("Loading workflows");
        context.resetProgress(msg, workflowCount);
        for (int i = 1; i <= workflowCount; ++i) {
            final String workflowXml = this.randomWorkflowGenerator.generate(statuses);
            final String workflowName = "jdg-workflow-" + i + "-" + timestamp;
            final WorkflowDescriptor workflowDescriptor = WorkflowLoader.load(IOUtils.toInputStream(workflowXml), true);
            final ConfigurableJiraWorkflow newWorkflow = new ConfigurableJiraWorkflow(workflowName, workflowDescriptor, this.workflowManager);
            newWorkflow.setDescription("");
            this.workflowManager.createWorkflow(context.userName, (JiraWorkflow)newWorkflow);
            generatedWorkflowsNames.add(newWorkflow.getName());
            context.incProgress();
        }
        return (List<String>)ImmutableList.copyOf((Collection)generatedWorkflowsNames);
    }
    
    private void assignSchemeToProjects(final GeneratorContext context, final List<String> jiraWorkflowsNames) {
        try {
            final String msg = String.format("Assigning schemes to generated projects (%d in total)", context.createdProjects.size());
            WorkflowGenerator.LOG.info(msg);
            context.resetProgress(msg, context.createdProjects.size());
            for (final Project projectObj : context.createdProjects) {
                final GenericValue projectGV = projectObj.getGenericValue();
                final Scheme assignedScheme = this.workflowSchemeManager.getSchemeFor(projectObj);
                if (assignedScheme != null || this.projectManager.getCurrentCounterForProject(projectObj.getId()) != 0L) {
                    this.workflowSchemeManager.removeSchemesFromProject(projectObj);
                    this.workflowSchemeManager.deleteScheme(assignedScheme.getId());
                }
                final GenericValue schemeGV = this.generateSchemaForProject(projectObj.getName(), jiraWorkflowsNames);
                this.workflowSchemeManager.addSchemeToProject(projectGV, schemeGV);
                context.incProgress();
            }
        }
        catch (final GenericEntityException e) {
            throw new RuntimeException("Error creating workflow scheme for projects selected from data generator", (Throwable)e);
        }
    }
    
    private GenericValue generateSchemaForProject(final String projectName, final List<String> jiraWorkflowsNames) throws GenericEntityException {
        final String workflowSchemeName = "jdg-schema-for-project-" + projectName.trim().toLowerCase();
        final GenericValue schemeGV = this.workflowSchemeManager.createScheme(workflowSchemeName, "Autogenerated");
        final Collection<IssueType> issueTypes = this.constantsManager.getAllIssueTypeObjects();
        final Iterator<String> workflowsToCycle = Iterables.cycle((Iterable)jiraWorkflowsNames).iterator();
        for (final IssueType issueType : issueTypes) {
            this.workflowSchemeManager.addWorkflowToScheme(schemeGV, (String)workflowsToCycle.next(), issueType.getId());
        }
        return schemeGV;
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)ProjectGenerator.class);
    }
}
