// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.drivers;

import java.util.Set;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Collections;
import com.google.common.collect.Lists;
import com.atlassian.jira.plugins.datagenerator.rest.client.RestResponse;
import org.json.JSONObject;
import com.atlassian.jira.plugins.datagenerator.rest.client.RestRequest;
import com.atlassian.jira.plugins.datagenerator.rest.client.DefaultRestClient;
import org.joda.time.LocalDate;
import org.ofbiz.core.entity.GenericEntityException;
import org.json.JSONException;
import com.atlassian.jira.plugins.datagenerator.util.JiraSoftwareUtil;
import com.atlassian.jira.plugins.datagenerator.model.RapidViewInfo;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.text.SimpleDateFormat;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.jira.plugins.datagenerator.UserUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import com.atlassian.jira.plugins.datagenerator.generators.RestDataGenerator;

@Service
public class SprintDriver implements RestDataGenerator
{
    private static final Logger log;
    private static final int MAX_ISSUES_MOVED_IN_ONE_REQUEST = 50;
    @Autowired
    private UserUtils userUtils;
    @Autowired
    private ApplicationProperties applicationProperties;
    @Autowired
    private IssueManager issueManager;
    private final SimpleDateFormat sdf;
    private final Random random;
    
    public SprintDriver() throws NoSuchAlgorithmException {
        this.sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        this.random = new Random();
    }
    
    public void generateSprintWithIssues(final GeneratorContext context, final Project project, final RapidViewInfo rapidViewInfo) throws JSONException, GenericEntityException {
        if (!JiraSoftwareUtil.isSoftwarePresent()) {
            return;
        }
        final int sprintId = this.generateSprint(rapidViewInfo);
        this.moveIssuesToSprint(context, project, sprintId);
        this.startSprint(sprintId);
    }
    
    private int generateSprint(final RapidViewInfo rapidViewInfo) throws JSONException {
        final CreateSprintRequest request = new CreateSprintRequest();
        request.name = "sprint " + Math.abs(this.random.nextInt());
        request.startDate = this.sdf.format(LocalDate.now().toDate());
        request.endDate = this.sdf.format(LocalDate.now().plusWeeks(2).toDate());
        request.originBoardId = rapidViewInfo.getId();
        final DefaultRestClient restClient = new DefaultRestClient(this.applicationProperties, this.userUtils);
        final RestRequest.Builder builder = RestRequest.builder();
        builder.resourceUrl("sprint/");
        builder.body(request);
        final RestResponse restResponse = restClient.sendPostRequest(this, builder.build());
        if (restResponse.getCode() != 201) {
            throw new RuntimeException(String.format("%s (status code %d)", restResponse.getContent(), restResponse.getCode()));
        }
        final JSONObject jsonObject = new JSONObject(restResponse.getContent());
        return jsonObject.getInt("id");
    }
    
    private void moveIssuesToSprint(final GeneratorContext context, final Project project, final int sprintId) throws GenericEntityException {
        final List<Long> issueIdsForProject = Lists.newArrayList((Iterable)this.issueManager.getIssueIdsForProject(project.getId()));
        Collections.shuffle(issueIdsForProject);
        int issuesPerBoard = Math.min(context.generatorConfiguration.issuesPerBoard, issueIdsForProject.size());
        issuesPerBoard = ((issuesPerBoard > 0) ? (this.random.nextInt(issuesPerBoard) + 1) : 0);
        int offset = 0;
        while (issuesPerBoard > 0) {
            final int issuesToMove = Math.min(issuesPerBoard, 49);
            final List<Long> subListIds = issueIdsForProject.subList(offset, issuesToMove + offset);
            issuesPerBoard -= 49;
            offset += 49;
            final MoveIssuesRequest request = new MoveIssuesRequest();
            request.issues = Sets.newHashSet((Iterable)subListIds);
            final DefaultRestClient restClient = new DefaultRestClient(this.applicationProperties, this.userUtils);
            final RestRequest.Builder builder = RestRequest.builder();
            builder.resourceUrl("sprint/" + sprintId + "/issue");
            builder.body(request);
            final RestResponse restResponse = restClient.sendPostRequest(this, builder.build());
            if (restResponse.getCode() != 204) {
                throw new RuntimeException(String.format("%s (status code %d), sprintId=%d", restResponse.getContent(), restResponse.getCode(), sprintId));
            }
        }
    }
    
    private void startSprint(final int sprintId) {
        final StartSprintRequest request = new StartSprintRequest();
        request.state = "active";
        final DefaultRestClient restClient = new DefaultRestClient(this.applicationProperties, this.userUtils);
        final RestRequest.Builder builder = RestRequest.builder();
        builder.resourceUrl("sprint/" + sprintId);
        builder.body(request);
        final RestResponse restResponse = restClient.sendPostRequest(this, builder.build());
        if (restResponse.getCode() != 200) {
            throw new RuntimeException(String.format("%s (status code %d), sptrintId=%d", restResponse.getContent(), restResponse.getCode(), sprintId));
        }
    }
    
    @Override
    public String getUrlPrefix() {
        return "agile";
    }
    
    @Override
    public String getApiVersion() {
        return "1.0";
    }
    
    static {
        log = LoggerFactory.getLogger((Class)SprintDriver.class);
    }
    
    @JsonAutoDetect
    private static class CreateSprintRequest
    {
        @JsonProperty
        String name;
        @JsonProperty
        String startDate;
        @JsonProperty
        String endDate;
        @JsonProperty
        Integer originBoardId;
    }
    
    @JsonAutoDetect
    private static class MoveIssuesRequest
    {
        @JsonProperty
        Set<Long> issues;
    }
    
    @JsonAutoDetect
    private static class StartSprintRequest
    {
        @JsonProperty
        String state;
    }
}
