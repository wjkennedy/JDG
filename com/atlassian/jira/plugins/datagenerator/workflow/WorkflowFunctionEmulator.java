// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.workflow;

import com.atlassian.jira.issue.history.ChangeItemBean;
import java.sql.Timestamp;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.atlassian.jira.plugins.datagenerator.db.Entity;

public interface WorkflowFunctionEmulator
{
    String className();
    
    ChangeItemBean emulate(final Entity p0, final FunctionDescriptor p1, final Timestamp p2);
}
