// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.db;

import com.atlassian.jira.issue.history.ChangeItemBean;

public class ChangeItemEntity extends Entity
{
    public ChangeItemEntity(final ChangeGroupEntity changeGroupEntity, final ChangeItemBean cib) {
        this.setRef("group", changeGroupEntity);
        this.put("fieldtype", cib.getFieldType());
        this.put("field", cib.getField());
        this.put("oldvalue", cib.getFrom());
        this.put("oldstring", cib.getFromString());
        this.put("newvalue", cib.getTo());
        this.put("newstring", cib.getToString());
    }
}
