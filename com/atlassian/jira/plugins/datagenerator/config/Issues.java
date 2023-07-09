// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.config;

import java.util.concurrent.TimeUnit;

public class Issues
{
    public int count;
    public int summaryMinLength;
    public int summaryMaxLength;
    public int descriptionMinLength;
    public int descriptionMaxLength;
    public float descriptionPc;
    public int environmentMaxLength;
    public int environmentMinLength;
    public float environmentPc;
    public double commentCount;
    public int commentMinLength;
    public int commentMaxLength;
    public float commentTailPc;
    public int commentMaxCount;
    public long avgCommentInterval;
    public int subTasksMinLength;
    public int subTasksMaxLength;
    public float subTasksProbability;
    public float issueLinkProbability;
    public int issueWorklogCount;
    public int avgWorklogInterval;
    
    public Issues() {
        this.count = 300;
        this.summaryMinLength = 5;
        this.summaryMaxLength = 10;
        this.descriptionMinLength = 0;
        this.descriptionMaxLength = 120;
        this.descriptionPc = 0.9f;
        this.environmentMaxLength = 200;
        this.environmentMinLength = 10;
        this.environmentPc = 0.1f;
        this.commentCount = 5.0;
        this.commentMinLength = 5;
        this.commentMaxLength = 50;
        this.commentTailPc = 0.3f;
        this.commentMaxCount = 1000;
        this.avgCommentInterval = 10800000L;
        this.subTasksMinLength = 2;
        this.subTasksMaxLength = 10;
        this.subTasksProbability = 0.1f;
        this.issueLinkProbability = 0.0f;
        this.issueWorklogCount = 2;
        this.avgWorklogInterval = (int)TimeUnit.HOURS.toMillis(8L);
    }
}
