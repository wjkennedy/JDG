// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.db;

import java.sql.Timestamp;

public class CommentEntity extends Entity
{
    public CommentEntity(final Entity issue, final String author, final String commentText, final Timestamp timestamp) {
        this.setRef("issue", issue);
        this.put("author", author);
        this.put("updateauthor", author);
        this.put("type", "comment");
        this.put("body", commentText);
        this.setTimestamp(timestamp);
    }
    
    public void setTimestamp(final Timestamp timestamp) {
        this.put("created", timestamp);
        this.put("updated", timestamp);
    }
}
