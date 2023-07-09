// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator;

import com.atlassian.jira.plugins.datagenerator.customfields.FieldValueGenerator;
import com.atlassian.jira.issue.fields.CustomField;

public class CustomFieldInfo
{
    public final CustomField customField;
    public final long id;
    public final String idStr;
    public final String name;
    public FieldValueGenerator valueGenerator;
    
    public CustomFieldInfo(final CustomField customField) {
        this.customField = customField;
        this.id = customField.getIdAsLong();
        this.idStr = customField.getId();
        this.name = customField.getName();
    }
    
    @Override
    public boolean equals(final Object o) {
        return this == o || (o != null && this.getClass() == o.getClass() && this.id == ((CustomFieldInfo)o).id);
    }
    
    @Override
    public int hashCode() {
        return (int)(this.id ^ this.id >>> 32);
    }
}
