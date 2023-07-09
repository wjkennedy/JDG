// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.config;

public class TextConfiguration
{
    public boolean enabled;
    public int minLength;
    public int maxLength;
    public float wikiRendererProbability;
    
    public TextConfiguration() {
        this.enabled = true;
        this.minLength = 5;
        this.maxLength = 100;
        this.wikiRendererProbability = 0.2f;
    }
}
