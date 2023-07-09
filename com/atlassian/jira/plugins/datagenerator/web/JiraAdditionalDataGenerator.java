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
public class JiraAdditionalDataGenerator extends StatusAwareGeneratorAction
{
    protected final GeneratorContext context;
    
    protected JiraAdditionalDataGenerator(final DataGeneratorDriver dataGeneratorDriver, @ComponentImport final PageBuilderService pageBuilderService) {
        super(dataGeneratorDriver, pageBuilderService);
        this.context = new GeneratorContext();
        this.context.generatorConfiguration = new GeneratorConfiguration();
    }
    
    protected void doValidation() {
        if (this.context.generatorConfiguration.attachmentCount < 0) {
            this.addError("attachments", "Please specify a non-negative integer for number of attachments to generate.");
        }
        if (this.context.generatorConfiguration.kanbanBoards < 0) {
            this.addError("kanbanBoards", "Please specify a non-negative integer for number of Kanban boards to generate.");
        }
        if (this.context.generatorConfiguration.scrumBoards < 0) {
            this.addError("scrumBoards", "Please specify a non-negative integer for number of Scrum boards to generate.");
        }
        if (this.context.generatorConfiguration.issuesPerBoard < 0 || this.context.generatorConfiguration.issuesPerBoard > 10000) {
            this.addError("issuesPerBoard", "Please specify a non-negative integer for issues per board to generate in range <0,10000>.");
        }
        if (this.context.generatorConfiguration.issueWatchers < 0) {
            this.addError("issueWatchers", "Please specify a non-negative integer for number of watchers to add.");
        }
        if (this.context.generatorConfiguration.additionalComments < 0) {
            this.addError("additionalComments", "Please specify a non-negative integer for number of additional comments to generate.");
        }
        if (this.context.generatorConfiguration.projectIds == null || this.context.generatorConfiguration.projectIds.length == 0) {
            this.addError("projectIds", "Please select at least one project.");
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
        if (!this.dataGeneratorDriver.scheduleAdditionalDataGeneration(this.context)) {
            this.addErrorMessage("Cannot schedule generation task. Is there active task running? Please reopen generation form to see if there is generation in progress or wait few seconds and try again.");
            return "input";
        }
        return this.getRedirect("JiraAdditionalDataGenerator.jspa");
    }
    
    @ActionViewData
    public Map<String, Object> getSoyData() {
        final ImmutableMap.Builder<String, Object> mapBuilder = (ImmutableMap.Builder<String, Object>)ImmutableMap.builder();
        return (Map<String, Object>)mapBuilder.put((Object)"errors", (Object)this.getErrorsMap()).put((Object)"projects", (Object)this.getProjects()).put((Object)"attachments", (Object)Integer.toString(this.context.generatorConfiguration.attachmentCount)).put((Object)"kanbanBoards", (Object)Integer.toString(this.context.generatorConfiguration.kanbanBoards)).put((Object)"scrumBoards", (Object)Integer.toString(this.context.generatorConfiguration.scrumBoards)).put((Object)"issuesPerBoard", (Object)Integer.toString(this.context.generatorConfiguration.issuesPerBoard)).put((Object)"issueWatchers", (Object)Integer.toString(this.context.generatorConfiguration.issueWatchers)).put((Object)"additionalComments", (Object)Integer.toString(this.context.generatorConfiguration.additionalComments)).put((Object)"includeUserMentions", (Object)this.context.generatorConfiguration.includeUserMentions).put((Object)"projectIds", (Object)this.context.generatorConfiguration.projectIds).put((Object)"xsrfToken", (Object)this.getXsrfToken()).build();
    }
    
    public Set<Long> getProjectIdsSet() {
        return (this.context.generatorConfiguration.projectIds != null) ? Sets.newHashSet((Object[])this.context.generatorConfiguration.projectIds) : Collections.emptySet();
    }
    
    public void setAttachments(final int attachments) {
        this.context.generatorConfiguration.attachmentCount = attachments;
    }
    
    public void setKanbanBoards(final int kanbanBoards) {
        this.context.generatorConfiguration.kanbanBoards = kanbanBoards;
    }
    
    public void setScrumBoards(final int scrumBoards) {
        this.context.generatorConfiguration.scrumBoards = scrumBoards;
    }
    
    public void setIssuesPerBoard(final int issuesPerBoard) {
        this.context.generatorConfiguration.issuesPerBoard = issuesPerBoard;
    }
    
    public void setIssueWatchers(final int watchers) {
        this.context.generatorConfiguration.issueWatchers = watchers;
    }
    
    public void setAdditionalComments(final int additionalComments) {
        this.context.generatorConfiguration.additionalComments = additionalComments;
    }
    
    public void setIncludeUserMentions(final boolean includeUserMentions) {
        this.context.generatorConfiguration.includeUserMentions = includeUserMentions;
    }
    
    public void setProjectIds(final Long[] projectIds) {
        this.context.generatorConfiguration.projectIds = projectIds;
    }
}
