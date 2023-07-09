// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.web;

import com.google.common.collect.ImmutableList;
import java.util.List;
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
public class JiraIssueHistoryGenerator extends StatusAwareGeneratorAction
{
    protected final GeneratorContext context;
    
    protected JiraIssueHistoryGenerator(final DataGeneratorDriver dataGeneratorDriver, @ComponentImport final PageBuilderService pageBuilderService) {
        super(dataGeneratorDriver, pageBuilderService);
        this.context = new GeneratorContext();
        this.context.generatorConfiguration = new GeneratorConfiguration();
    }
    
    protected void doValidation() {
        if (this.context.generatorConfiguration.projectIds == null || this.context.generatorConfiguration.projectIds.length == 0) {
            this.addError("projectIds", "Please select at least one project.");
        }
        if (this.context.generatorConfiguration.assigneesCount < 0) {
            this.addError("assigneesCount", "Please specify a non-negative integer for number of assignees to change.");
        }
        if (this.context.generatorConfiguration.reportersCount < 0) {
            this.addError("reportersCount", "Please specify a non-negative integer for number of reporters to change.");
        }
        if (this.context.generatorConfiguration.descriptionsCount < 0) {
            this.addError("descriptionsCount", "Please specify a non-negative integer for number of descriptions to change.");
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
        if (!this.dataGeneratorDriver.scheduleIssueHistoryGeneration(this.context)) {
            this.addErrorMessage("Cannot schedule generation task. Is there active task running? Please reopen generation form to see if there is generation in progress or wait few seconds and try again.");
            return "input";
        }
        return this.getRedirect("JiraIssueHistoryGenerator.jspa");
    }
    
    @ActionViewData
    public Map<String, Object> getSoyData() {
        final ImmutableMap.Builder<String, Object> mapBuilder = (ImmutableMap.Builder<String, Object>)ImmutableMap.builder();
        return (Map<String, Object>)mapBuilder.put((Object)"errors", (Object)this.getErrorsMap()).put((Object)"projects", (Object)this.getProjects()).put((Object)"projectIds", (Object)this.context.generatorConfiguration.projectIds).put((Object)"changesAuthor", (Object)this.getChangesAuthorOptions()).put((Object)"assigneesCount", (Object)Integer.toString(this.context.generatorConfiguration.assigneesCount)).put((Object)"reportersCount", (Object)Integer.toString(this.context.generatorConfiguration.reportersCount)).put((Object)"descriptionsCount", (Object)Integer.toString(this.context.generatorConfiguration.descriptionsCount)).put((Object)"includeUserMentions", (Object)this.context.generatorConfiguration.includeUserMentions).put((Object)"xsrfToken", (Object)this.getXsrfToken()).build();
    }
    
    public List<Map<String, Object>> getChangesAuthorOptions() {
        final ImmutableList.Builder<Map<String, Object>> builder = (ImmutableList.Builder<Map<String, Object>>)ImmutableList.builder();
        builder.add((Object)ImmutableMap.of((Object)"id", (Object)"jdg-current-user-author", (Object)"isChecked", (Object)false, (Object)"labelText", (Object)"Current user", (Object)"value", (Object)"0"));
        builder.add((Object)ImmutableMap.of((Object)"id", (Object)"jdg-developers-author", (Object)"isChecked", (Object)true, (Object)"labelText", (Object)"Developers", (Object)"value", (Object)"1"));
        builder.add((Object)ImmutableMap.of((Object)"id", (Object)"jdg-all-users-author", (Object)"isChecked", (Object)false, (Object)"labelText", (Object)"All users", (Object)"value", (Object)"2"));
        return (List<Map<String, Object>>)builder.build();
    }
    
    public void setProjectIds(final Long[] projectIds) {
        this.context.generatorConfiguration.projectIds = projectIds;
    }
    
    public void setChangesAuthor(final int changesAuthorValue) {
        this.context.generatorConfiguration.changesAuthor = GeneratorConfiguration.ChangeAuthor.values()[changesAuthorValue];
    }
    
    public void setAssigneesCount(final int assigneesCount) {
        this.context.generatorConfiguration.assigneesCount = assigneesCount;
    }
    
    public void setReportersCount(final int reportersCount) {
        this.context.generatorConfiguration.reportersCount = reportersCount;
    }
    
    public void setDescriptionsCount(final int descriptionsCount) {
        this.context.generatorConfiguration.descriptionsCount = descriptionsCount;
    }
    
    public void setIncludeUserMentions(final boolean includeUserMentions) {
        this.context.generatorConfiguration.includeUserMentions = includeUserMentions;
    }
}
