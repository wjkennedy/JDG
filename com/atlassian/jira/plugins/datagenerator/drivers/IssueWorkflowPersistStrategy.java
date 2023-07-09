// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.drivers;

import org.slf4j.LoggerFactory;
import java.util.Iterator;
import com.atlassian.jira.plugins.datagenerator.db.Entity;
import java.util.Collection;
import org.ofbiz.core.entity.GenericEntityException;
import java.sql.SQLException;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import java.util.List;
import com.atlassian.jira.plugins.datagenerator.db.EntityHandler;
import com.atlassian.jira.plugins.datagenerator.db.EntityManager;

public class IssueWorkflowPersistStrategy
{
    private final EntityManager entityManager;
    private final EntityHandler issueHandler;
    private final EntityHandler workflowEntryHandler;
    private final EntityHandler currentStepHandler;
    private final EntityHandler currentStepPrevHandler;
    private final EntityHandler historyStepHandler;
    private final EntityHandler historyStepPrevHandler;
    private final EntityHandler changeGroupHandler;
    private final EntityHandler changeItemHandler;
    private final List<EntityHandler> cleanupList;
    private final EntityHandler commentHandler;
    private static final Logger LOG;
    
    public IssueWorkflowPersistStrategy(final EntityManager entityManager) throws SQLException, GenericEntityException {
        this.cleanupList = Lists.newArrayList();
        this.entityManager = entityManager;
        this.issueHandler = this.create("Issue");
        this.workflowEntryHandler = this.create("OSWorkflowEntry");
        this.currentStepHandler = this.create("OSCurrentStep");
        this.historyStepHandler = this.create("OSHistoryStep");
        this.currentStepPrevHandler = this.create("OSCurrentStepPrev");
        this.historyStepPrevHandler = this.create("OSHistoryStepPrev");
        this.changeGroupHandler = this.create("ChangeGroup");
        this.changeItemHandler = this.create("ChangeItem");
        this.commentHandler = this.create("Action");
    }
    
    public void persist(final IssueWorkflowDriver issueWorkflowDriver) throws SQLException {
        issueWorkflowDriver.workflowEntry.store(this.workflowEntryHandler);
        issueWorkflowDriver.issue.store(this.issueHandler);
        this.persistCollection(issueWorkflowDriver.historySteps, this.historyStepHandler, this.currentStepHandler);
        issueWorkflowDriver.currentStep.store(this.currentStepHandler);
        this.persistCollection(issueWorkflowDriver.currentStepPrev, this.currentStepPrevHandler);
        this.persistCollection(issueWorkflowDriver.historyStepPrev, this.historyStepPrevHandler);
        this.persistCollection(issueWorkflowDriver.changeGroups, this.changeGroupHandler);
        this.persistCollection(issueWorkflowDriver.changeItems, this.changeItemHandler);
        this.persistCollection(issueWorkflowDriver.comments, this.commentHandler);
    }
    
    private void persistCollection(final Collection<? extends Entity> entities, final EntityHandler entityHandler) throws SQLException {
        for (final Entity entity : entities) {
            entity.store(entityHandler);
        }
    }
    
    private void persistCollection(final Collection<? extends Entity> entities, final EntityHandler entityHandler, final EntityHandler idProvider) throws SQLException {
        for (final Entity entity : entities) {
            entity.store(entityHandler, idProvider);
        }
    }
    
    private EntityHandler create(final String s) throws SQLException, GenericEntityException {
        final EntityHandler entityHandler = this.entityManager.getEntityHandler(s);
        this.cleanupList.add(entityHandler);
        return entityHandler;
    }
    
    public void close() throws SQLException, GenericEntityException {
        for (final EntityHandler entityHandler : this.cleanupList) {
            entityHandler.close();
        }
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)IssueWorkflowPersistStrategy.class);
    }
}
