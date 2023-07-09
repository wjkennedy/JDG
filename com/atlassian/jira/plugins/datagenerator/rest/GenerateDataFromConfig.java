// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import javax.ws.rs.core.Response;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.plugins.datagenerator.UserUtils;
import com.atlassian.jira.plugins.datagenerator.drivers.DataGeneratorDriver;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;

@Path("/generate")
@Produces({ "application/json" })
@Consumes({ "application/json" })
public class GenerateDataFromConfig
{
    private final DataGeneratorDriver dataGeneratorDriver;
    private final UserUtils userUtils;
    private final TaskManager taskManager;
    
    public GenerateDataFromConfig(final DataGeneratorDriver dataGeneratorDriver, final UserUtils userUtils, final TaskManager taskManager) {
        this.dataGeneratorDriver = dataGeneratorDriver;
        this.userUtils = userUtils;
        this.taskManager = taskManager;
    }
    
    @POST
    public Response generateDataFromConfig(final GeneratorConfiguration configuration) throws Exception {
        if (!this.hasPermission(44)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        final GeneratorContext context = new GeneratorContext();
        context.userName = this.userUtils.getLoggedInUsername();
        context.generatorConfiguration = configuration;
        final TaskDescriptor<String> taskDescriptor = this.dataGeneratorDriver.scheduleBothPhases(context);
        if (taskDescriptor == null) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        this.taskManager.waitUntilTaskCompletes(taskDescriptor.getTaskId());
        return Response.ok((Object)"Data generated").build();
    }
    
    @GET
    @Path("/config")
    public Response getConfig() {
        if (!this.hasPermission(44)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        final GeneratorConfiguration config = new GeneratorConfiguration();
        config.period = 365;
        return Response.ok((Object)config).build();
    }
    
    private boolean hasPermission(final int permissionsId) {
        return this.userUtils.hasPermission(permissionsId);
    }
}
