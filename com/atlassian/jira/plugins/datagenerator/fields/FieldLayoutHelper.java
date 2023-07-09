// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.fields;

import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.project.Project;
import java.util.List;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayout;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;

public class FieldLayoutHelper
{
    final FieldLayoutManager fieldLayoutManager;
    
    public FieldLayoutHelper(final FieldLayoutManager fieldLayoutManager) {
        this.fieldLayoutManager = fieldLayoutManager;
    }
    
    public void addSchemeAssociation(final GenericValue genericValue, final Long newFieldLayoutSchemeID) {
        try {
            this.fieldLayoutManager.addSchemeAssociation(genericValue, newFieldLayoutSchemeID);
        }
        catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public EditableDefaultFieldLayout getEditableDefaultFieldLayout() {
        try {
            return this.fieldLayoutManager.getEditableDefaultFieldLayout();
        }
        catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void storeEditableFieldLayout(final EditableFieldLayout editableFieldLayout) {
        try {
            this.fieldLayoutManager.storeEditableFieldLayout(editableFieldLayout);
        }
        catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public List<EditableFieldLayout> getEditableFieldLayouts() {
        try {
            return this.fieldLayoutManager.getEditableFieldLayouts();
        }
        catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public FieldLayout getFieldLayout(final Project project, final String id) {
        try {
            return this.fieldLayoutManager.getFieldLayout(project, id);
        }
        catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
