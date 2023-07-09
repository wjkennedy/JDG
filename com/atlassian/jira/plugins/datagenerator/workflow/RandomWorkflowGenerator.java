// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.workflow;

import java.util.ArrayList;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;
import org.jdom.Content;
import org.jdom.DocType;
import org.jdom.Element;
import com.atlassian.jira.plugins.datagenerator.StatusBean;
import java.util.List;
import java.io.IOException;
import org.jdom.JDOMException;
import com.atlassian.jira.plugins.datagenerator.generators.WorkflowGenerator;
import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import java.util.Random;
import org.springframework.stereotype.Component;

@Component
public class RandomWorkflowGenerator
{
    private static final int MIN_STATUSES_NUMBER = 5;
    private static final int MAX_STATUSES_NUMBER = 20;
    private Random random;
    private final Document postFunctionsDocument;
    private final Document initialActionsDocument;
    
    public RandomWorkflowGenerator() throws JDOMException, IOException {
        this.initialActionsDocument = new SAXBuilder(false).build(WorkflowGenerator.class.getResourceAsStream("initial-action.xml"));
        this.postFunctionsDocument = new SAXBuilder(false).build(WorkflowGenerator.class.getResourceAsStream("post-functions.xml"));
        this.random = new Random();
    }
    
    public String generate(final List<StatusBean> statuses) throws JDOMException, IOException {
        final Element workflow = new Element("workflow");
        final Document doc = new Document(workflow, new DocType("workflow", "-//OpenSymphony Group//DTD OSWorkflow 2.8//EN", "http://www.opensymphony.com/osworkflow/workflow_2_8.dtd"));
        workflow.addContent(this.getInitialActions());
        final Element steps = this.createSteps(this.getRandomStatuses(statuses));
        workflow.addContent((Content)steps);
        return new XMLOutputter(Format.getRawFormat()).outputString(doc);
    }
    
    protected Element createSteps(final List<StatusBean> statuses) {
        final Element steps = new Element("steps");
        steps.addContent((Content)this.createFirstStep(statuses.get(0)));
        for (int i = 0; i < statuses.size() - 1; ++i) {
            steps.addContent((Content)this.createMiddleStep(statuses, i));
        }
        steps.addContent((Content)this.createLastStep(statuses.get(statuses.size() - 1)));
        return steps;
    }
    
    private Element createFirstStep(final StatusBean nextStatus) {
        final Element firstStep = this.createStep("1", "Open");
        final Element firstStepActions = new Element("actions");
        firstStepActions.addContent((Content)this.createAction(nextStatus));
        firstStep.addContent((Content)firstStepActions);
        return firstStep;
    }
    
    private Element createMiddleStep(final List<StatusBean> availableStatuses, final int currentStatusIndex) {
        final StatusBean currentStatus = availableStatuses.get(currentStatusIndex);
        final StatusBean nextStatus = availableStatuses.get(currentStatusIndex + 1);
        final Element actions = new Element("actions");
        actions.addContent((Content)this.createAction(nextStatus));
        this.addRandomActions(availableStatuses, currentStatus, actions);
        final Element step = this.createStep(currentStatus.id, currentStatus.name);
        step.addContent((Content)actions);
        return step;
    }
    
    private Element createLastStep(final StatusBean lastStatus) {
        return this.createStep(lastStatus.id, lastStatus.name);
    }
    
    private Element createStep(final String id, final String name) {
        final Element step = new Element("step");
        step.setAttribute("id", id);
        step.setAttribute("name", name);
        final Element meta = new Element("meta");
        meta.setAttribute("name", "jira.status.id");
        meta.addContent(id);
        step.addContent((Content)meta);
        return step;
    }
    
    private void addRandomActions(final List<StatusBean> availableStatuses, final StatusBean currentStatus, final Element actions) {
        for (int randomActions = this.random.nextInt(2), j = 0; j < randomActions; ++j) {
            final StatusBean randomStatus = availableStatuses.get(this.random.nextInt(availableStatuses.size()));
            actions.addContent((Content)this.createAction(randomStatus, this.getCommonActionId(currentStatus) + j));
        }
    }
    
    private List<StatusBean> getRandomStatuses(final List<StatusBean> statuses) {
        int statusesNumber = Math.min(this.random.nextInt(16) + 5, statuses.size());
        final List<StatusBean> randomStatuses = new ArrayList<StatusBean>(statusesNumber);
        while (statusesNumber > 0) {
            final StatusBean status = statuses.get(this.random.nextInt(statuses.size()));
            if (!randomStatuses.contains(status)) {
                randomStatuses.add(status);
                --statusesNumber;
            }
        }
        return randomStatuses;
    }
    
    protected Element createResults(final StatusBean status) {
        final Element result = new Element("unconditional-result");
        result.setAttribute("old-status", "whatever");
        result.setAttribute("status", status.name);
        result.setAttribute("step", status.id);
        result.addContent(this.getPostFunctions());
        return result;
    }
    
    protected Element createAction(final StatusBean status) {
        return this.createAction(status, this.getCommonActionId(status));
    }
    
    protected Element createAction(final StatusBean status, final String id) {
        final Element action = new Element("action");
        action.setAttribute("id", id);
        action.setAttribute("name", "Go to " + status.name);
        final Element descriptionMeta = new Element("meta");
        descriptionMeta.setAttribute("name", "jira.description");
        final Element fieldScreenMeta = new Element("meta");
        fieldScreenMeta.setAttribute("name", "jira.fieldscreen.id");
        action.addContent((Content)descriptionMeta);
        action.addContent((Content)fieldScreenMeta);
        final Element results = new Element("results");
        final Element commonActionUnconditionalResults = this.createResults(status);
        results.addContent((Content)commonActionUnconditionalResults);
        action.addContent((Content)results);
        return action;
    }
    
    private Content getInitialActions() {
        final Document initialActions = (Document)this.initialActionsDocument.clone();
        return initialActions.getRootElement().detach();
    }
    
    private Content getPostFunctions() {
        final Document clonedPostFunctions = (Document)this.postFunctionsDocument.clone();
        return clonedPostFunctions.getRootElement().detach();
    }
    
    private String getCommonActionId(final StatusBean status) {
        return status.id + "00";
    }
}
