// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.model;

import java.util.List;

public class ScreenInfo
{
    private final int index;
    final List<Integer> visibleFields;
    
    public ScreenInfo(final int index, final List<Integer> visibleFields) {
        this.index = index;
        this.visibleFields = visibleFields;
    }
    
    public int getIndex() {
        return this.index;
    }
    
    public List<Integer> getVisibleFields() {
        return this.visibleFields;
    }
    
    public int getHumanReadableIndex() {
        return this.getIndex() + 1;
    }
}
