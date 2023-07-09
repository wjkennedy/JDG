// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.workflow;

import org.slf4j.LoggerFactory;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import com.atlassian.jira.issue.history.ChangeItemBean;
import java.sql.Timestamp;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.atlassian.jira.plugins.datagenerator.db.Entity;
import com.atlassian.jira.workflow.function.issue.UpdateIssueFieldFunction;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.jira.plugins.datagenerator.fields.ResolutionMutator;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class UpdateIssueFieldFunctionEmulator implements WorkflowFunctionEmulator
{
    private static final Logger log;
    private final ResolutionMutator resolutionMutator;
    
    @Autowired
    public UpdateIssueFieldFunctionEmulator(final ResolutionMutator resolutionMutator) {
        this.resolutionMutator = resolutionMutator;
    }
    
    @Override
    public String className() {
        return UpdateIssueFieldFunction.class.getName();
    }
    
    @Override
    public ChangeItemBean emulate(final Entity issue, final FunctionDescriptor function, final Timestamp currentNow) {
        final Map args = function.getArgs();
        final String fieldName = args.get("field.name");
        final String fieldValue = StringUtils.trimToNull((String)args.get("field.value"));
        if (!"resolution".equals(fieldName)) {
            UpdateIssueFieldFunctionEmulator.log.error("Unsupported field for UpdateIssueFieldFunction: " + fieldName);
            return null;
        }
        return StringUtils.equals((String)issue.get("resolution"), fieldValue) ? null : this.resolutionMutator.updateResolution(issue, fieldValue, currentNow);
    }
    
    static {
        log = LoggerFactory.getLogger((Class)UpdateIssueFieldFunctionEmulator.class);
    }
}
