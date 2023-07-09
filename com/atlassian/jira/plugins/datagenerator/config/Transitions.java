// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.config;

public class Transitions
{
    public int avgCount;
    public long avgInterval;
    public float commentProbability;
    public int commentMinLength;
    public int commentMaxLength;
    public float reassignProbability;
    public float randomizeResolutionProbability;
    
    public Transitions() {
        this.avgCount = 5;
        this.avgInterval = 10800000L;
        this.commentProbability = 0.5f;
        this.commentMinLength = 5;
        this.commentMaxLength = 50;
        this.reassignProbability = 0.1f;
        this.randomizeResolutionProbability = 0.5f;
    }
}
