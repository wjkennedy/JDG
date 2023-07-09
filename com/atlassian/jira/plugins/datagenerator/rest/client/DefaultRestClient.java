// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.rest.client;

import org.apache.commons.lang3.StringUtils;
import javax.annotation.Nonnull;
import org.apache.commons.httpclient.NameValuePair;
import java.util.Map;
import java.io.UnsupportedEncodingException;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.Header;
import org.apache.http.entity.ContentType;
import java.io.IOException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import java.util.Iterator;
import java.util.List;
import com.atlassian.jira.plugins.datagenerator.generators.RestDataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import io.atlassian.util.concurrent.Lazy;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.apache.commons.httpclient.HttpClient;
import java.util.function.Supplier;
import com.atlassian.jira.plugins.datagenerator.UserUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import org.springframework.stereotype.Component;

@Component
public class DefaultRestClient
{
    public static final String JDG_REST_ADMIN = "jdg-rest-admin";
    public static final String JDG_REST_ADMIN_PASSWORD = "jdgrestadmin";
    private final ApplicationProperties applicationProperties;
    private final UserUtils userUtils;
    private final Supplier<HttpClient> authenticatedHttpClient;
    
    @Autowired
    public DefaultRestClient(@ComponentImport final ApplicationProperties applicationProperties, final UserUtils userUtils) {
        this.authenticatedHttpClient = Lazy.supplier((Supplier)new Supplier<HttpClient>() {
            @Override
            public HttpClient get() {
                final ApplicationUser restAdmin = DefaultRestClient.this.userUtils.getUserByName("jdg-rest-admin");
                if (restAdmin == null) {
                    try {
                        DefaultRestClient.this.userUtils.createAdmin("jdg-rest-admin", "jdgrestadmin", "jdg@localhost.com", "JDG REST Admin");
                        DefaultRestClient.this.userUtils.addUserToGroup("jira-administrators", "jdg-rest-admin");
                        DefaultRestClient.this.userUtils.addUserToGroup("atlassian-staff", "jdg-rest-admin");
                    }
                    catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                return HttpClientFactory.getAuthenticatedHttpClient("jdg-rest-admin", "jdgrestadmin");
            }
        });
        this.applicationProperties = applicationProperties;
        this.userUtils = userUtils;
    }
    
    public RestResponse sendPostRequest(final RestDataGenerator restDataGenerator, final RestRequest request) {
        return this.sendPost(this.authenticatedHttpClient.get(), this.createPost(restDataGenerator, request));
    }
    
    public void sendPostRequests(final RestDataGenerator generator, final List<RestRequest> requests) {
        final HttpClient authenticatedHttpClient = HttpClientFactory.getAuthenticatedHttpClient("jdg-rest-admin", "jdgrestadmin");
        for (final RestRequest request : requests) {
            this.sendPost(authenticatedHttpClient, this.createPost(generator, request));
        }
    }
    
    public RestResponse sendPost(final HttpClient client, final PostMethod post) {
        try {
            return new RestResponse(client.executeMethod((HttpMethod)post), post.getResponseBodyAsString());
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private PostMethod createPost(final RestDataGenerator restDataGenerator, final RestRequest request) {
        final PostMethod post = new PostMethod(this.getFullUrl(restDataGenerator, request));
        if (request.getPathParams() != null && !request.getPathParams().isEmpty()) {
            post.addParameters(this.getNameValueParams(request.getPathParams()));
        }
        if (request.getBody() != null && !request.getBody().isEmpty()) {
            final ContentType contentType = ContentType.APPLICATION_JSON;
            post.setRequestHeader(new Header("Content-Type", contentType.getMimeType()));
            try {
                post.setRequestEntity((RequestEntity)new StringRequestEntity(request.getBody(), contentType.getMimeType(), contentType.getCharset().name()));
            }
            catch (final UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return post;
    }
    
    private NameValuePair[] getNameValueParams(final Map<String, String> pathParams) {
        if (pathParams == null || pathParams.isEmpty()) {
            return null;
        }
        final NameValuePair[] nvp = new NameValuePair[pathParams.keySet().size()];
        int i = 0;
        for (final Map.Entry<String, String> entry : pathParams.entrySet()) {
            nvp[i++] = new NameValuePair((String)entry.getKey(), (String)entry.getValue());
        }
        return nvp;
    }
    
    private String getFullUrl(@Nonnull final RestDataGenerator restDataGenerator, @Nonnull final RestRequest restRequest) {
        final String jiraBaseUrl = this.applicationProperties.getString("jira.baseurl");
        return jiraBaseUrl + "/rest/" + restDataGenerator.getUrlPrefix() + '/' + (String)StringUtils.defaultIfEmpty((CharSequence)restRequest.getApiVersion(), (CharSequence)restDataGenerator.getApiVersion()) + '/' + restRequest.getResourceUrl();
    }
}
