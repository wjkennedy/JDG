// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.web;

import com.google.common.collect.ImmutableList;
import java.util.List;
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
public class JiraDataGenerator extends StatusAwareGeneratorAction
{
    protected final GeneratorContext context;
    
    public JiraDataGenerator(final DataGeneratorDriver dataGeneratorDriver, @ComponentImport final PageBuilderService pageBuilderService) {
        super(dataGeneratorDriver, pageBuilderService);
        this.context = new GeneratorContext();
        this.context.generatorConfiguration = new GeneratorConfiguration();
        if (this.getLoggedInUser() != null) {
            this.context.userName = this.getLoggedInUser().getName();
        }
    }
    
    protected void doValidation() {
        if (this.context.generatorConfiguration.issues.count > 0) {
            if (this.context.generatorConfiguration.issues.commentCount < 0.0) {
                this.addError("issueCommentCount", "Please specify a non-negative integer for number of comments to generate for each issue.");
            }
            if (this.context.generatorConfiguration.period <= 0) {
                this.addError("period", "Please specify a positive integer for data age in days.");
            }
            if (this.context.generatorConfiguration.projectIds == null || this.context.generatorConfiguration.projectIds.length == 0) {
                this.addError("projectIds", "Please select at least one project.");
            }
            if (this.context.generatorConfiguration.transitions.avgCount <= 0) {
                this.addError("averageTransitionCount", "Please specify a positive integer for transition count.");
            }
            if (this.context.generatorConfiguration.transitions.reassignProbability < 0.0f) {
                this.addError("reassignProbability", "Please specify a positive float for reassign issues");
            }
            else if (this.context.generatorConfiguration.transitions.reassignProbability > 1.0f) {
                this.addError("reassignProbability", "Please specify a positive float not greater than 1.0 for reassign issues");
            }
            if (this.context.generatorConfiguration.issues.subTasksProbability < 0.0f) {
                this.addError("subTasksProbability", "Please specify a positive float for sub-tasks generation probability");
            }
            else if (this.context.generatorConfiguration.issues.subTasksProbability > 1.0f) {
                this.addError("subTasksProbability", "Please specify a positive float not greater than 1.0 for sub-tasks generation probability");
            }
            if (this.context.generatorConfiguration.issues.issueLinkProbability < 0.0f) {
                this.addError("issueLinkProbability", "Please specify a positive float for issue link generation probability");
            }
            else if (this.context.generatorConfiguration.issues.issueLinkProbability > 1.0f) {
                this.addError("issueLinkProbability", "Please specify a positive float not greater than 1.0 for issue link generation probability");
            }
        }
    }
    
    @RequiresXsrfCheck
    @SupportedMethods({ RequestMethod.POST })
    public String doExecute() throws Exception {
        if (!this.hasGlobalPermission(GlobalPermissionKey.SYSTEM_ADMIN)) {
            return "permissionviolation";
        }
        this.validate();
        this.context.userName = this.getLoggedInUser().getName();
        if (!this.dataGeneratorDriver.scheduleDataGeneration(this.context)) {
            this.addErrorMessage("Cannot schedule generation task. Is there active task running? Please reopen generation form to see if there is generation in progress or wait few seconds and try again.");
            return "input";
        }
        return this.getRedirect("JiraDataGenerator.jspa");
    }
    
    @ActionViewData
    public Map<String, Object> getSoyData() {
        final ImmutableMap.Builder<String, Object> mapBuilder = (ImmutableMap.Builder<String, Object>)ImmutableMap.builder();
        return (Map<String, Object>)mapBuilder.put((Object)"projects", (Object)this.getProjects()).put((Object)"projectIds", (Object)this.context.generatorConfiguration.projectIds).put((Object)"versionsCount", (Object)Integer.toString(this.getVersionsCount())).put((Object)"componentsCount", (Object)Integer.toString(this.getComponentsCount())).put((Object)"issueCount", (Object)Integer.toString(this.getIssueCount())).put((Object)"includeUserMentions", (Object)this.isIncludeUserMentions()).put((Object)"distribution", (Object)this.getDistributionOptions()).put((Object)"maxPercentagePerProject", (Object)Double.toString(this.getMaxPercentagePerProject())).put((Object)"distributionTailTreshold", (Object)Integer.toString(this.getDistributionTailTreshold())).put((Object)"issueCommentCount", (Object)Double.toString(this.getIssueCommentCount())).put((Object)"issueWorklogCount", (Object)Integer.toString(this.getIssueWorklogCount())).put((Object)"period", (Object)Integer.toString(this.getPeriod())).put((Object)"populatedCustomFieldCount", (Object)Integer.toString(this.getPopulatedCustomFieldCount())).put((Object)"averageTransitionCount", (Object)Integer.toString(this.getAverageTransitionCount())).put((Object)"subTasksProbability", (Object)Float.toString(this.getSubTasksProbability())).put((Object)"issueLinkProbability", (Object)Float.toString(this.getIssueLinkProbability())).put((Object)"reassignProbability", (Object)Float.toString(this.getReassignProbability())).put((Object)"performReindex", (Object)this.isPerformReindex()).put((Object)"assignSecurityLevelsToIssues", (Object)this.isAssignSecurityLevelsToIssues()).put((Object)"errors", (Object)this.getErrorsMap()).put((Object)"xsrfToken", (Object)this.getXsrfToken()).build();
    }
    
    public Long[] getProjectIds() {
        return this.context.generatorConfiguration.projectIds;
    }
    
    public Set<Long> getProjectIdsSet() {
        return (this.context.generatorConfiguration.projectIds != null) ? Sets.newHashSet((Object[])this.context.generatorConfiguration.projectIds) : Collections.emptySet();
    }
    
    public List<Map<String, Object>> getDistributionOptions() {
        final ImmutableList.Builder<Map<String, Object>> builder = (ImmutableList.Builder<Map<String, Object>>)ImmutableList.builder();
        builder.add((Object)ImmutableMap.of((Object)"id", (Object)"jdg-equal-issue-distribution", (Object)"isChecked", (Object)false, (Object)"labelText", (Object)"Equal distribiution", (Object)"value", (Object)"equal.distribution"));
        builder.add((Object)ImmutableMap.of((Object)"id", (Object)"jdg-max-percent-issue-distribution", (Object)"isChecked", (Object)true, (Object)"labelText", (Object)"Max percentage distribution", (Object)"value", (Object)"max.percentage.distribution"));
        return (List<Map<String, Object>>)builder.build();
    }
    
    public int getDistributionTailTreshold() {
        return this.context.generatorConfiguration.distributionTailTreshold;
    }
    
    public double getMaxPercentagePerProject() {
        return this.context.generatorConfiguration.maxPercentagePerProject;
    }
    
    public int getIssueCount() {
        return this.context.generatorConfiguration.issues.count;
    }
    
    public boolean isIncludeUserMentions() {
        return this.context.generatorConfiguration.includeUserMentions;
    }
    
    public int getPeriod() {
        return this.context.generatorConfiguration.period;
    }
    
    public boolean isPerformReindex() {
        return this.context.generatorConfiguration.performReindex;
    }
    
    public boolean isAssignSecurityLevelsToIssues() {
        return this.context.generatorConfiguration.assignSecurityLevelsToIssues;
    }
    
    public int getAverageTransitionCount() {
        return this.context.generatorConfiguration.transitions.avgCount;
    }
    
    public int getPopulatedCustomFieldCount() {
        return this.context.generatorConfiguration.customFields.changesPerEdit;
    }
    
    public float getReassignProbability() {
        return this.context.generatorConfiguration.transitions.reassignProbability;
    }
    
    public double getIssueCommentCount() {
        return this.context.generatorConfiguration.issues.commentCount;
    }
    
    public int getIssueWorklogCount() {
        return this.context.generatorConfiguration.issues.issueWorklogCount;
    }
    
    public float getSubTasksProbability() {
        return this.context.generatorConfiguration.issues.subTasksProbability;
    }
    
    public float getIssueLinkProbability() {
        return this.context.generatorConfiguration.issues.issueLinkProbability;
    }
    
    public void setProjectIds(final Long[] projectIds) {
        this.context.generatorConfiguration.projectIds = projectIds;
    }
    
    public void setIssueCount(final int issueCount) {
        this.context.generatorConfiguration.issues.count = issueCount;
    }
    
    public void setIncludeUserMentions(final boolean includeUserMentions) {
        this.context.generatorConfiguration.includeUserMentions = includeUserMentions;
    }
    
    public void setPeriod(final int period) {
        this.context.generatorConfiguration.period = period;
    }
    
    public void setPerformReindex(final boolean performReindex) {
        this.context.generatorConfiguration.performReindex = performReindex;
    }
    
    public void setAssignSecurityLevelsToIssues(final boolean assignSecurityLevelsToIssues) {
        this.context.generatorConfiguration.assignSecurityLevelsToIssues = assignSecurityLevelsToIssues;
    }
    
    public void setAverageTransitionCount(final int averageTransitionCount) {
        this.context.generatorConfiguration.transitions.avgCount = averageTransitionCount;
    }
    
    public void setPopulatedCustomFieldCount(final int populatedCustomFieldCount) {
        this.context.generatorConfiguration.customFields.changesPerEdit = populatedCustomFieldCount;
    }
    
    public void setReassignProbability(final float reassignProbability) {
        this.context.generatorConfiguration.transitions.reassignProbability = reassignProbability;
    }
    
    public void setIssueCommentCount(final double issueCommentCount) {
        this.context.generatorConfiguration.issues.commentCount = issueCommentCount;
    }
    
    public void setIssueWorklogCount(final int issueWorklogCount) {
        this.context.generatorConfiguration.issues.issueWorklogCount = issueWorklogCount;
    }
    
    public void setSubTasksProbability(final float subTasksProbability) {
        this.context.generatorConfiguration.issues.subTasksProbability = subTasksProbability;
    }
    
    public void setIssueLinkProbability(final float issueLinkProbability) {
        this.context.generatorConfiguration.issues.issueLinkProbability = issueLinkProbability;
    }
    
    public void setVersionsCount(final int count) {
        this.context.generatorConfiguration.versionsCount = count;
    }
    
    public int getVersionsCount() {
        return this.context.generatorConfiguration.versionsCount;
    }
    
    public void setComponentsCount(final int count) {
        this.context.generatorConfiguration.componentsCount = count;
    }
    
    public int getComponentsCount() {
        return this.context.generatorConfiguration.componentsCount;
    }
    
    public void setDistribution(final String distribution) {
        this.context.generatorConfiguration.distribution = distribution;
    }
    
    public void setDistributionTailTreshold(final int distributionTailTreshold) {
        this.context.generatorConfiguration.distributionTailTreshold = distributionTailTreshold;
    }
    
    public void setMaxPercentagePerProject(final double maxPercentagePerProject) {
        this.context.generatorConfiguration.maxPercentagePerProject = maxPercentagePerProject;
    }
}
