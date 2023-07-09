// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator;

import javax.annotation.Nullable;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.issue.context.IssueContext;

public class IssueInfo implements IssueContext
{
    public final Project project;
    public final IssueType issueType;
    public final Long id;
    public final long created;
    public final String assignee;
    public final String reporter;
    
    public IssueInfo(final Project project, final Long id, final String assignee, final String reporter, final IssueType issueType, final long created) {
        this.project = project;
        this.id = id;
        this.issueType = issueType;
        this.created = created;
        this.assignee = assignee;
        this.reporter = reporter;
    }
    
    public Project getProjectObject() {
        return this.project;
    }
    
    public GenericValue getProject() {
        return this.project.getGenericValue();
    }
    
    public Long getProjectId() {
        return this.project.getId();
    }
    
    public IssueType getIssueTypeObject() {
        return this.issueType;
    }
    
    @Nullable
    public IssueType getIssueType() {
        return this.issueType;
    }
    
    public String getIssueTypeId() {
        return this.issueType.getId();
    }
    
    public String getAssignee() {
        return this.assignee;
    }
    
    public long getCreated() {
        return this.created;
    }
    
    public Long getId() {
        return this.id;
    }
    
    public String getReporter() {
        return this.reporter;
    }
}
