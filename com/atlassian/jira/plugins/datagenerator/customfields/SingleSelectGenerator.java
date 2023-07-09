// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.customfields;

import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import com.atlassian.jira.issue.customfields.option.Options;
import com.google.common.collect.Sets;
import java.util.Set;
import java.util.Iterator;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.plugins.datagenerator.util.Randomizer;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.plugins.datagenerator.CustomFieldInfo;
import com.atlassian.jira.plugins.datagenerator.IssueInfo;
import com.atlassian.jira.plugins.datagenerator.text.DictionaryTextGenerator;
import com.atlassian.jira.issue.customfields.option.Option;
import java.util.List;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.plugins.datagenerator.text.TextGenerator;
import com.atlassian.jira.plugins.datagenerator.config.OptionsConfiguration;

public class SingleSelectGenerator implements FieldValueGenerator
{
    protected final OptionsConfiguration config;
    protected final TextGenerator optionGenerator;
    protected CustomFieldType customFieldType;
    protected List<Option> availableOptions;
    protected boolean useStringValue;
    public static final Factory factory;
    
    public SingleSelectGenerator(final OptionsConfiguration config) {
        this.config = config;
        this.optionGenerator = new DictionaryTextGenerator(config.minOptionLength, config.maxOptionLength);
    }
    
    protected void init(final IssueInfo issue, final CustomFieldInfo customFieldInfo) {
        if (this.availableOptions != null) {
            return;
        }
        final CustomField customField = customFieldInfo.customField;
        this.customFieldType = customField.getCustomFieldType();
        this.availableOptions = (List<Option>)customField.getOptions((String)null, customField.getRelevantConfig((IssueContext)issue), (JiraContextNode)null);
        if (this.availableOptions != null && !this.availableOptions.isEmpty()) {
            final Option option = this.availableOptions.get(0);
            final Object value = customFieldInfo.customField.getCustomFieldType().getSingularObjectFromString(String.valueOf(option.getOptionId()));
            this.useStringValue = (value instanceof String);
        }
    }
    
    @Override
    public Object generate(final IssueInfo issue, final CustomFieldInfo customFieldInfo) {
        this.init(issue, customFieldInfo);
        final Option option = Randomizer.randomItem(this.availableOptions);
        return (option != null && this.useStringValue) ? option.getValue() : option;
    }
    
    @Override
    public Object convertToDbValue(final Object generatedValue) {
        return this.customFieldType.getStringFromSingularObject(generatedValue);
    }
    
    @Override
    public void generateOptions(final CustomFieldInfo customFieldInfo) {
        for (final FieldConfigScheme scheme : customFieldInfo.customField.getConfigurationSchemes()) {
            final Set<FieldConfig> fieldConfigs = scheme.getConfigsByConfig().keySet();
            for (final FieldConfig fieldConfig : fieldConfigs) {
                this.generateOptions(customFieldInfo, fieldConfig);
            }
        }
    }
    
    private void generateOptions(final CustomFieldInfo customFieldInfo, final FieldConfig fieldConfig) {
        final Options options = customFieldInfo.customField.getOptions((String)null, fieldConfig, (JiraContextNode)null);
        if (options.size() >= this.config.avgOptionsCount) {
            return;
        }
        final Set<String> used = Sets.newHashSet();
        for (final Object option : options) {
            used.add(((Option)option).getValue());
        }
        for (int i = options.size(); i < this.config.avgOptionsCount; ++i) {
            String optionStr;
            do {
                optionStr = this.optionGenerator.generateText();
            } while (!used.add(optionStr));
            final Option option2 = options.addOption((Option)null, optionStr);
            if (options.size() == i) {
                options.add((Object)option2);
            }
        }
    }
    
    @Override
    public String fieldType() {
        return "stringvalue";
    }
    
    static {
        factory = new Factory() {
            @Override
            public FieldValueGenerator create(final GeneratorContext generatorContext) {
                final OptionsConfiguration config = generatorContext.generatorConfiguration.customFields.singleSelect;
                return new SingleSelectGenerator(config);
            }
            
            @Override
            public boolean isEnabled(final GeneratorContext generatorContext) {
                return generatorContext.generatorConfiguration.customFields.singleSelect.enabled;
            }
        };
    }
}
