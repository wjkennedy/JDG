// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.fields;

import com.atlassian.jira.plugins.datagenerator.config.module.IssueCreationParameters;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import javax.annotation.Nullable;
import com.atlassian.jira.issue.history.ChangeItemBean;
import java.sql.Timestamp;
import com.atlassian.jira.plugins.datagenerator.db.Entity;
import java.util.Map;

public interface FieldMutator
{
    String fieldType();
    
    void handle(final Map<Object, Object> p0);
    
    @Nullable
    ChangeItemBean handle(final Entity p0, final Timestamp p1);
    
    void init(final GeneratorContext p0, final IssueCreationParameters p1);
}
