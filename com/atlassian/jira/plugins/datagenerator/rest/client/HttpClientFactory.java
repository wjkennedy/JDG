// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.rest.client;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.HttpClient;

public class HttpClientFactory
{
    public static HttpClient getAuthenticatedHttpClient(final String username, final String password) {
        return getHttpClient(username, password);
    }
    
    private static HttpClient getHttpClient(final String username, final String password) {
        final HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);
        final Credentials credentials = (Credentials)new UsernamePasswordCredentials(username, password);
        client.getState().setCredentials(new AuthScope(AuthScope.ANY), credentials);
        return client;
    }
}
