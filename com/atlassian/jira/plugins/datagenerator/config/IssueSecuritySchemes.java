// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.config;

public class IssueSecuritySchemes
{
    public int schemesCount;
    public int levelsPerScheme;
    public int permissionsPerLevel;
    
    public IssueSecuritySchemes() {
        this.schemesCount = 0;
        this.levelsPerScheme = 0;
        this.permissionsPerLevel = 0;
    }
}
