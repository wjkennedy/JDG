// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.db;

import java.sql.Timestamp;

public class ChangeGroupEntity extends Entity
{
    public ChangeGroupEntity setAuthor(final String author) {
        this.put("author", author);
        return this;
    }
    
    public ChangeGroupEntity setIssue(final Entity issue) {
        this.setRef("issue", issue);
        return this;
    }
    
    public ChangeGroupEntity setIssueId(final Long issueId) {
        this.put("issue", issueId);
        return this;
    }
    
    public ChangeGroupEntity setCreated(final Timestamp created) {
        this.put("created", created);
        return this;
    }
}
