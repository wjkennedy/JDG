// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.rest.client;

import java.io.IOException;
import org.codehaus.jackson.map.ObjectMapper;
import java.util.Map;

public class RestRequest
{
    private String resourceUrl;
    private Map<String, String> pathParams;
    private String body;
    private String apiVersion;
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static Builder defaultBuilder() {
        final Builder builder = new Builder();
        return builder.apiVersion("1");
    }
    
    public String getResourceUrl() {
        return this.resourceUrl;
    }
    
    public Map<String, String> getPathParams() {
        return this.pathParams;
    }
    
    public String getBody() {
        return this.body;
    }
    
    public String getApiVersion() {
        return this.apiVersion;
    }
    
    public static class Builder
    {
        RestRequest instance;
        
        Builder() {
            this.instance = new RestRequest();
        }
        
        public Builder resourceUrl(final String resourceUrl) {
            this.instance.resourceUrl = resourceUrl;
            return this;
        }
        
        public Builder pathParams(final Map<String, String> pathParams) {
            this.instance.pathParams = pathParams;
            return this;
        }
        
        public Builder body(final String body) {
            this.instance.body = body;
            return this;
        }
        
        public Builder body(final Object body) {
            try {
                this.instance.body = new ObjectMapper().writeValueAsString(body);
            }
            catch (final IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }
        
        public Builder apiVersion(final String apiVersion) {
            this.instance.apiVersion = apiVersion;
            return this;
        }
        
        public RestRequest build() {
            return this.instance;
        }
    }
}
