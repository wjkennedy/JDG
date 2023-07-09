// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.config;

public class PermissionSchemes
{
    public int schemesCount;
    public int developerGroupsCount;
    public int userGroupsCount;
    
    public PermissionSchemes() {
        this.schemesCount = 0;
        this.developerGroupsCount = 10;
        this.userGroupsCount = 10;
    }
}
