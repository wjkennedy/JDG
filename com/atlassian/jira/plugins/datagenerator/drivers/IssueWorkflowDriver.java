// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.drivers;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.google.common.collect.Sets;
import java.util.Collections;
import com.atlassian.jira.workflow.WorkflowActionsBean;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.atlassian.jira.issue.status.Status;
import com.opensymphony.workflow.loader.StepDescriptor;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.opensymphony.workflow.loader.ResultDescriptor;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import java.util.Collection;
import com.atlassian.jira.plugins.datagenerator.db.ChangeItemEntity;
import com.atlassian.jira.plugins.datagenerator.db.ChangeGroupEntity;
import com.google.common.collect.Iterables;
import com.atlassian.jira.plugins.datagenerator.util.Randomizer;
import com.atlassian.jira.plugins.datagenerator.config.module.IssueCreationParameters;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.atlassian.jira.plugins.datagenerator.config.Transitions;
import com.atlassian.jira.plugins.datagenerator.text.DictionaryTextGenerator;
import com.atlassian.jira.plugins.datagenerator.timestamp.FixedIntervalEndlessGenerator;
import java.sql.Timestamp;
import com.google.common.collect.Lists;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import com.atlassian.jira.plugins.datagenerator.workflow.WorkflowPostFunctionEmulator;
import com.atlassian.jira.plugins.datagenerator.fields.FieldMutators;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.plugins.datagenerator.text.TextGenerator;
import com.atlassian.jira.plugins.datagenerator.timestamp.TimestampGenerator;
import com.atlassian.jira.plugins.datagenerator.db.CommentEntity;
import java.util.List;
import com.atlassian.jira.plugins.datagenerator.db.Entity;

public class IssueWorkflowDriver
{
    public final Entity issue;
    public Entity currentStep;
    private int currentStepId;
    public final List<Entity> currentStepPrev;
    public final List<Entity> historySteps;
    public final List<Entity> historyStepPrev;
    public final List<CommentEntity> comments;
    public final Entity workflowEntry;
    public final List<Entity> changeGroups;
    public final List<Entity> changeItems;
    private final List<String> potentialAssigneesNames;
    private final TimestampGenerator transitionTicker;
    private final TextGenerator commentGenerator;
    private final JiraWorkflow workflow;
    private final ConstantsManager constantsManager;
    private final FieldMutators fieldMutators;
    private final WorkflowPostFunctionEmulator workflowPostFunctionEmulator;
    
    public IssueWorkflowDriver(final GeneratorContext context, final ConstantsManager constantsManager, final FieldMutators fieldMutators, final WorkflowPostFunctionEmulator workflowPostFunctionEmulator, final JiraWorkflow jiraWorkflow, final Entity issue, final List<String> potentialAssigneesNames) {
        this.currentStepPrev = Lists.newArrayList();
        this.historySteps = Lists.newArrayList();
        this.historyStepPrev = Lists.newArrayList();
        this.comments = Lists.newArrayList();
        this.workflowEntry = new Entity();
        this.changeGroups = Lists.newArrayList();
        this.changeItems = Lists.newArrayList();
        this.constantsManager = constantsManager;
        this.fieldMutators = fieldMutators;
        this.workflowPostFunctionEmulator = workflowPostFunctionEmulator;
        this.workflow = jiraWorkflow;
        this.issue = issue;
        final Timestamp created = (Timestamp)issue.get("created");
        final Transitions transitionCfg = context.generatorConfiguration.transitions;
        this.transitionTicker = new FixedIntervalEndlessGenerator(created.getTime(), transitionCfg.avgInterval);
        this.commentGenerator = new DictionaryTextGenerator(transitionCfg.commentProbability, transitionCfg.commentMinLength, transitionCfg.commentMaxLength);
        this.potentialAssigneesNames = potentialAssigneesNames;
    }
    
    public void initialAction() {
        this.workflowEntry.put("name", this.workflow.getName()).put("state", 1);
        this.issue.setRef("workflowId", this.workflowEntry);
        final ActionDescriptor initialActionDescriptor = this.workflow.getDescriptor().getInitialActions().get(0);
        this.transition(this.transitionTicker.next(), initialActionDescriptor);
    }
    
    public void randomAction(final IssueCreationParameters issueCreationParameters) {
        final Timestamp currentNow = this.transitionTicker.next();
        final String actor = this.pickActor(issueCreationParameters);
        final List<ActionDescriptor> availableActions = this.getAvailableActions();
        final ActionDescriptor action = Randomizer.randomItem(availableActions);
        if (action != null) {
            final ResultDescriptor result = action.getUnconditionalResult();
            final Entity historyStep = this.currentStep;
            historyStep.put("finishDate", currentNow);
            historyStep.put("caller", actor);
            historyStep.put("status", result.getOldStatus());
            historyStep.put("actionId", action.getId());
            if (!this.historySteps.isEmpty()) {
                final Entity preHist = (Entity)Iterables.getLast((Iterable)this.historySteps);
                this.historyStepPrev.add(new Entity().setRef("previousId", preHist).setRef("id", historyStep));
            }
            this.historySteps.add(historyStep);
            final ChangeItemBean statusChangeItemBean = this.prepareStatusChangeItemBean(this.issue);
            this.transition(currentNow, action);
            this.updateStatusChangeItemBean(statusChangeItemBean, this.issue);
            this.issue.put("updated", currentNow);
            this.currentStepPrev.add(new Entity().setRef("previousId", historyStep).setRef("id", this.currentStep));
            final ChangeGroupEntity changeGroupEntity = new ChangeGroupEntity().setAuthor(actor).setCreated(currentNow).setIssue(this.issue);
            this.changeGroups.add(changeGroupEntity);
            this.changeItems.add(new ChangeItemEntity(changeGroupEntity, statusChangeItemBean));
            this.mutateFields(action, changeGroupEntity, currentNow);
            final List<FunctionDescriptor> postFunctions = result.getPostFunctions();
            final Collection<ChangeItemBean> functionResults = this.workflowPostFunctionEmulator.emulatePostFunctions(this.issue, postFunctions, currentNow);
            final List<ChangeItemEntity> changeItemEntities = functionResults.stream().map(input -> new ChangeItemEntity(changeGroupEntity, input)).collect((Collector<? super Object, ?, List<ChangeItemEntity>>)Collectors.toList());
            this.changeItems.addAll(changeItemEntities);
            if (this.workflowPostFunctionEmulator.processesComments(postFunctions)) {
                this.addComment(actor, currentNow);
            }
        }
    }
    
    private void addComment(final String actor, final Timestamp timestamp) {
        final String comment = this.commentGenerator.generateText();
        if (comment != null) {
            this.comments.add(new CommentEntity(this.issue, actor, comment, timestamp));
        }
    }
    
    private String pickActor(final IssueCreationParameters issueCreationParameters) {
        final String currentAssignee = (String)this.issue.get("assignee");
        return StringUtils.isNotBlank(currentAssignee) ? currentAssignee : Randomizer.randomItem(this.potentialAssigneesNames);
    }
    
    private void mutateFields(final ActionDescriptor action, final ChangeGroupEntity changeGroupEntity, final Timestamp currentNow) {
        final Set<String> visibleFields = this.getVisibleFields(action);
        for (final String field : visibleFields) {
            final ChangeItemBean cib = this.fieldMutators.handle(field, this.issue, currentNow);
            if (cib != null) {
                this.changeItems.add(new ChangeItemEntity(changeGroupEntity, cib));
            }
        }
    }
    
    private ChangeItemBean prepareStatusChangeItemBean(final Entity issue) {
        final ChangeItemBean cib = new ChangeItemBean();
        cib.setFieldType("jira");
        cib.setField("status");
        final String statusId = (String)issue.get("status");
        cib.setFrom(statusId);
        cib.setFromString(this.constantsManager.getStatusObject(statusId).getName());
        return cib;
    }
    
    private void updateStatusChangeItemBean(final ChangeItemBean cib, final Entity issue) {
        final String newStatus = (String)issue.get("status");
        cib.setTo(newStatus);
        cib.setToString(this.constantsManager.getStatusObject(newStatus).getName());
    }
    
    public void transition(final Timestamp currentTime, final ActionDescriptor action) {
        final ResultDescriptor result = action.getUnconditionalResult();
        this.currentStep = this.prepareStep(currentTime, 0, result);
        this.currentStepId = result.getStep();
        final StepDescriptor step = this.workflow.getDescriptor().getStep(this.currentStepId);
        final Status status = this.workflow.getLinkedStatusObject(step);
        this.issue.put("status", status.getId());
    }
    
    public List<ActionDescriptor> getAvailableActions() {
        final WorkflowDescriptor descriptor = this.workflow.getDescriptor();
        final List<ActionDescriptor> actions = Lists.newArrayList();
        final StepDescriptor stepDescriptor = descriptor.getStep(this.currentStepId);
        actions.addAll(stepDescriptor.getActions());
        return actions;
    }
    
    private Set<String> getVisibleFields(final ActionDescriptor actionDescriptor) {
        final WorkflowActionsBean wab = new WorkflowActionsBean();
        final FieldScreen fieldScreen = wab.getFieldScreenForView(actionDescriptor);
        if (fieldScreen == null) {
            return Collections.emptySet();
        }
        final Set<String> visibleFields = Sets.newHashSet();
        for (final Object fieldScreenTab : fieldScreen.getTabs()) {
            final List<FieldScreenLayoutItem> fieldScreenLayoutItems = ((FieldScreenTab)fieldScreenTab).getFieldScreenLayoutItems();
            for (final FieldScreenLayoutItem fieldScreenLayoutItem : fieldScreenLayoutItems) {
                visibleFields.add(fieldScreenLayoutItem.getFieldId());
            }
        }
        return visibleFields;
    }
    
    private Entity prepareStep(final Timestamp currentNow, final int actionId, final ResultDescriptor result) {
        final Entity step = new Entity();
        step.setRef("entryId", this.workflowEntry);
        step.put("actionId", actionId);
        step.put("stepId", result.getStep());
        step.put("owner", result.getOwner());
        step.put("startDate", currentNow);
        step.put("status", result.getStatus());
        return step;
    }
}
