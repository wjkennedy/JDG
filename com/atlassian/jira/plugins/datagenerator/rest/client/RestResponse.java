// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.rest.client;

public class RestResponse
{
    private final int code;
    private final String content;
    
    RestResponse(final int responseCode) {
        this.code = responseCode;
        this.content = "";
    }
    
    RestResponse(final int responseCode, final String content) {
        this.code = responseCode;
        this.content = content;
    }
    
    public int getCode() {
        return this.code;
    }
    
    public String getContent() {
        return this.content;
    }
    
    @Override
    public String toString() {
        return "RestResponse{code=" + this.code + ", content='" + this.content + '\'' + '}';
    }
}
