// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.customfields;

import com.atlassian.jira.plugins.datagenerator.CustomFieldInfo;

abstract class AbstractFieldValueGenerator implements FieldValueGenerator
{
    @Override
    public void generateOptions(final CustomFieldInfo customFieldInfo) {
    }
    
    @Override
    public Object convertToDbValue(final Object generatedValue) {
        return generatedValue;
    }
}
