// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.model;

import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItemImpl;

public class FieldLayoutItemBuilderImpl implements FieldLayoutItemBuilder
{
    final FieldLayoutItemImpl.Builder builder;
    
    public FieldLayoutItemBuilderImpl(final FieldLayoutItem fieldLayoutItem) {
        this.builder = new FieldLayoutItemImpl.Builder(fieldLayoutItem);
    }
    
    @Override
    public void setHidden(final boolean hidden) {
        this.builder.setHidden(hidden);
    }
    
    @Override
    public FieldLayoutItem build() {
        return (FieldLayoutItem)this.builder.build();
    }
}
