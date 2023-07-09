// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.config.module;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import com.atlassian.jira.issue.fields.CustomField;
import java.util.List;

public class IssueCreationParameters
{
    public static final IssueCreationParameters EMPTY;
    private final List<String> assigneeUsernames;
    private final List<String> reporterUsernames;
    private final List<CustomField> customFieldsToPopulate;
    
    private IssueCreationParameters() {
        this.assigneeUsernames = Collections.emptyList();
        this.reporterUsernames = Collections.emptyList();
        this.customFieldsToPopulate = Collections.emptyList();
    }
    
    public IssueCreationParameters(final List<String> assigneeUsernames, final List<String> reporterUsernames, final List<CustomField> customFieldsToPopulate) {
        final ImmutableList.Builder<String> assigneeUsernamesBuilder = (ImmutableList.Builder<String>)ImmutableList.builder();
        final ImmutableList.Builder<String> reporterUsernamesBuilder = (ImmutableList.Builder<String>)ImmutableList.builder();
        final ImmutableList.Builder<CustomField> customFieldsToPopulateBuilder = (ImmutableList.Builder<CustomField>)ImmutableList.builder();
        if (assigneeUsernames != null) {
            assigneeUsernamesBuilder.addAll((Iterable)assigneeUsernames);
        }
        this.assigneeUsernames = (List<String>)assigneeUsernamesBuilder.build();
        if (reporterUsernames != null) {
            reporterUsernamesBuilder.addAll((Iterable)reporterUsernames);
        }
        this.reporterUsernames = (List<String>)reporterUsernamesBuilder.build();
        if (customFieldsToPopulate != null) {
            customFieldsToPopulateBuilder.addAll((Iterable)customFieldsToPopulate);
        }
        this.customFieldsToPopulate = (List<CustomField>)customFieldsToPopulateBuilder.build();
    }
    
    public List<String> getAssigneeUsernames() {
        return this.assigneeUsernames;
    }
    
    public List<CustomField> getCustomFieldsToPopulate() {
        return this.customFieldsToPopulate;
    }
    
    public List<String> getReporterUsernames() {
        return this.reporterUsernames;
    }
    
    static {
        EMPTY = new IssueCreationParameters();
    }
}
