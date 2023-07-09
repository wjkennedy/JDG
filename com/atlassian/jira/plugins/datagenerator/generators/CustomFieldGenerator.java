// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.generators;

import com.atlassian.jira.issue.context.ProjectContext;
import com.atlassian.jira.project.Project;
import java.util.ArrayList;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.plugins.datagenerator.customfields.MultiUserPickerGenerator;
import com.atlassian.jira.plugins.datagenerator.customfields.UserPickerGenerator;
import com.atlassian.jira.plugins.datagenerator.customfields.NumericGenerator;
import com.atlassian.jira.plugins.datagenerator.customfields.MultiSelectGenerator;
import com.atlassian.jira.plugins.datagenerator.customfields.SingleSelectGenerator;
import com.atlassian.jira.plugins.datagenerator.customfields.DateTimeGenerator;
import com.atlassian.jira.plugins.datagenerator.customfields.TextareaGenerator;
import com.atlassian.jira.plugins.datagenerator.customfields.TextareaWithMentionsGenerator;
import org.slf4j.LoggerFactory;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.google.common.collect.ImmutableList;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.Collection;
import com.atlassian.jira.plugins.datagenerator.customfields.FieldValueGenerator;
import com.atlassian.jira.issue.fields.CustomField;
import org.ofbiz.core.entity.GenericEntityException;
import java.util.Iterator;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayout;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.plugins.datagenerator.util.Randomizer;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.google.common.collect.Lists;
import org.apache.commons.lang.time.StopWatch;
import java.util.Collections;
import com.atlassian.jira.plugins.datagenerator.CustomFieldInfo;
import com.atlassian.jira.plugins.datagenerator.FieldScreenDistribution;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.jira.plugins.datagenerator.text.DictionaryTextGenerator;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.plugins.datagenerator.text.TextGenerator;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.CustomFieldManager;
import java.util.List;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class CustomFieldGenerator
{
    private static final Logger LOG;
    private static final FieldSpec FREE_TEXT_WITH_MENTIONS;
    private static final List<FieldSpec> SUPPORTED_FIELDS;
    private final CustomFieldManager customFieldManager;
    private final FieldManager fieldManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final TextGenerator nameGenerator;
    private final ProjectManager projectManager;
    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    
    @Autowired
    public CustomFieldGenerator(@ComponentImport final CustomFieldManager customFieldManager, @ComponentImport final FieldManager fieldManager, @ComponentImport final FieldLayoutManager fieldLayoutManager, @ComponentImport final ProjectManager projectManager, @ComponentImport final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager) {
        this.nameGenerator = new DictionaryTextGenerator(1, 3);
        this.customFieldManager = customFieldManager;
        this.fieldManager = fieldManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.projectManager = projectManager;
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
    }
    
    public List<CustomFieldInfo> generate(final GeneratorContext context, final FieldScreenDistribution fieldScreenDistribution) throws GenericEntityException {
        final int fieldCount = context.generatorConfiguration.customFields.fieldCount;
        if (fieldCount <= 0) {
            CustomFieldGenerator.LOG.info("No custom fields configured to be created, skipping.");
            return Collections.emptyList();
        }
        final List<FieldSpec> enabledFields = this.enabledFieldSpecs(context);
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        context.resetProgress(String.format("Generating custom fields (%d in total)", fieldCount), fieldCount);
        final List<CustomFieldInfo> generatedFields = Lists.newArrayListWithCapacity(fieldCount);
        context.createdCustomFields = generatedFields;
        final float wikiRendererProbability = context.generatorConfiguration.customFields.freeText.wikiRendererProbability;
        final IssueTypeScreenSchemeEntity defaultScreen = this.issueTypeScreenSchemeManager.getDefaultScheme().getEntity((String)null);
        final FieldScreenScheme defaultScreenScheme = defaultScreen.getFieldScreenScheme();
        final FieldScreenTab defaultestTab = defaultScreenScheme.getFieldScreen((IssueOperation)null).getTab(0);
        final ContextFactory contextFactory = this.getContextFactory(context, fieldScreenDistribution);
        for (int i = 0; i < fieldCount; ++i) {
            final String customFieldName = this.nameGenerator.generateText();
            final FieldSpec field = enabledFields.get(i % enabledFields.size());
            final String description = "Auto generated custom field: " + field.type;
            final List<JiraContextNode> contexts = contextFactory.getContextsForCustomField(i);
            final CustomFieldInfo cfi = this.createCustomField(customFieldName, field.type, field.searcher, description, contexts);
            CustomFieldGenerator.LOG.info("Created custom field '{}' of type '{}'", (Object)customFieldName, (Object)field.type);
            if (context.generatorConfiguration.customFields.screenSchemes == 0) {
                defaultestTab.addFieldScreenLayoutItem(cfi.idStr);
            }
            if (CustomFieldGenerator.FREE_TEXT_WITH_MENTIONS.type.equals(field.type) && Randomizer.probability(wikiRendererProbability)) {
                final EditableDefaultFieldLayout defaultFieldLayout = this.fieldLayoutManager.getEditableDefaultFieldLayout();
                final FieldLayoutItem fieldLayoutItem = defaultFieldLayout.getFieldLayoutItem(cfi.idStr);
                defaultFieldLayout.setRendererType(fieldLayoutItem, "atlassian-wiki-renderer");
                cfi.valueGenerator = CustomFieldGenerator.FREE_TEXT_WITH_MENTIONS.generatorFactory.create(context);
                this.fieldLayoutManager.storeEditableDefaultFieldLayout(defaultFieldLayout);
            }
            else {
                cfi.valueGenerator = field.generatorFactory.create(context);
            }
            generatedFields.add(cfi);
            context.incProgress();
        }
        final StopWatch refreshStopWatch = new StopWatch();
        refreshStopWatch.start();
        this.customFieldManager.refresh();
        this.fieldManager.refresh();
        refreshStopWatch.stop();
        for (final CustomFieldInfo fieldInfo : generatedFields) {
            fieldInfo.valueGenerator.generateOptions(fieldInfo);
        }
        stopWatch.stop();
        final String message = String.format("Generated %d custom fields in %s, Field Managers refresh took %s.", fieldCount, stopWatch.toString(), refreshStopWatch.toString());
        context.messages.add(message);
        CustomFieldGenerator.LOG.info(message);
        return generatedFields;
    }
    
    public FieldValueGenerator.Factory getFieldValueGeneratorFactory(final CustomField customField) {
        final String type = customField.getCustomFieldType().getKey();
        if (CustomFieldGenerator.FREE_TEXT_WITH_MENTIONS.type.equals(type)) {
            final String rendererType = this.fieldLayoutManager.getEditableDefaultFieldLayout().getFieldLayoutItem(customField.getId()).getRendererType();
            if ("atlassian-wiki-renderer".equals(rendererType)) {
                return CustomFieldGenerator.FREE_TEXT_WITH_MENTIONS.generatorFactory;
            }
        }
        for (final FieldSpec fieldSpec : CustomFieldGenerator.SUPPORTED_FIELDS) {
            if (fieldSpec.type.equals(type)) {
                return fieldSpec.generatorFactory;
            }
        }
        return null;
    }
    
    private List<FieldSpec> enabledFieldSpecs(final GeneratorContext context) {
        return (List<FieldSpec>)ImmutableList.copyOf((Collection)CustomFieldGenerator.SUPPORTED_FIELDS.stream().filter(input -> input.generatorFactory.isEnabled(context)).collect((Collector<? super Object, ?, List<? super Object>>)Collectors.toList()));
    }
    
    private CustomFieldInfo createCustomField(final String fieldName, final String fieldType, final String searcher, final String description, final List<JiraContextNode> contexts) throws GenericEntityException {
        final CustomFieldType customFieldType = this.customFieldManager.getCustomFieldType(fieldType);
        final CustomFieldSearcher customFieldSearcher = this.customFieldManager.getCustomFieldSearcher(searcher);
        final CustomField customField = this.customFieldManager.createCustomField(fieldName, description, customFieldType, customFieldSearcher, (List)contexts, FieldConfigSchemeManager.ALL_ISSUE_TYPES);
        return new CustomFieldInfo(customField);
    }
    
    private ContextFactory getContextFactory(final GeneratorContext context, final FieldScreenDistribution fieldScreenDistribution) {
        if (context.generatorConfiguration.customFields.useGlobalCustomFieldContext) {
            return new GlobalContextFactory();
        }
        return new MappedContextFactory(fieldScreenDistribution, this.projectManager);
    }
    
    static {
        LOG = LoggerFactory.getLogger((Class)CustomFieldGenerator.class);
        FREE_TEXT_WITH_MENTIONS = new FieldSpec("textarea", "textsearcher", TextareaWithMentionsGenerator.factory);
        SUPPORTED_FIELDS = Lists.newArrayList((Object[])new FieldSpec[] { new FieldSpec("textarea", "textsearcher", TextareaGenerator.factory), new FieldSpec("datetime", "datetimerange", DateTimeGenerator.factory), new FieldSpec("select", "multiselectsearcher", SingleSelectGenerator.factory), new FieldSpec("multiselect", "multiselectsearcher", MultiSelectGenerator.factory), new FieldSpec("float", "numberrange", NumericGenerator.factory), new FieldSpec("userpicker", "userpickergroupsearcher", UserPickerGenerator.factory), new FieldSpec("multiuserpicker", "userpickergroupsearcher", MultiUserPickerGenerator.factory) });
    }
    
    private static class FieldSpec
    {
        final String type;
        final String searcher;
        final FieldValueGenerator.Factory generatorFactory;
        
        private FieldSpec(final String type, final String searcher, final FieldValueGenerator.Factory generatorFactory) {
            this.type = "com.atlassian.jira.plugin.system.customfieldtypes:" + type;
            this.searcher = "com.atlassian.jira.plugin.system.customfieldtypes:" + searcher;
            this.generatorFactory = generatorFactory;
        }
    }
    
    class GlobalContextFactory implements ContextFactory
    {
        final List<JiraContextNode> globalContext;
        
        GlobalContextFactory() {
            this.globalContext = Collections.singletonList(GlobalIssueContext.getInstance());
        }
        
        @Override
        public List<JiraContextNode> getContextsForCustomField(final int customFieldIndex) {
            return this.globalContext;
        }
    }
    
    static class MappedContextFactory implements ContextFactory
    {
        private final FieldScreenDistribution fieldScreenDistribution;
        private final ProjectManager projectManager;
        
        MappedContextFactory(final FieldScreenDistribution fieldScreenDistribution, final ProjectManager projectManager) {
            this.fieldScreenDistribution = fieldScreenDistribution;
            this.projectManager = projectManager;
        }
        
        @Override
        public List<JiraContextNode> getContextsForCustomField(final int customFieldIndex) {
            final List<Project> projects = this.fieldScreenDistribution.getProjectsForCustomField(customFieldIndex);
            final List<JiraContextNode> projectContexts = new ArrayList<JiraContextNode>(projects.size());
            for (final Project project : projects) {
                projectContexts.add((JiraContextNode)new ProjectContext(project.getId(), this.projectManager));
            }
            return projectContexts;
        }
    }
    
    interface ContextFactory
    {
        List<JiraContextNode> getContextsForCustomField(final int p0);
    }
}
