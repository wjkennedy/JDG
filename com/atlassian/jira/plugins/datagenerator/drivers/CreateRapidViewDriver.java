// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.drivers;

import java.util.Set;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.slf4j.LoggerFactory;
import com.atlassian.jira.plugins.datagenerator.rest.client.RestResponse;
import org.json.JSONObject;
import com.atlassian.jira.plugins.datagenerator.rest.client.RestRequest;
import com.atlassian.jira.plugins.datagenerator.rest.client.DefaultRestClient;
import com.google.common.collect.ImmutableSet;
import com.atlassian.jira.plugins.datagenerator.util.JiraSoftwareUtil;
import com.atlassian.jira.plugins.datagenerator.model.RapidViewInfo;
import org.ofbiz.core.entity.GenericEntityException;
import org.json.JSONException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import java.security.NoSuchAlgorithmException;
import com.atlassian.jira.config.properties.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.jira.plugins.datagenerator.UserUtils;
import java.util.Random;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import com.atlassian.jira.plugins.datagenerator.generators.RestDataGenerator;

@Service
public class CreateRapidViewDriver implements RestDataGenerator
{
    private static final Logger log;
    private Random random;
    @Autowired
    private UserUtils userUtils;
    @Autowired
    private ApplicationProperties applicationProperties;
    @Autowired
    private SprintDriver sprintDriver;
    
    public CreateRapidViewDriver() throws NoSuchAlgorithmException {
        this.random = new Random();
    }
    
    public void generateKanbanBoard(final GeneratorContext context, final Project project) {
        try {
            this.generateBoards(context, project, "kanban");
        }
        catch (final JSONException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void generateScrumBoard(final GeneratorContext context, final Project project) {
        try {
            final RapidViewInfo rapidViewInfo = this.generateBoards(context, project, "scrum");
            this.sprintDriver.generateSprintWithIssues(context, project, rapidViewInfo);
        }
        catch (final JSONException e) {
            throw new RuntimeException(e);
        }
        catch (final GenericEntityException e2) {
            throw new RuntimeException((Throwable)e2);
        }
    }
    
    protected RapidViewInfo generateBoards(final GeneratorContext context, final Project project, final String preset) throws JSONException {
        if (!JiraSoftwareUtil.isSoftwarePresent()) {
            return null;
        }
        final CreatePresetsRequest request = new CreatePresetsRequest();
        request.name = project.getName() + Math.abs(this.random.nextInt());
        request.preset = preset;
        request.projectIds = (Set<String>)ImmutableSet.of((Object)Long.toString(project.getId()));
        final DefaultRestClient restClient = new DefaultRestClient(this.applicationProperties, this.userUtils);
        final RestRequest.Builder builder = RestRequest.builder();
        builder.resourceUrl("rapidview/create/presets");
        builder.body(request);
        final RestResponse restResponse = restClient.sendPostRequest(this, builder.build());
        if (restResponse.getCode() != 200) {
            throw new RuntimeException(String.format("%s (status code %d)", restResponse.getContent(), restResponse.getCode()));
        }
        if (restResponse.getContent() == null || restResponse.getContent().isEmpty()) {
            throw new RuntimeException("Board id was not returned, probably board not created");
        }
        final JSONObject jsonObject = new JSONObject(restResponse.getContent());
        final int id = jsonObject.getInt("id");
        final String name = jsonObject.getString("name");
        return new RapidViewInfo(id, name);
    }
    
    @Override
    public String getUrlPrefix() {
        return "greenhopper";
    }
    
    @Override
    public String getApiVersion() {
        return "1.0";
    }
    
    static {
        log = LoggerFactory.getLogger((Class)CreateRapidViewDriver.class);
    }
    
    @JsonAutoDetect
    private static class CreatePresetsRequest
    {
        @JsonProperty
        String name;
        @JsonProperty
        Set<String> projectIds;
        @JsonProperty
        String preset;
    }
}
