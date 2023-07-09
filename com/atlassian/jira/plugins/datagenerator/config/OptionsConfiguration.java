// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.config;

public class OptionsConfiguration
{
    public boolean enabled;
    public int avgOptionsCount;
    public int minOptionLength;
    public int maxOptionLength;
    
    public OptionsConfiguration() {
        this.enabled = true;
        this.avgOptionsCount = 5;
        this.minOptionLength = 1;
        this.maxOptionLength = 2;
    }
}
