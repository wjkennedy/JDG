// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.web;

import org.slf4j.LoggerFactory;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import com.atlassian.jira.web.action.ActionViewData;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.google.common.collect.Iterables;
import com.atlassian.jira.plugins.datagenerator.config.module.ConfigurationField;
import com.atlassian.jira.plugins.datagenerator.plugins.access.MetadataPluginConfigurationFormData;
import java.util.Enumeration;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableMap;
import com.atlassian.jira.util.ErrorCollection;
import java.util.Iterator;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugins.datagenerator.config.CustomFields;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Collection;
import com.atlassian.jira.plugins.datagenerator.plugins.access.MetadataPluginConfigurationValidationData;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.atlassian.jira.plugins.datagenerator.drivers.DataGeneratorDriver;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.plugins.datagenerator.plugins.access.MetadataPluginConfigurationAccessor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Map;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.slf4j.Logger;
import com.atlassian.jira.security.request.RequestMethod;
import com.atlassian.jira.security.request.SupportedMethods;

@SupportedMethods({ RequestMethod.GET })
public class JiraMetadataGenerator extends StatusAwareGeneratorAction
{
    private static final Logger LOG;
    protected final GeneratorContext context;
    private boolean numericCustomFieldEnabledValue;
    private boolean dateTimeCustomFieldEnabledValue;
    private boolean freeTextCustomFieldEnabledValue;
    private boolean singleSelectCustomFieldEnabledValue;
    private boolean multiSelectCustomFieldEnabledValue;
    private boolean singleUserPickerCustomFieldEnabledValue;
    private boolean multiUserPickerCustomFieldEnabledValue;
    private boolean restrictByFieldConfigValue;
    private boolean useGlobalCustomFieldContextValue;
    private final Map<String, Boolean> enabledPlugins;
    private final AtomicBoolean pluginDefaultValuesSet;
    private final MetadataPluginConfigurationAccessor metadataPluginConfigurationAccessor;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    
    public JiraMetadataGenerator(final DataGeneratorDriver dataGeneratorDriver, @ComponentImport final PageBuilderService pageBuilderService, final MetadataPluginConfigurationAccessor metadataPluginConfigurationAccessor, @ComponentImport final IssueTypeSchemeManager issueTypeSchemeManager) {
        super(dataGeneratorDriver, pageBuilderService);
        this.context = new GeneratorContext();
        this.pluginDefaultValuesSet = new AtomicBoolean(false);
        this.context.generatorConfiguration = new GeneratorConfiguration();
        if (this.getLoggedInUser() != null) {
            this.context.userName = this.getLoggedInUser().getName();
        }
        this.metadataPluginConfigurationAccessor = metadataPluginConfigurationAccessor;
        this.enabledPlugins = metadataPluginConfigurationAccessor.getEnabledStates();
        this.issueTypeSchemeManager = issueTypeSchemeManager;
    }
    
    protected void doValidation() {
        this.updateConfigurationBooleanValues();
        this.updatePluginConfigurationsFromRequest();
        if (this.context.generatorConfiguration.projectCount < 0) {
            this.addError("projectCount", "Please specify a positive integer for number of projects to generate.");
        }
        else if (this.context.generatorConfiguration.projectCount > 20000) {
            this.addError("projectCount", "Number of projects to generate cannot be higher than 20000.");
        }
        if (this.context.generatorConfiguration.usersCount < 0) {
            this.addError("usersCount", "Please specify a non-negative integer for number of users to generate.");
        }
        if (this.context.generatorConfiguration.developersCount < 0) {
            this.addError("developersCount", "Please specify a non-negative integer for number of developers to generate.");
        }
        if (this.context.generatorConfiguration.groupsCount < 0) {
            this.addError("groupsCount", "Please specify a non-negative integer for number of groups to generate.");
        }
        if (this.context.generatorConfiguration.rolesCount < 0) {
            this.addError("rolesCount", "Please specify a non-negative integer for number of roles to generate.");
        }
        if (this.context.generatorConfiguration.assignRolesCount < 0) {
            this.addError("assignRolesCount", "Please specify a non-negative integer for number of assign roles to generate.");
        }
        if (this.context.generatorConfiguration.additionalStatusesCount < 0) {
            this.addError("additionalStatusesCount", "Please specify a non-negative integer for number of workflow statuses to generate");
        }
        if (this.context.generatorConfiguration.workflows.count < 0) {
            this.addError("workflowCount", "Please specify a non-negative integer for number of workflows to generate");
        }
        final CustomFields customFields = this.context.generatorConfiguration.customFields;
        if (customFields.fieldCount < 0) {
            this.addError("customFieldsCount", "Please specify a non-negative integer for number of custom fields to generate");
        }
        if (this.context.generatorConfiguration.customFields.freeText.wikiRendererProbability < 0.0f) {
            this.addError("wikiRendererProbability", "Please specify a positive float for Wiki renderer generation probability");
        }
        else if (this.context.generatorConfiguration.customFields.freeText.wikiRendererProbability > 1.0f) {
            this.addError("wikiRendererProbability", "Please specify a positive float not greater than 1.0 for Wiki renderer generation probability");
        }
        if (customFields.customFieldsPerScreen < 0) {
            this.addError("customFieldsPerScreen", "Please specify a non-negative integer for number of custom fields to assign per screen");
        }
        else if (customFields.screenSchemes > 0 && customFields.customFieldsPerScreen > customFields.fieldCount) {
            this.addError("customFieldsPerScreen", "Please specify a value that is no larger than the number of custom fields to generate");
        }
        if (customFields.screenSchemes < 0) {
            this.addError("screenSchemes", "Please specify a non-negative integer for number of screens");
        }
        else if (customFields.screenSchemes > this.context.generatorConfiguration.projectCount) {
            this.addError("screenSchemes", "Please specify a value that is no larger than the number of projects to generate");
        }
        if (customFields.screenSchemes == 0 && customFields.restrictByFieldConfig) {
            this.addError("restrictByFieldConfig", "We can only create Field Configurations when there is at least one screen created");
        }
        if (customFields.screenSchemes == 0 && !customFields.useGlobalCustomFieldContext) {
            this.addError("useGlobalCustomFieldContext", "We can only restrict Custom Field Contexts when there is at least one screen created");
        }
        if (customFields.singleSelect.avgOptionsCount < 0) {
            this.addError("averageSingleSelectOptionsCount", "Please specify a non-negative integer for number of Single Select options");
        }
        if (customFields.multiSelect.avgOptionsCount < 0) {
            this.addError("averageMultiSelectOptionsCount", "Please specify a non-negative integer for number of Multi Select options");
        }
        if (customFields.fieldCount > 0 && !this.isAnyCustomFieldTypeSelected()) {
            this.addError("customFieldsCount", "Please enable at least one type of custom field or specify 0 as number of custom fields per issue type");
        }
        if (this.context.generatorConfiguration.issueTypes.standardCount <= 0) {
            final Collection<IssueType> issueTypesForDefaultScheme = this.issueTypeSchemeManager.getIssueTypesForDefaultScheme();
            if (issueTypesForDefaultScheme.size() == 0) {
                this.addError("standardIssueTypeCount", "Please specify a value greater than zero for number of standard issue types to generate.");
            }
        }
        if (this.context.generatorConfiguration.additionalResolutionsCount < 0) {
            this.addError("resolutionCount", "Please specify a non-negative integer for number of resolutions to generate.");
        }
        if (this.context.generatorConfiguration.subtaskTypeCount < 0) {
            this.addError("subtaskTypeCount", "Please specify a non-negative integer for number of subtask types to generate");
        }
        for (final MetadataPluginConfigurationValidationData validationData : this.metadataPluginConfigurationAccessor.validateModules(this.context.generatorConfiguration)) {
            final ErrorCollection errors = validationData.getErrorCollection();
            if (errors != null) {
                if (!errors.hasAnyErrors()) {
                    continue;
                }
                for (final String errorMessage : Optional.of((List<Object>)errors.getErrorMessages()).orElse(Collections.emptyList())) {
                    this.addErrorMessage(errorMessage);
                }
                for (final Map.Entry<String, String> error : Optional.of(errors.getErrors()).orElse(Collections.emptyMap()).entrySet()) {
                    this.addError((String)error.getKey(), (String)error.getValue());
                }
            }
            else {
                this.addErrorMessage("No validation result performed on plugin " + validationData.getPluginName());
            }
        }
    }
    
    private boolean isAnyCustomFieldTypeSelected() {
        final CustomFields customFieldsConfiguration = this.context.generatorConfiguration.customFields;
        return customFieldsConfiguration.numericEnabled || customFieldsConfiguration.dateTimeEnabled || customFieldsConfiguration.freeText.enabled || customFieldsConfiguration.singleSelect.enabled || customFieldsConfiguration.multiSelect.enabled || customFieldsConfiguration.singleUserPickerEnabled || customFieldsConfiguration.multiUserPickerEnabled;
    }
    
    private void updateConfigurationBooleanValues() {
        this.context.generatorConfiguration.customFields.numericEnabled = this.numericCustomFieldEnabledValue;
        this.context.generatorConfiguration.customFields.dateTimeEnabled = this.dateTimeCustomFieldEnabledValue;
        this.context.generatorConfiguration.customFields.freeText.enabled = this.freeTextCustomFieldEnabledValue;
        this.context.generatorConfiguration.customFields.singleSelect.enabled = this.singleSelectCustomFieldEnabledValue;
        this.context.generatorConfiguration.customFields.multiSelect.enabled = this.multiSelectCustomFieldEnabledValue;
        this.context.generatorConfiguration.customFields.singleUserPickerEnabled = this.singleUserPickerCustomFieldEnabledValue;
        this.context.generatorConfiguration.customFields.multiUserPickerEnabled = this.multiUserPickerCustomFieldEnabledValue;
        this.context.generatorConfiguration.customFields.restrictByFieldConfig = this.restrictByFieldConfigValue;
        this.context.generatorConfiguration.customFields.useGlobalCustomFieldContext = this.useGlobalCustomFieldContextValue;
    }
    
    private void updatePluginConfigurationsFromRequest() {
        final ImmutableMap.Builder<String, Collection<String>> parametersMapBuilder = (ImmutableMap.Builder<String, Collection<String>>)ImmutableMap.builder();
        final Enumeration<String> enumeration = this.getHttpRequest().getParameterNames();
        while (enumeration.hasMoreElements()) {
            final String element = enumeration.nextElement();
            final String[] paramValues = this.getHttpRequest().getParameterValues(element);
            final List<String> values = Lists.newArrayListWithCapacity(1);
            if (paramValues != null) {
                values.addAll(Arrays.asList(paramValues));
            }
            parametersMapBuilder.put((Object)element, (Object)ImmutableList.copyOf((Collection)values));
        }
        this.updatePluginConfiguration((Map<String, Collection<String>>)parametersMapBuilder.build());
        this.pluginDefaultValuesSet.set(true);
    }
    
    private void updatePluginConfigurationDefaultValues() {
        if (!this.pluginDefaultValuesSet.getAndSet(true)) {
            final ImmutableMap.Builder<String, Collection<String>> builder = (ImmutableMap.Builder<String, Collection<String>>)ImmutableMap.builder();
            for (final MetadataPluginConfigurationFormData pluginFormConfiguration : this.metadataPluginConfigurationAccessor.getFormGenerationValues()) {
                for (final ConfigurationField configurationField : pluginFormConfiguration.getConfigurationFields()) {
                    final String key = configurationField.getId();
                    final String value = configurationField.getDefaultValue();
                    builder.put((Object)key, (Object)Collections.singleton(value));
                }
            }
            this.updatePluginConfiguration((Map<String, Collection<String>>)builder.build());
        }
    }
    
    private void updatePluginConfiguration(final Map<String, Collection<String>> parametersMap) {
        for (final MetadataPluginConfigurationFormData pluginFormConfiguration : this.metadataPluginConfigurationAccessor.getFormGenerationValues()) {
            for (final ConfigurationField configurationField : pluginFormConfiguration.getConfigurationFields()) {
                final String key = configurationField.getId();
                String firstValue;
                if (parametersMap.containsKey(key)) {
                    firstValue = (String)Iterables.getFirst((Iterable)parametersMap.get(key), (Object)"");
                }
                else {
                    firstValue = "";
                }
                final Object value = configurationField.getFieldType().parseValue(firstValue);
                this.context.generatorConfiguration.pluginConfiguration.moduleConfiguration.put(key, value);
            }
        }
    }
    
    @RequiresXsrfCheck
    @SupportedMethods({ RequestMethod.POST })
    public String doExecute() throws Exception {
        if (!this.hasGlobalPermission(GlobalPermissionKey.SYSTEM_ADMIN)) {
            return "permissionviolation";
        }
        this.validate();
        this.context.userName = this.getLoggedInUser().getName();
        if (!this.dataGeneratorDriver.scheduleMetadataGeneration(this.context)) {
            this.addErrorMessage("Cannot schedule generation task. Is there active task running? Please reopen generation form to see if there is generation in progress or wait few seconds and try again.");
            return "input";
        }
        return this.getRedirect("JiraMetadataGenerator.jspa");
    }
    
    @ActionViewData
    public Map<String, Object> getSoyData() {
        final ImmutableMap.Builder<String, Object> mapBuilder = (ImmutableMap.Builder<String, Object>)ImmutableMap.builder();
        try {
            return (Map<String, Object>)mapBuilder.putAll((Map)this.getGeneralConfigurationParams()).putAll((Map)this.getUsersAndGroupsParams()).putAll((Map)this.getSecurityAndPermissionsParams()).putAll((Map)this.getCustomFieldConfigurationParams()).putAll((Map)this.getPluginsParams()).put((Object)"errors", (Object)this.getErrorsMap()).put((Object)"xsrfToken", (Object)this.getXsrfToken()).build();
        }
        catch (final Exception e) {
            JiraMetadataGenerator.LOG.error("Error when creating soy data", (Throwable)e);
            throw e;
        }
    }
    
    private Map<String, Object> getGeneralConfigurationParams() {
        final ImmutableMap.Builder<String, Object> mapBuilder = (ImmutableMap.Builder<String, Object>)ImmutableMap.builder();
        return (Map<String, Object>)mapBuilder.put((Object)"projectCount", (Object)Integer.toString(this.context.generatorConfiguration.projectCount)).put((Object)"creatingFromTemplatesAvailable", (Object)this.dataGeneratorDriver.isCreatingFromTemplatesAvailable()).put((Object)"projectTemplate", (Object)this.emptyWhenNull(this.context.generatorConfiguration.projectTemplate)).put((Object)"workflowCount", (Object)Integer.toString(this.context.generatorConfiguration.workflows.count)).put((Object)"additionalStatusesCount", (Object)Integer.toString(this.context.generatorConfiguration.additionalStatusesCount)).put((Object)"standardIssueTypeCount", (Object)Integer.toString(this.context.generatorConfiguration.issueTypes.standardCount)).put((Object)"subtaskTypeCount", (Object)Integer.toString(this.context.generatorConfiguration.subtaskTypeCount)).put((Object)"resolutionCount", (Object)Integer.toString(this.context.generatorConfiguration.additionalResolutionsCount)).build();
    }
    
    private String emptyWhenNull(final String string) {
        return (string == null) ? "" : string;
    }
    
    private Map<String, Object> getUsersAndGroupsParams() {
        final ImmutableMap.Builder<String, Object> mapBuilder = (ImmutableMap.Builder<String, Object>)ImmutableMap.builder();
        return (Map<String, Object>)mapBuilder.put((Object)"groupsCount", (Object)Integer.toString(this.context.generatorConfiguration.groupsCount)).put((Object)"developersCount", (Object)Integer.toString(this.context.generatorConfiguration.developersCount)).put((Object)"usersCount", (Object)Integer.toString(this.context.generatorConfiguration.usersCount)).put((Object)"rolesCount", (Object)Integer.toString(this.context.generatorConfiguration.rolesCount)).put((Object)"assignRolesCount", (Object)Integer.toString(this.context.generatorConfiguration.assignRolesCount)).build();
    }
    
    private Map<String, Object> getSecurityAndPermissionsParams() {
        final ImmutableMap.Builder<String, Object> mapBuilder = (ImmutableMap.Builder<String, Object>)ImmutableMap.builder();
        return (Map<String, Object>)mapBuilder.put((Object)"permissionSchemesCount", (Object)Integer.toString(this.context.generatorConfiguration.permissionSchemes.schemesCount)).put((Object)"issueSecuritySchemesCount", (Object)Integer.toString(this.context.generatorConfiguration.issueSecuritySchemes.schemesCount)).put((Object)"issueSecurityLevelsCount", (Object)Integer.toString(this.context.generatorConfiguration.issueSecuritySchemes.levelsPerScheme)).put((Object)"issueSecurityPermissionCount", (Object)Integer.toString(this.context.generatorConfiguration.issueSecuritySchemes.permissionsPerLevel)).build();
    }
    
    private Map<String, Object> getCustomFieldConfigurationParams() {
        final ImmutableMap.Builder<String, Object> mapBuilder = (ImmutableMap.Builder<String, Object>)ImmutableMap.builder();
        return (Map<String, Object>)mapBuilder.put((Object)"customFieldsCount", (Object)Integer.toString(this.context.generatorConfiguration.customFields.fieldCount)).put((Object)"numericCustomFieldEnabled", (Object)this.context.generatorConfiguration.customFields.numericEnabled).put((Object)"dateTimeCustomFieldEnabled", (Object)this.context.generatorConfiguration.customFields.dateTimeEnabled).put((Object)"freeTextCustomFieldEnabled", (Object)this.context.generatorConfiguration.customFields.freeText.enabled).put((Object)"wikiRendererProbability", (Object)Float.toString(this.context.generatorConfiguration.customFields.freeText.wikiRendererProbability)).put((Object)"singleSelectCustomFieldEnabled", (Object)this.context.generatorConfiguration.customFields.singleSelect.enabled).put((Object)"averageSingleSelectOptionsCount", (Object)Integer.toString(this.context.generatorConfiguration.customFields.singleSelect.avgOptionsCount)).put((Object)"multiSelectCustomFieldEnabled", (Object)this.context.generatorConfiguration.customFields.multiSelect.enabled).put((Object)"averageMultiSelectOptionsCount", (Object)Integer.toString(this.context.generatorConfiguration.customFields.multiSelect.avgOptionsCount)).put((Object)"singleUserPickerCustomFieldEnabled", (Object)this.context.generatorConfiguration.customFields.singleUserPickerEnabled).put((Object)"multiUserPickerCustomFieldEnabled", (Object)this.context.generatorConfiguration.customFields.multiUserPickerEnabled).put((Object)"screenSchemes", (Object)Integer.toString(this.context.generatorConfiguration.customFields.screenSchemes)).put((Object)"restrictByFieldConfig", (Object)this.context.generatorConfiguration.customFields.restrictByFieldConfig).put((Object)"customFieldsPerScreen", (Object)Integer.toString(this.context.generatorConfiguration.customFields.customFieldsPerScreen)).put((Object)"useGlobalCustomFieldContext", (Object)this.context.generatorConfiguration.customFields.useGlobalCustomFieldContext).build();
    }
    
    private Map<String, Object> getPluginsParams() {
        final ImmutableMap.Builder<String, Object> mapBuilder = (ImmutableMap.Builder<String, Object>)ImmutableMap.builder();
        return (Map<String, Object>)mapBuilder.put((Object)"pluginModuleValues", (Object)this.getAllPluginModuleConfigurationValues()).put((Object)"metadataFormGenerators", (Object)this.getEnabledMetadataPluginFormConfigurations()).build();
    }
    
    public int getSubtaskTypeCount() {
        return this.context.generatorConfiguration.subtaskTypeCount;
    }
    
    public int getProjectCount() {
        return this.context.generatorConfiguration.projectCount;
    }
    
    public int getGroupsCount() {
        return this.context.generatorConfiguration.groupsCount;
    }
    
    public int getRolesCount() {
        return this.context.generatorConfiguration.rolesCount;
    }
    
    public int getAssignRolesCount() {
        return this.context.generatorConfiguration.assignRolesCount;
    }
    
    public int getUsersCount() {
        return this.context.generatorConfiguration.usersCount;
    }
    
    public int getDevelopersCount() {
        return this.context.generatorConfiguration.developersCount;
    }
    
    public int getAdditionalStatusesCount() {
        return this.context.generatorConfiguration.additionalStatusesCount;
    }
    
    public int getCustomFieldsCount() {
        return this.context.generatorConfiguration.customFields.fieldCount;
    }
    
    public int getScreenSchemes() {
        return this.context.generatorConfiguration.customFields.screenSchemes;
    }
    
    public boolean isRestrictByFieldConfig() {
        return this.context.generatorConfiguration.customFields.restrictByFieldConfig;
    }
    
    public int getCustomFieldsPerScreen() {
        return this.context.generatorConfiguration.customFields.customFieldsPerScreen;
    }
    
    public boolean isUseGlobalCustomFieldContext() {
        return this.context.generatorConfiguration.customFields.useGlobalCustomFieldContext;
    }
    
    public boolean isNumericCustomFieldEnabled() {
        return this.context.generatorConfiguration.customFields.numericEnabled;
    }
    
    public boolean isDateTimeCustomFieldEnabled() {
        return this.context.generatorConfiguration.customFields.dateTimeEnabled;
    }
    
    public boolean isFreeTextCustomFieldEnabled() {
        return this.context.generatorConfiguration.customFields.freeText.enabled;
    }
    
    public float getWikiRendererProbability() {
        return this.context.generatorConfiguration.customFields.freeText.wikiRendererProbability;
    }
    
    public boolean isSingleSelectCustomFieldEnabled() {
        return this.context.generatorConfiguration.customFields.singleSelect.enabled;
    }
    
    public boolean isMultiSelectCustomFieldEnabled() {
        return this.context.generatorConfiguration.customFields.multiSelect.enabled;
    }
    
    public int getAverageSingleSelectOptionsCount() {
        return this.context.generatorConfiguration.customFields.singleSelect.avgOptionsCount;
    }
    
    public int getAverageMultiSelectOptionsCount() {
        return this.context.generatorConfiguration.customFields.multiSelect.avgOptionsCount;
    }
    
    public boolean isSingleUserPickerCustomFieldEnabled() {
        return this.context.generatorConfiguration.customFields.singleUserPickerEnabled;
    }
    
    public boolean isMultiUserPickerCustomFieldEnabled() {
        return this.context.generatorConfiguration.customFields.multiUserPickerEnabled;
    }
    
    public int getWorkflowCount() {
        return this.context.generatorConfiguration.workflows.count;
    }
    
    public int getResolutionCount() {
        return this.context.generatorConfiguration.additionalResolutionsCount;
    }
    
    public int getStandardIssueTypeCount() {
        return this.context.generatorConfiguration.issueTypes.standardCount;
    }
    
    public int getIssueSecuritySchemesCount() {
        return this.context.generatorConfiguration.issueSecuritySchemes.schemesCount;
    }
    
    public int getIssueSecurityLevelsCount() {
        return this.context.generatorConfiguration.issueSecuritySchemes.levelsPerScheme;
    }
    
    public int getIssueSecurityPermissionCount() {
        return this.context.generatorConfiguration.issueSecuritySchemes.permissionsPerLevel;
    }
    
    public int getPermissionSchemesCount() {
        return this.context.generatorConfiguration.permissionSchemes.schemesCount;
    }
    
    public void setWorkflowCount(final int workflowCount) {
        this.context.generatorConfiguration.workflows.count = workflowCount;
    }
    
    public void setProjectCount(final int projectCount) {
        this.context.generatorConfiguration.projectCount = projectCount;
    }
    
    public void setProjectTemplate(final String projectTemplate) {
        this.context.generatorConfiguration.projectTemplate = projectTemplate;
    }
    
    public void setGroupsCount(final int groupsCount) {
        this.context.generatorConfiguration.groupsCount = groupsCount;
    }
    
    public void setRolesCount(final int rolesCount) {
        this.context.generatorConfiguration.rolesCount = rolesCount;
    }
    
    public void setAssignRolesCount(final int assignRolesCount) {
        this.context.generatorConfiguration.assignRolesCount = assignRolesCount;
    }
    
    public void setUsersCount(final int usersCount) {
        this.context.generatorConfiguration.usersCount = usersCount;
    }
    
    public void setDevelopersCount(final int developersCount) {
        this.context.generatorConfiguration.developersCount = developersCount;
    }
    
    public void setAdditionalStatusesCount(final int additionalStatusesCount) {
        this.context.generatorConfiguration.additionalStatusesCount = additionalStatusesCount;
    }
    
    public void setCustomFieldsCount(final int customFieldsCount) {
        this.context.generatorConfiguration.customFields.fieldCount = customFieldsCount;
    }
    
    public void setScreenSchemes(final int screenSchemes) {
        this.context.generatorConfiguration.customFields.screenSchemes = screenSchemes;
    }
    
    public void setRestrictByFieldConfig(final boolean restrictByFieldConfig) {
        this.restrictByFieldConfigValue = restrictByFieldConfig;
    }
    
    public void setCustomFieldsPerScreen(final int customFieldsPerScreen) {
        this.context.generatorConfiguration.customFields.customFieldsPerScreen = customFieldsPerScreen;
    }
    
    public void setUseGlobalCustomFieldContext(final boolean useGlobalCustomFieldContext) {
        this.useGlobalCustomFieldContextValue = useGlobalCustomFieldContext;
    }
    
    public void setNumericCustomFieldEnabled(final boolean isNumericCustomFieldEnabled) {
        this.numericCustomFieldEnabledValue = isNumericCustomFieldEnabled;
    }
    
    public void setDateTimeCustomFieldEnabled(final boolean isDateTimeCustomFieldEnabled) {
        this.dateTimeCustomFieldEnabledValue = isDateTimeCustomFieldEnabled;
    }
    
    public void setFreeTextCustomFieldEnabled(final boolean isFreeTextCustomFieldEnabled) {
        this.freeTextCustomFieldEnabledValue = isFreeTextCustomFieldEnabled;
    }
    
    public void setWikiRendererProbability(final float wikiRendererProbability) {
        this.context.generatorConfiguration.customFields.freeText.wikiRendererProbability = wikiRendererProbability;
    }
    
    public void setSingleSelectCustomFieldEnabled(final boolean isSingleSelectCustomFieldEnabled) {
        this.singleSelectCustomFieldEnabledValue = isSingleSelectCustomFieldEnabled;
    }
    
    public void setMultiSelectCustomFieldEnabled(final boolean isMultiSelectCustomFieldEnabled) {
        this.multiSelectCustomFieldEnabledValue = isMultiSelectCustomFieldEnabled;
    }
    
    public void setSingleUserPickerCustomFieldEnabled(final boolean singleUserPickerCustomFieldEnabled) {
        this.singleUserPickerCustomFieldEnabledValue = singleUserPickerCustomFieldEnabled;
    }
    
    public void setMultiUserPickerCustomFieldEnabled(final boolean multiUserPickerCustomFieldEnabled) {
        this.multiUserPickerCustomFieldEnabledValue = multiUserPickerCustomFieldEnabled;
    }
    
    public void setAverageSingleSelectOptionsCount(final int averageSingleSelectOptionsCount) {
        this.context.generatorConfiguration.customFields.singleSelect.avgOptionsCount = averageSingleSelectOptionsCount;
    }
    
    public void setAverageMultiSelectOptionsCount(final int averageMultiSelectOptionsCount) {
        this.context.generatorConfiguration.customFields.multiSelect.avgOptionsCount = averageMultiSelectOptionsCount;
    }
    
    public void setStandardIssueTypeCount(final int standardIssueTypeCount) {
        this.context.generatorConfiguration.issueTypes.standardCount = standardIssueTypeCount;
    }
    
    public void setPermissionSchemesCount(final int count) {
        this.context.generatorConfiguration.permissionSchemes.schemesCount = count;
    }
    
    public void setResolutionCount(final int resolutionCount) {
        this.context.generatorConfiguration.additionalResolutionsCount = resolutionCount;
    }
    
    public void setSubtaskTypeCount(final int subtaskTypeCount) {
        this.context.generatorConfiguration.subtaskTypeCount = subtaskTypeCount;
    }
    
    public void setIssueSecuritySchemesCount(final int schemesCount) {
        this.context.generatorConfiguration.issueSecuritySchemes.schemesCount = schemesCount;
    }
    
    public void setIssueSecurityLevelsCount(final int levelCount) {
        this.context.generatorConfiguration.issueSecuritySchemes.levelsPerScheme = levelCount;
    }
    
    public void setIssueSecurityPermissionCount(final int permissionCount) {
        this.context.generatorConfiguration.issueSecuritySchemes.permissionsPerLevel = permissionCount;
    }
    
    public boolean checkPluginEnabled(final String key) {
        return this.enabledPlugins.get(key) == Boolean.TRUE;
    }
    
    public List<MetadataPluginConfigurationFormData> getAllMetadataPluginFormConfigurations() {
        return (List<MetadataPluginConfigurationFormData>)ImmutableList.copyOf((Collection)this.metadataPluginConfigurationAccessor.getFormGenerationValues());
    }
    
    public List<MetadataPluginConfigurationFormData> getEnabledMetadataPluginFormConfigurations() {
        return this.metadataPluginConfigurationAccessor.getFormGenerationValues().stream().filter(pluginConfig -> this.enabledPlugins.get(pluginConfig.getPluginKey()) == Boolean.TRUE).collect((Collector<? super Object, Object, List<MetadataPluginConfigurationFormData>>)Collectors.collectingAndThen((Collector<? super Object, A, Collection<? super Object>>)Collectors.toList(), (Function<Collection<? super Object>, R>)ImmutableList::copyOf));
    }
    
    public Map<String, Object> getAllPluginModuleConfigurationValues() {
        this.updatePluginConfigurationDefaultValues();
        return (Map<String, Object>)ImmutableMap.copyOf((Map)this.context.generatorConfiguration.pluginConfiguration.moduleConfiguration);
    }
    
    public boolean isCreatingFromTemplatesAvailable() {
        return this.dataGeneratorDriver.isCreatingFromTemplatesAvailable();
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)JiraMetadataGenerator.class);
    }
}
