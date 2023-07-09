// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.model;

import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;

public interface FieldLayoutItemBuilder
{
    void setHidden(final boolean p0);
    
    FieldLayoutItem build();
}
