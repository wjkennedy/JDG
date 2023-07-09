// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.config.module;

import com.atlassian.jira.project.Project;
import java.util.Collection;
import com.atlassian.annotations.PublicSpi;

@PublicSpi
public interface DataPluginConfiguration
{
    String getPluginKey();
    
    Collection<Project> getProjects();
    
    IssueCreationParameters getIssueCreationParameters(final Project p0);
    
    void performIssueUpdates(final Project p0, final Collection<Long> p1);
    
    void performPostReindexUpdates(final Project p0);
    
    CommentParameters getCommentAuthorAndProperties(final Project p0, final Long p1, final String p2, final String p3);
}
