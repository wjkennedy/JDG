// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators;

import org.slf4j.LoggerFactory;
import java.util.Collections;
import com.atlassian.jira.action.screen.AddFieldToScreenUtilImpl;
import com.atlassian.jira.component.pico.ComponentManager;
import com.atlassian.jira.action.screen.AddFieldToScreenUtil;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntityImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeImpl;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeImpl;
import com.atlassian.jira.issue.fields.CommentSystemField;
import com.atlassian.jira.issue.fields.screen.FieldScreenImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.plugins.datagenerator.model.FieldLayoutItemBuilder;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntityImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeImpl;
import java.util.Date;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayoutImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugins.datagenerator.model.FieldLayoutItemBuilderImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.plugins.datagenerator.model.ScreenInfo;
import com.atlassian.jira.project.Project;
import org.apache.commons.lang.time.StopWatch;
import com.atlassian.jira.plugins.datagenerator.CustomFieldInfo;
import java.util.List;
import com.atlassian.jira.plugins.datagenerator.FieldScreenDistribution;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.plugins.datagenerator.fields.FieldLayoutHelper;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class FieldConfigurationGenerator
{
    private static final Logger log;
    private final FieldLayoutHelper fieldLayoutHelper;
    private final FieldLayoutManager fieldLayoutManager;
    private final ConstantsManager constantsManager;
    private final FieldScreenManager fieldScreenManager;
    private final FieldScreenSchemeManager fieldScreenSchemeManager;
    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    
    @Autowired
    public FieldConfigurationGenerator(@ComponentImport final FieldLayoutManager fieldLayoutManager, @ComponentImport final ConstantsManager constantsManager, @ComponentImport final FieldScreenManager fieldScreenManager, @ComponentImport final FieldScreenSchemeManager fieldScreenSchemeManager, @ComponentImport final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager) {
        this.fieldLayoutManager = fieldLayoutManager;
        this.fieldLayoutHelper = new FieldLayoutHelper(fieldLayoutManager);
        this.constantsManager = constantsManager;
        this.fieldScreenManager = fieldScreenManager;
        this.fieldScreenSchemeManager = fieldScreenSchemeManager;
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
    }
    
    public void generate(final GeneratorContext context, final FieldScreenDistribution fieldScreenDistribution, final List<CustomFieldInfo> generatedCustomFields) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (final ScreenInfo screenInfo : fieldScreenDistribution.getAllScreens()) {
            final IssueTypeScreenScheme issueTypeScreenScheme = this.generateIssueTypeScreenScheme(screenInfo, generatedCustomFields);
            Long newFieldLayoutSchemeID = null;
            if (context.generatorConfiguration.customFields.restrictByFieldConfig) {
                newFieldLayoutSchemeID = this.generateFieldConfigScheme(screenInfo, generatedCustomFields);
            }
            final Collection<Project> relevantProjects = fieldScreenDistribution.getProjectsForScreen(screenInfo);
            for (final Project relevantProject : relevantProjects) {
                this.issueTypeScreenSchemeManager.addSchemeAssociation(relevantProject.getGenericValue(), issueTypeScreenScheme);
                if (context.generatorConfiguration.customFields.restrictByFieldConfig) {
                    this.fieldLayoutHelper.addSchemeAssociation(relevantProject.getGenericValue(), newFieldLayoutSchemeID);
                }
            }
        }
        stopWatch.stop();
        final String message = String.format("Generated %d screen configs in %s.", fieldScreenDistribution.getAllScreens().length, stopWatch.toString());
        context.messages.add(message);
    }
    
    private Long generateFieldConfigScheme(final ScreenInfo screenInfo, final List<CustomFieldInfo> generatedCustomFields) {
        final List<FieldLayoutItem> fieldLayoutItems = this.fieldLayoutHelper.getEditableDefaultFieldLayout().getFieldLayoutItems();
        final List<FieldLayoutItem> newFieldLayoutItems = new ArrayList<FieldLayoutItem>(fieldLayoutItems.size());
        for (final FieldLayoutItem fieldLayoutItem : fieldLayoutItems) {
            final FieldLayoutItemBuilder builder = new FieldLayoutItemBuilderImpl(fieldLayoutItem);
            if (fieldLayoutItem.getOrderableField() instanceof CustomField) {
                final CustomField cf = (CustomField)fieldLayoutItem.getOrderableField();
                builder.setHidden(!this.isVisible(cf, screenInfo, generatedCustomFields));
            }
            newFieldLayoutItems.add(builder.build());
        }
        final EditableFieldLayout editableFieldLayout = (EditableFieldLayout)new EditableFieldLayoutImpl((GenericValue)null, (List)newFieldLayoutItems);
        editableFieldLayout.setName("My Field Config " + screenInfo.getHumanReadableIndex());
        editableFieldLayout.setDescription("Generated by JDG " + new Date());
        this.fieldLayoutHelper.storeEditableFieldLayout(editableFieldLayout);
        final Long newFieldLayoutId = this.findNewFieldLayoutId();
        FieldLayoutScheme fieldLayoutScheme = (FieldLayoutScheme)new FieldLayoutSchemeImpl(this.fieldLayoutManager, (GenericValue)null);
        fieldLayoutScheme.setName("My Field Config Scheme " + screenInfo.getHumanReadableIndex());
        fieldLayoutScheme.setDescription("Generated by JDG " + new Date());
        fieldLayoutScheme = this.fieldLayoutManager.createFieldLayoutScheme(fieldLayoutScheme);
        final FieldLayoutSchemeEntity fieldLayoutSchemeEntity = (FieldLayoutSchemeEntity)new FieldLayoutSchemeEntityImpl(this.fieldLayoutManager, (GenericValue)null, this.constantsManager);
        fieldLayoutSchemeEntity.setIssueTypeId((String)null);
        fieldLayoutSchemeEntity.setFieldLayoutId(newFieldLayoutId);
        fieldLayoutScheme.removeEntity((String)null);
        fieldLayoutScheme.addEntity(fieldLayoutSchemeEntity);
        return fieldLayoutScheme.getId();
    }
    
    private Long findNewFieldLayoutId() {
        long maxId = 0L;
        for (final EditableFieldLayout editableFieldLayout : this.fieldLayoutHelper.getEditableFieldLayouts()) {
            final Long id = editableFieldLayout.getId();
            if (id != null && id > maxId) {
                maxId = id;
            }
        }
        return maxId;
    }
    
    private IssueTypeScreenScheme generateIssueTypeScreenScheme(final ScreenInfo screenInfo, final List<CustomFieldInfo> generatedCustomFields) {
        final FieldScreen fieldScreen = (FieldScreen)new FieldScreenImpl(this.fieldScreenManager);
        fieldScreen.setName("My Screen " + screenInfo.getHumanReadableIndex());
        fieldScreen.setDescription("Generated Screen");
        fieldScreen.store();
        fieldScreen.addTab("Field Tab");
        for (final FieldLayoutItem fieldLayoutItem : this.fieldLayoutHelper.getEditableDefaultFieldLayout().getFieldLayoutItems()) {
            final OrderableField field = fieldLayoutItem.getOrderableField();
            if (field instanceof CustomField) {
                if (!this.isVisible((CustomField)field, screenInfo, generatedCustomFields)) {
                    continue;
                }
                this.addFieldToScreen(fieldScreen, field);
            }
            else {
                if (field instanceof CommentSystemField) {
                    continue;
                }
                this.addFieldToScreen(fieldScreen, field);
            }
        }
        final FieldScreenScheme fieldScreenScheme = (FieldScreenScheme)new FieldScreenSchemeImpl(this.fieldScreenSchemeManager, (GenericValue)null);
        fieldScreenScheme.setName("My Screen Scheme " + screenInfo.getHumanReadableIndex());
        fieldScreenScheme.setDescription("Generated Screen Scheme");
        fieldScreenScheme.store();
        final FieldScreenSchemeItem fieldScreenSchemeItem = (FieldScreenSchemeItem)new FieldScreenSchemeItemImpl(this.fieldScreenSchemeManager, (GenericValue)null, this.fieldScreenManager);
        fieldScreenSchemeItem.setIssueOperation((ScreenableIssueOperation)null);
        fieldScreenSchemeItem.setFieldScreen(fieldScreen);
        fieldScreenScheme.addFieldScreenSchemeItem(fieldScreenSchemeItem);
        final IssueTypeScreenScheme issueTypeScreenScheme = (IssueTypeScreenScheme)new IssueTypeScreenSchemeImpl(this.issueTypeScreenSchemeManager, (GenericValue)null);
        issueTypeScreenScheme.setName("My Issue Type Screen Scheme " + screenInfo.getHumanReadableIndex());
        issueTypeScreenScheme.setDescription("Generated Issue Type Screen Scheme");
        issueTypeScreenScheme.store();
        final IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = (IssueTypeScreenSchemeEntity)new IssueTypeScreenSchemeEntityImpl(this.issueTypeScreenSchemeManager, (GenericValue)null, this.fieldScreenSchemeManager, this.constantsManager);
        issueTypeScreenSchemeEntity.setIssueTypeId((String)null);
        issueTypeScreenSchemeEntity.setFieldScreenScheme(fieldScreenScheme);
        issueTypeScreenScheme.addEntity(issueTypeScreenSchemeEntity);
        return issueTypeScreenScheme;
    }
    
    private void addFieldToScreen(final FieldScreen fieldScreen, final OrderableField field) {
        final AddFieldToScreenUtil addFieldToScreenUtil = (AddFieldToScreenUtil)ComponentManager.getInstance().loadComponent((Class)AddFieldToScreenUtilImpl.class, (Collection)Collections.emptyList());
        addFieldToScreenUtil.setFieldScreenId(fieldScreen.getId());
        addFieldToScreenUtil.setTabPosition(0);
        addFieldToScreenUtil.setFieldId(new String[] { field.getId() });
        addFieldToScreenUtil.setFieldPosition("");
        addFieldToScreenUtil.validate();
        addFieldToScreenUtil.execute();
    }
    
    private boolean isVisible(final CustomField cf, final ScreenInfo screenInfo, final List<CustomFieldInfo> generatedCustomFields) {
        for (final Integer customFieldIndex : screenInfo.getVisibleFields()) {
            if (generatedCustomFields.get(customFieldIndex).id == cf.getIdAsLong()) {
                return true;
            }
        }
        return false;
    }
    
    static {
        log = LoggerFactory.getLogger((Class)FieldConfigurationGenerator.class);
    }
}
