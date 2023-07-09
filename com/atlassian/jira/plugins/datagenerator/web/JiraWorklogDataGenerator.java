// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.web;

import java.util.Collections;
import com.google.common.collect.Sets;
import java.util.Set;
import com.atlassian.jira.web.action.ActionViewData;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.atlassian.jira.plugins.datagenerator.drivers.DataGeneratorDriver;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import com.atlassian.jira.security.request.RequestMethod;
import com.atlassian.jira.security.request.SupportedMethods;

@SupportedMethods({ RequestMethod.GET })
public class JiraWorklogDataGenerator extends StatusAwareGeneratorAction
{
    protected final GeneratorContext context;
    
    protected JiraWorklogDataGenerator(final DataGeneratorDriver dataGeneratorDriver, @ComponentImport final PageBuilderService pageBuilderService) {
        super(dataGeneratorDriver, pageBuilderService);
        this.context = new GeneratorContext();
        this.context.generatorConfiguration = new GeneratorConfiguration();
    }
    
    protected void doValidation() {
        if (this.context.generatorConfiguration.projectIds.length < 1) {
            this.addError("projectIds", "Please specify a project.");
        }
        if (this.context.generatorConfiguration.totalWorklogs < 1) {
            this.addError("worklogs", "Please specify at least a single work log to generate.");
        }
        if (this.context.generatorConfiguration.worklogsPerIssue < 1) {
            this.addError("worklogsPerIssue", "Please specify at least one issue to generate.");
        }
        if (this.context.generatorConfiguration.worklogsPerIssue != 0 && this.context.generatorConfiguration.totalWorklogs % this.context.generatorConfiguration.worklogsPerIssue != 0) {
            this.addError("worklogs", "Please specify ratio that doesn't give a reminder.");
            this.addError("worklogsPerIssue", "Please specify ratio that doesn't give a reminder.");
        }
        if (this.context.generatorConfiguration.worklogsWithGroup < 0) {
            this.addError("worklogsWithGroup", "Please specify non negative number of work logs visible to a group.");
        }
        if (this.context.generatorConfiguration.worklogsWithGroup > 0 && this.context.generatorConfiguration.uniqueGroups <= 0) {
            this.addError("worklogsWithGroup", "Please specify number of unique groups to generate.");
            this.addError("uniqueGroups", "Please specify number of unique groups to generate.");
        }
        if (this.context.generatorConfiguration.worklogsWithProjectRole < 0) {
            this.addError("worklogsWithProjectRole", "Please specify non negative number of work logs visible to a project role.");
        }
        if (this.context.generatorConfiguration.worklogsWithProjectRole > 0 && this.context.generatorConfiguration.uniqueProjectRoles <= 0) {
            this.addError("worklogsWithProjectRole", "Please specify number of unique project roles to generate.");
            this.addError("uniqueProjectRoles", "Please specify number of unique project roles to generate.");
        }
        if (this.context.generatorConfiguration.worklogsPerIssue != 0 && (this.context.generatorConfiguration.worklogsWithGroup + this.context.generatorConfiguration.worklogsWithProjectRole) % this.context.generatorConfiguration.worklogsPerIssue != 0) {
            this.addError("worklogsPerIssue", "Please specify number of work logs visible to group and role that does not give per issue ratio with a reminder.");
            this.addError("worklogsWithGroup", "Please specify number of work logs visible to group and role that does not give per issue ratio with a reminder.");
            this.addError("worklogsWithProjectRole", "Please specify number of work logs visible to group and role that does not give per issue ratio with a reminder.");
        }
        if (this.context.generatorConfiguration.worklogsWithGroup + this.context.generatorConfiguration.worklogsWithProjectRole > this.context.generatorConfiguration.totalWorklogs) {
            this.addError("worklogsWithGroup", "The combined number of work logs visible to group and visible to project role mustn't be higher then the total number of work logs to generate.");
            this.addError("worklogsWithProjectRole", "The combined number of work logs visible to group and visible to project role mustn't be higher then the total number of work logs to generate.");
        }
    }
    
    @RequiresXsrfCheck
    @SupportedMethods({ RequestMethod.POST })
    public String doExecute() throws Exception {
        if (!this.hasGlobalPermission(GlobalPermissionKey.ADMINISTER)) {
            return "permissionviolation";
        }
        this.validate();
        this.context.userName = this.getLoggedInUser().getName();
        if (!this.dataGeneratorDriver.scheduleWorklogDataGeneration(this.context)) {
            this.addErrorMessage("Cannot schedule generation task. Is there active task running? Please reopen generation form to see if there is generation in progress or wait few seconds and try again.");
            return "input";
        }
        return this.getRedirect("JiraWorklogDataGenerator.jspa");
    }
    
    @ActionViewData
    public Map<String, Object> getSoyData() {
        final ImmutableMap.Builder<String, Object> mapBuilder = (ImmutableMap.Builder<String, Object>)ImmutableMap.builder();
        return (Map<String, Object>)mapBuilder.put((Object)"errors", (Object)this.getErrorsMap()).put((Object)"projects", (Object)this.getProjects()).put((Object)"totalWorklogs", (Object)Integer.toString(this.context.generatorConfiguration.totalWorklogs)).put((Object)"worklogsPerIssue", (Object)Integer.toString(this.context.generatorConfiguration.worklogsPerIssue)).put((Object)"includeUserMentions", (Object)this.context.generatorConfiguration.includeUserMentions).put((Object)"worklogsWithGroup", (Object)Integer.toString(this.context.generatorConfiguration.worklogsWithGroup)).put((Object)"worklogsWithProjectRole", (Object)Integer.toString(this.context.generatorConfiguration.worklogsWithProjectRole)).put((Object)"uniqueProjectRoles", (Object)Integer.toString(this.context.generatorConfiguration.uniqueProjectRoles)).put((Object)"uniqueGroups", (Object)Integer.toString(this.context.generatorConfiguration.uniqueGroups)).put((Object)"projectIds", (Object)this.context.generatorConfiguration.projectIds).put((Object)"xsrfToken", (Object)this.getXsrfToken()).build();
    }
    
    public Set<Long> getProjectIdsSet() {
        return (this.context.generatorConfiguration.projectIds != null) ? Sets.newHashSet((Object[])this.context.generatorConfiguration.projectIds) : Collections.emptySet();
    }
    
    public int getWorklogsWithProjectRole() {
        return this.context.generatorConfiguration.worklogsWithProjectRole;
    }
    
    public int getWorklogsWithGroup() {
        return this.context.generatorConfiguration.worklogsWithGroup;
    }
    
    public int getWorklogsPerIssue() {
        return this.context.generatorConfiguration.worklogsPerIssue;
    }
    
    public boolean getIncludeUserMentions() {
        return this.context.generatorConfiguration.includeUserMentions;
    }
    
    public int getTotalWorklogs() {
        return this.context.generatorConfiguration.totalWorklogs;
    }
    
    public int getUniqueProjectRoles() {
        return this.context.generatorConfiguration.uniqueProjectRoles;
    }
    
    public int getUniqueGroups() {
        return this.context.generatorConfiguration.uniqueGroups;
    }
    
    public void setWorklogsWithGroup(final int worklogsWithUniqueGroup) {
        this.context.generatorConfiguration.worklogsWithGroup = worklogsWithUniqueGroup;
    }
    
    public void setWorklogsWithProjectRole(final int worklogsWithUniqueProjectRole) {
        this.context.generatorConfiguration.worklogsWithProjectRole = worklogsWithUniqueProjectRole;
    }
    
    public void setWorklogsPerIssue(final int worklogsPerIssue) {
        this.context.generatorConfiguration.worklogsPerIssue = worklogsPerIssue;
    }
    
    public void setIncludeUserMentions(final boolean includeUserMentions) {
        this.context.generatorConfiguration.includeUserMentions = includeUserMentions;
    }
    
    public void setTotalWorklogs(final int totalWorklogs) {
        this.context.generatorConfiguration.totalWorklogs = totalWorklogs;
    }
    
    public void setUniqueProjectRoles(final int uniqueProjectRoles) {
        this.context.generatorConfiguration.uniqueProjectRoles = uniqueProjectRoles;
    }
    
    public void setUniqueGroups(final int uniqueGroups) {
        this.context.generatorConfiguration.uniqueGroups = uniqueGroups;
    }
    
    public void setProjectIds(final Long[] projectIds) {
        this.context.generatorConfiguration.projectIds = projectIds;
    }
}
