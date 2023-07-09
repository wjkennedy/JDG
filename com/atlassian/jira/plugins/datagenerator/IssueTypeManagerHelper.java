// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator;

import java.util.Iterator;
import java.util.Collection;
import com.atlassian.jira.issue.issuetype.IssueType;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.IssueTypeManager;
import org.springframework.stereotype.Component;

@Component
public class IssueTypeManagerHelper
{
    private final IssueTypeManager issueTypeManager;
    private final SubTaskManager subTaskManager;
    
    @Autowired
    public IssueTypeManagerHelper(@ComponentImport final IssueTypeManager issueTypeManager, @ComponentImport final SubTaskManager subTaskManager) {
        this.issueTypeManager = issueTypeManager;
        this.subTaskManager = subTaskManager;
    }
    
    public IssueType createIssueType(final String name, final String description, final String iconUrl) {
        return this.issueTypeManager.createIssueType(name, description, iconUrl);
    }
    
    public Collection<IssueType> getIssueTypes() {
        return this.issueTypeManager.getIssueTypes();
    }
    
    public long getNextSubtaskSequenceNumber() {
        final Collection<IssueType> subtaskTypes = this.subTaskManager.getSubTaskIssueTypeObjects();
        long maxSequence = subtaskTypes.size();
        for (final IssueType subtaskType : subtaskTypes) {
            final Long sequence = subtaskType.getSequence();
            maxSequence = ((sequence != null && maxSequence < sequence) ? sequence : maxSequence);
        }
        return maxSequence + 1L;
    }
}
