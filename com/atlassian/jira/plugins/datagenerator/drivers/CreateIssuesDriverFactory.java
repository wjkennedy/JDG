// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.drivers;

import org.ofbiz.core.entity.GenericEntityException;
import java.sql.SQLException;
import com.atlassian.jira.plugins.datagenerator.db.EntityManager;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.plugins.datagenerator.db.FieldDetector;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.plugins.datagenerator.workflow.WorkflowPostFunctionEmulator;
import com.atlassian.jira.plugins.datagenerator.fields.FieldMutators;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.plugins.datagenerator.ProjectUpdater;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.workflow.WorkflowManager;
import org.springframework.stereotype.Component;

@Component
public class CreateIssuesDriverFactory
{
    private final WorkflowManager workflowManager;
    private final ProjectManager projectManager;
    private final ConstantsManager constantsManager;
    private final ProjectUpdater projectUpdater;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final FieldMutators fieldMutators;
    private final WorkflowPostFunctionEmulator workflowPostFunctionEmulator;
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final FieldDetector fieldDetector;
    private final IssueSecuritySchemeManager issueSecuritySchemeManager;
    private final IssueSecurityLevelManager issueSecurityLevelManager;
    
    @Autowired
    public CreateIssuesDriverFactory(@ComponentImport final WorkflowManager workflowManager, @ComponentImport final ProjectManager projectManager, @ComponentImport final ConstantsManager constantsManager, final ProjectUpdater projectUpdater, @ComponentImport final IssueTypeSchemeManager issueTypeSchemeManager, final FieldMutators fieldMutators, final WorkflowPostFunctionEmulator workflowPostFunctionEmulator, @ComponentImport final IssueLinkTypeManager issueLinkTypeManager, final FieldDetector fieldDetector, @ComponentImport final IssueSecuritySchemeManager issueSecuritySchemeManager, @ComponentImport final IssueSecurityLevelManager issueSecurityLevelManager) {
        this.workflowManager = workflowManager;
        this.projectManager = projectManager;
        this.constantsManager = constantsManager;
        this.projectUpdater = projectUpdater;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.fieldMutators = fieldMutators;
        this.workflowPostFunctionEmulator = workflowPostFunctionEmulator;
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.fieldDetector = fieldDetector;
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
        this.issueSecurityLevelManager = issueSecurityLevelManager;
    }
    
    public CreateIssuesDriver create(final GeneratorContext context, final EntityManager entityManager) throws SQLException, GenericEntityException {
        return new CreateIssuesDriver(context, entityManager, this.workflowManager, this.projectManager, this.constantsManager, this.issueTypeSchemeManager, this.projectUpdater, this.fieldMutators, this.workflowPostFunctionEmulator, this.issueLinkTypeManager, this.fieldDetector, this.issueSecuritySchemeManager, this.issueSecurityLevelManager);
    }
}
