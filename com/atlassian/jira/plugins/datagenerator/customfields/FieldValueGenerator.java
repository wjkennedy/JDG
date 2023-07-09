// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.customfields;

import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import com.atlassian.jira.plugins.datagenerator.CustomFieldInfo;
import com.atlassian.jira.plugins.datagenerator.IssueInfo;

public interface FieldValueGenerator
{
    Object generate(final IssueInfo p0, final CustomFieldInfo p1);
    
    void generateOptions(final CustomFieldInfo p0);
    
    String fieldType();
    
    Object convertToDbValue(final Object p0);
    
    public interface Factory
    {
        FieldValueGenerator create(final GeneratorContext p0);
        
        boolean isEnabled(final GeneratorContext p0);
    }
}
