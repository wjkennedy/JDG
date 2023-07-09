// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.drivers;

import org.slf4j.LoggerFactory;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import java.util.ListIterator;
import com.atlassian.jira.plugins.datagenerator.db.ChangeGroupEntity;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.plugins.datagenerator.db.ChangeItemEntity;
import com.atlassian.jira.issue.history.ChangeItemBean;
import org.apache.commons.lang.StringUtils;
import java.sql.Timestamp;
import java.util.Set;
import com.atlassian.jira.plugins.datagenerator.timestamp.TimestampGenerator;
import com.atlassian.jira.plugins.datagenerator.util.Randomizer;
import com.google.common.collect.Sets;
import com.atlassian.jira.plugins.datagenerator.timestamp.FixedIntervalEndlessGenerator;
import com.atlassian.jira.plugins.datagenerator.config.module.IssueCreationParameters;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.plugins.datagenerator.customfields.FieldValueGenerator;
import com.atlassian.jira.issue.fields.CustomField;
import com.google.common.collect.Lists;
import java.util.Collections;
import com.atlassian.jira.plugins.datagenerator.IssueInfo;
import java.util.Iterator;
import com.atlassian.jira.project.Project;
import java.sql.SQLException;
import org.ofbiz.core.entity.GenericEntityException;
import java.util.Collection;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import com.atlassian.jira.plugins.datagenerator.db.EntityHandler;
import com.atlassian.jira.plugins.datagenerator.db.EntityManager;
import com.atlassian.jira.plugins.datagenerator.CustomFieldInfo;
import java.util.List;
import java.util.Map;
import com.atlassian.jira.plugins.datagenerator.fields.FieldLayoutHelper;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.plugins.datagenerator.generators.CustomFieldGenerator;
import com.atlassian.jira.plugins.datagenerator.config.CustomFields;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.slf4j.Logger;

public class CustomFieldDriver
{
    private static final Logger LOG;
    private final GeneratorContext context;
    private final CustomFields customFieldsConfig;
    private final CustomFieldGenerator customFieldGenerator;
    private final CustomFieldManager customFieldManager;
    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final FieldLayoutHelper fieldLayoutHelper;
    private final Map<String, List<CustomFieldInfo>> fieldCache;
    private final List<String> assigneeNames;
    private final EntityManager entityManager;
    private EntityHandler changeGroupHandler;
    private EntityHandler changeItemHandler;
    private EntityHandler customFieldValueHandler;
    
    public CustomFieldDriver(final GeneratorContext context, final EntityManager entityManager, final CustomFieldGenerator customFieldGenerator, final CustomFieldManager customFieldManager, final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, final FieldLayoutHelper fieldLayoutHelper) throws GenericEntityException, SQLException {
        this.fieldCache = new HashMap<String, List<CustomFieldInfo>>(16);
        this.context = context;
        this.customFieldGenerator = customFieldGenerator;
        this.customFieldsConfig = context.generatorConfiguration.customFields;
        this.customFieldManager = customFieldManager;
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        this.fieldLayoutHelper = fieldLayoutHelper;
        this.assigneeNames = (List<String>)ImmutableList.copyOf((Collection)context.assignees.keySet());
        this.entityManager = entityManager;
        this.changeGroupHandler = entityManager.getEntityHandler("ChangeGroup");
        this.changeItemHandler = entityManager.getEntityHandler("ChangeItem");
        this.customFieldValueHandler = entityManager.getEntityHandler("CustomFieldValue");
    }
    
    private void warnForIssueTypesWithNoCustomFields(final Project project) {
        final StringBuilder sb = new StringBuilder(128);
        for (final Map.Entry<String, List<CustomFieldInfo>> entry : this.fieldCache.entrySet()) {
            if (entry.getValue().isEmpty()) {
                sb.append(entry.getKey()).append(", ");
            }
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
            CustomFieldDriver.LOG.warn("No custom fields in project " + project.getKey() + "'; issue types: " + (Object)sb);
        }
    }
    
    private List<CustomFieldInfo> getFieldList(final Project project, final IssueInfo issue) throws GenericEntityException {
        final String issueType = issue.issueType.getName();
        List<CustomFieldInfo> list = this.fieldCache.get(issueType);
        if (list != null) {
            return list;
        }
        final boolean debug = CustomFieldDriver.LOG.isDebugEnabled();
        if (debug) {
            CustomFieldDriver.LOG.debug("Scanning custom fields for project " + project.getKey() + ", issue type '" + issueType + "'...");
        }
        final List<String> issueTypeAsList = Collections.singletonList(issueType);
        list = Lists.newArrayList();
        for (final CustomField customField : this.customFieldManager.getCustomFieldObjects()) {
            this.getCustomFieldsForIssue(project, issue, list, debug, issueTypeAsList, customField);
        }
        if (debug) {
            CustomFieldDriver.LOG.debug("Found " + list.size() + " custom field(s) we can use: " + list);
        }
        this.fieldCache.put(issueType, list);
        return list;
    }
    
    private void getCustomFieldsForIssue(final Project project, final IssueInfo issue, final List<CustomFieldInfo> list, final boolean debug, final List<String> issueTypeAsList, final CustomField customField) {
        if (!customField.isEnabled()) {
            if (debug) {
                CustomFieldDriver.LOG.debug("Ignoring custom field '" + customField.getName() + ": not enabled");
            }
            return;
        }
        if (!customField.isEditable()) {
            if (debug) {
                CustomFieldDriver.LOG.debug("Ignoring custom field '" + customField.getName() + "': not editable");
            }
            return;
        }
        if (!this.isVisibleField(customField, issue)) {
            if (debug) {
                CustomFieldDriver.LOG.debug("Ignoring custom field '" + customField.getName() + "': not visible");
            }
            return;
        }
        if (customField.isGlobal() || customField.isInScope(project, (List)issueTypeAsList)) {
            final FieldValueGenerator.Factory factory = this.customFieldGenerator.getFieldValueGeneratorFactory(customField);
            if (factory != null) {
                final CustomFieldInfo customFieldInfo = new CustomFieldInfo(customField);
                customFieldInfo.valueGenerator = factory.create(this.context);
                list.add(customFieldInfo);
            }
            else if (debug) {
                CustomFieldDriver.LOG.debug("Ignoring custom field '" + customField.getName() + "': no value generator for type '" + customField.getCustomFieldType().getKey() + '\'');
            }
        }
        else if (debug) {
            CustomFieldDriver.LOG.debug("Ignoring custom field '" + customField.getName() + "': not in scope");
        }
    }
    
    private boolean isVisibleField(final CustomField customField, final IssueInfo issue) {
        final FieldLayout fieldLayout = this.fieldLayoutHelper.getFieldLayout(issue.getProjectObject(), issue.getIssueTypeObject().getId());
        if (fieldLayout.isFieldHidden(customField.getId())) {
            return false;
        }
        final IssueTypeScreenScheme issueTypeScreenScheme = this.issueTypeScreenSchemeManager.getIssueTypeScreenScheme(issue.getProject());
        if (issueTypeScreenScheme == null) {
            return false;
        }
        IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = issueTypeScreenScheme.getEntity(issue.getIssueTypeObject().getId());
        if (issueTypeScreenSchemeEntity == null) {
            issueTypeScreenSchemeEntity = issueTypeScreenScheme.getEntity((String)null);
        }
        final FieldScreen defaultScreen = issueTypeScreenSchemeEntity.getFieldScreenScheme().getFieldScreen((IssueOperation)null);
        return defaultScreen.containsField(customField.getId());
    }
    
    public void generate(final Project project, final List<IssueInfo> issueList, final IssueCreationParameters issueCreationParameters) throws SQLException, GenericEntityException {
        try {
            if (CustomFieldDriver.LOG.isDebugEnabled()) {
                CustomFieldDriver.LOG.debug("Generating custom fields for project " + project.getKey());
            }
            this.fieldCache.clear();
            for (int i = 0; i < issueList.size(); ++i) {
                final IssueInfo issue = issueList.get(i);
                this.generateForIssue(project, issue, this.changeGroupHandler, this.changeItemHandler, this.customFieldValueHandler, issueCreationParameters);
                if ((i + 1) % 10000 == 0) {
                    CustomFieldDriver.LOG.info(String.format("Created %d / %d custom fields", i, issueList.size()));
                }
            }
        }
        finally {
            this.warnForIssueTypesWithNoCustomFields(project);
        }
    }
    
    private void generateForIssue(final Project project, final IssueInfo issue, final EntityHandler changeGroupHandler, final EntityHandler changeItemHandler, final EntityHandler customFieldValueHandler, final IssueCreationParameters issueCreationParameters) throws SQLException, GenericEntityException {
        final boolean pluginRequiresCustomFields = issueCreationParameters != null && !issueCreationParameters.getCustomFieldsToPopulate().isEmpty();
        final TimestampGenerator timestampGenerator = new FixedIntervalEndlessGenerator(issue.created, this.customFieldsConfig.avgInterval);
        final List<CustomFieldInfo> availableFields = this.getFieldList(project, issue);
        if (availableFields.isEmpty()) {
            if (pluginRequiresCustomFields) {
                CustomFieldDriver.LOG.warn("Plugin have required custom fields to populate, but there are no fields available!");
            }
            return;
        }
        final boolean debug = CustomFieldDriver.LOG.isDebugEnabled();
        if (debug) {
            CustomFieldDriver.LOG.debug("Generating custom field changes for issue id = " + issue.id);
        }
        final Set<CustomFieldInfo> additionalPluginFields = Sets.newHashSet();
        if (pluginRequiresCustomFields) {
            final Iterator<CustomFieldInfo> customFieldInfoIterator = availableFields.iterator();
            while (customFieldInfoIterator.hasNext()) {
                final CustomFieldInfo customFieldInfo = customFieldInfoIterator.next();
                if (issueCreationParameters.getCustomFieldsToPopulate().contains(customFieldInfo.customField)) {
                    additionalPluginFields.add(customFieldInfo);
                    customFieldInfoIterator.remove();
                }
            }
        }
        final List<CustomFieldInfo> fieldsToChange = Randomizer.randomItems(this.customFieldsConfig.changesPerEdit, availableFields);
        this.changeCustomFields(fieldsToChange, issue, this.customFieldsConfig.avgEdits, timestampGenerator, changeGroupHandler, changeItemHandler, customFieldValueHandler, issueCreationParameters);
        this.changeCustomFields(additionalPluginFields, issue, Math.max(1, this.customFieldsConfig.avgEdits), timestampGenerator, changeGroupHandler, changeItemHandler, customFieldValueHandler, issueCreationParameters);
    }
    
    private void changeCustomFields(final Collection<CustomFieldInfo> fieldsToChange, final IssueInfo issue, final int numberEdits, final TimestampGenerator timestampGenerator, final EntityHandler changeGroupHandler, final EntityHandler changeItemHandler, final EntityHandler customFieldValueHandler, final IssueCreationParameters issueCreationParameters) throws SQLException {
        final List<Object> previousValues = Lists.newArrayList(new Object[fieldsToChange.size()]);
        for (int i = 1; i <= numberEdits; ++i) {
            final Timestamp timestamp = timestampGenerator.next();
            final ChangeGroupEntity chgGroup = this.createNewChgGroupId(changeGroupHandler, issue.id, timestamp, issueCreationParameters);
            final ListIterator<Object> prevValuesIterator = previousValues.listIterator();
            for (final CustomFieldInfo customFieldInfo : fieldsToChange) {
                final CustomField customField = customFieldInfo.customField;
                try {
                    final Object prevVal = prevValuesIterator.next();
                    final Object newVal = customFieldInfo.valueGenerator.generate(issue, customFieldInfo);
                    prevValuesIterator.set(newVal);
                    final CustomFieldType customFieldType = customField.getCustomFieldType();
                    final String changelogOldString = (prevVal == null) ? null : customFieldType.getChangelogString(customField, prevVal);
                    final String changelogNewString = (newVal == null) ? null : customFieldType.getChangelogString(customField, newVal);
                    final String changelogOldValue = (prevVal == null) ? null : customFieldType.getChangelogValue(customField, prevVal);
                    final String changelogNewValue = (newVal == null) ? null : customFieldType.getChangelogValue(customField, newVal);
                    final String oldvalue = (changelogOldString == null) ? null : changelogOldValue;
                    final String newvalue = (changelogNewString == null) ? null : changelogNewValue;
                    final String oldstring = StringUtils.defaultString(changelogOldString, changelogOldValue);
                    final String newstring = StringUtils.defaultString(changelogNewString, changelogNewValue);
                    final ChangeItemBean cib = new ChangeItemBean("custom", customFieldInfo.name, oldvalue, oldstring, newvalue, newstring);
                    new ChangeItemEntity(chgGroup, cib).store(changeItemHandler);
                    final Object dbValue = customFieldInfo.valueGenerator.convertToDbValue(newVal);
                    if (i == this.customFieldsConfig.avgEdits) {
                        final Collection values = (dbValue instanceof Collection) ? ((Collection)dbValue) : Collections.singletonList(dbValue);
                        for (final Object value : values) {
                            final Map<String, Object> customFieldValue = EasyMap.build((Object)"id", (Object)customFieldValueHandler.getNextSequenceId(), (Object)"issue", (Object)issue.id, (Object)"customfield", (Object)customFieldInfo.id, (Object)customFieldInfo.valueGenerator.fieldType(), value);
                            customFieldValueHandler.store(customFieldValue);
                        }
                    }
                    else {
                        for (int reserveCount = (dbValue instanceof Collection) ? ((Collection)dbValue).size() : 1, r = 0; r < reserveCount; ++r) {
                            customFieldValueHandler.getNextSequenceId();
                        }
                    }
                }
                catch (final Exception ex) {
                    CustomFieldDriver.LOG.warn("Error generating change history for custom field " + customField + ": " + ex);
                }
            }
        }
    }
    
    private ChangeGroupEntity createNewChgGroupId(final EntityHandler changeGroupHandler, final Long issueId, final Timestamp timestamp, final IssueCreationParameters issueCreationParameters) throws SQLException {
        final ChangeGroupEntity changeGroupEntity = new ChangeGroupEntity().setAuthor(Randomizer.randomItem((issueCreationParameters == null || issueCreationParameters.getAssigneeUsernames().isEmpty()) ? this.assigneeNames : issueCreationParameters.getAssigneeUsernames())).setCreated(timestamp).setIssueId(issueId);
        changeGroupEntity.store(changeGroupHandler);
        return changeGroupEntity;
    }
    
    public void flush() throws SQLException, GenericEntityException {
        try {
            this.changeItemHandler.close();
        }
        catch (final SQLException e) {
            CustomFieldDriver.LOG.warn("Could not close changeItemHandler", (Throwable)e);
        }
        finally {
            this.changeItemHandler = this.entityManager.getEntityHandler("ChangeItem");
        }
        try {
            this.changeGroupHandler.close();
        }
        catch (final SQLException e) {
            CustomFieldDriver.LOG.warn("Could not close changeGroupHandler", (Throwable)e);
        }
        finally {
            this.changeGroupHandler = this.entityManager.getEntityHandler("ChangeGroup");
        }
        try {
            this.customFieldValueHandler.close();
        }
        catch (final SQLException e) {
            CustomFieldDriver.LOG.warn("Could not close changeItemHandler", (Throwable)e);
        }
        finally {
            this.customFieldValueHandler = this.entityManager.getEntityHandler("CustomFieldValue");
        }
    }
    
    public void close() throws SQLException {
        this.changeItemHandler.close();
        this.changeGroupHandler.close();
        this.customFieldValueHandler.close();
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)CustomFieldDriver.class);
    }
}
