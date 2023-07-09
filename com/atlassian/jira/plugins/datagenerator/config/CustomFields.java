// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.config;

public class CustomFields
{
    public int fieldCount;
    public int screenSchemes;
    public boolean restrictByFieldConfig;
    public int customFieldsPerScreen;
    public boolean useGlobalCustomFieldContext;
    public int avgEdits;
    public int changesPerEdit;
    public long avgInterval;
    public boolean numericEnabled;
    public boolean dateTimeEnabled;
    public boolean singleUserPickerEnabled;
    public boolean multiUserPickerEnabled;
    public int maxUsersToAssignInMultiUserPicker;
    public TextConfiguration freeText;
    public OptionsConfiguration singleSelect;
    public OptionsConfiguration multiSelect;
    
    public CustomFields() {
        this.fieldCount = 0;
        this.screenSchemes = 0;
        this.customFieldsPerScreen = 1;
        this.useGlobalCustomFieldContext = true;
        this.avgEdits = 5;
        this.changesPerEdit = 5;
        this.avgInterval = 10800000L;
        this.numericEnabled = true;
        this.dateTimeEnabled = true;
        this.singleUserPickerEnabled = true;
        this.multiUserPickerEnabled = true;
        this.maxUsersToAssignInMultiUserPicker = 5;
        this.freeText = new TextConfiguration();
        this.singleSelect = new OptionsConfiguration();
        this.multiSelect = new OptionsConfiguration();
    }
}
