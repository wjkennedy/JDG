// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.model;

public class RapidViewInfo
{
    private final int id;
    private final String name;
    
    public RapidViewInfo(final int id, final String name) {
        this.id = id;
        this.name = name;
    }
    
    public int getId() {
        return this.id;
    }
    
    public String getName() {
        return this.name;
    }
    
    @Override
    public String toString() {
        return "RapidViewInfo{id=" + this.id + ", name='" + this.name + '\'' + '}';
    }
}
