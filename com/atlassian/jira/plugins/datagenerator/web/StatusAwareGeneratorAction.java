// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.web;

import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import java.util.Collection;
import java.util.Iterator;
import com.google.common.collect.ImmutableList;
import java.util.List;
import com.atlassian.jira.web.action.ActionViewData;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import java.util.Collections;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.atlassian.jira.plugins.datagenerator.drivers.DataGeneratorDriver;
import java.util.UUID;
import com.atlassian.jira.plugins.datagenerator.rest.model.GeneratorStatus;
import com.atlassian.jira.security.request.RequestMethod;
import com.atlassian.jira.security.request.SupportedMethods;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import webwork.action.CommandDriven;
import com.atlassian.jira.web.action.JiraWebActionSupport;

@WebSudoRequired
@SupportedMethods({ RequestMethod.GET })
public abstract class StatusAwareGeneratorAction extends JiraWebActionSupport implements CommandDriven
{
    private static final String STATUS = "status";
    static final String CANNOT_SCHEDULE_ERROR = "Cannot schedule generation task. Is there active task running? Please reopen generation form to see if there is generation in progress or wait few seconds and try again.";
    private final GeneratorStatus generatorStatus;
    private final boolean showStatus;
    private UUID acknowledgeContextId;
    final DataGeneratorDriver dataGeneratorDriver;
    
    protected StatusAwareGeneratorAction(final DataGeneratorDriver dataGeneratorDriver, @ComponentImport final PageBuilderService pageBuilderService) {
        pageBuilderService.assembler().resources().requireContext("datagenerator");
        this.command = "default";
        this.dataGeneratorDriver = dataGeneratorDriver;
        final GeneratorContext lastContext = dataGeneratorDriver.getLastContext();
        this.showStatus = (lastContext != null);
        this.generatorStatus = ((lastContext != null) ? lastContext.toRest() : new GeneratorStatus("No data generation in progress", Collections.singletonList("No data generation process is currently in process"), "", 0, true));
    }
    
    public String doDefault() throws Exception {
        if (!this.hasGlobalPermission(GlobalPermissionKey.SYSTEM_ADMIN)) {
            return "permissionviolation";
        }
        if (this.showStatus) {
            return "status";
        }
        return "input";
    }
    
    @RequiresXsrfCheck
    @SupportedMethods({ RequestMethod.POST })
    public String doAcknowledge() throws Exception {
        if (!this.hasGlobalPermission(GlobalPermissionKey.SYSTEM_ADMIN)) {
            return "permissionviolation";
        }
        final GeneratorContext lastContext = this.dataGeneratorDriver.getLastContext();
        if (lastContext != null) {
            if (!lastContext.id.equals(this.acknowledgeContextId)) {
                this.addErrorMessage("You're trying to acknowledge outdated status. Please review new one and acknowledge once again.");
                return "status";
            }
            this.dataGeneratorDriver.clearLastContext();
        }
        return "input";
    }
    
    @ActionViewData
    public Map<String, Object> getStatusSoyData() {
        final ImmutableMap.Builder<String, Object> mapBuilder = (ImmutableMap.Builder<String, Object>)ImmutableMap.builder();
        return (Map<String, Object>)mapBuilder.put((Object)"inProgress", (Object)this.isInProgress()).put((Object)"currentStatus", (Object)this.getCurrentStatus()).put((Object)"lastContextId", (Object)this.getLastContextId()).put((Object)"errors", (Object)this.getErrorsMap()).build();
    }
    
    protected ImmutableMap<String, List<String>> getErrorsMap() {
        final Map<String, String> errors = super.getErrors();
        final ImmutableMap.Builder<String, List<String>> errorsTransformed = (ImmutableMap.Builder<String, List<String>>)ImmutableMap.builder();
        for (final Map.Entry<String, String> entry : errors.entrySet()) {
            errorsTransformed.put((Object)entry.getKey(), (Object)ImmutableList.of((Object)entry.getValue()));
        }
        return (ImmutableMap<String, List<String>>)errorsTransformed.build();
    }
    
    public String getLastContextId() {
        final GeneratorContext lastContext = this.dataGeneratorDriver.getLastContext();
        return (lastContext == null) ? "" : lastContext.id.toString();
    }
    
    public boolean isInProgress() {
        return !this.generatorStatus.isFinished();
    }
    
    public GeneratorStatus getCurrentStatus() {
        return this.generatorStatus;
    }
    
    public Collection<Project> getProjects() {
        return this.getPermissionManager().getProjects(ProjectPermissions.BROWSE_PROJECTS, this.getLoggedInUser());
    }
    
    public void setAcknowledgeContextId(final String acknowledgeContextId) {
        this.acknowledgeContextId = UUID.fromString(acknowledgeContextId);
    }
}
