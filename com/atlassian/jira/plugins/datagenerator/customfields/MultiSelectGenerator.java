// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.customfields;

import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.function.Function;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.plugins.datagenerator.util.Randomizer;
import java.util.Collections;
import com.atlassian.jira.plugins.datagenerator.CustomFieldInfo;
import com.atlassian.jira.plugins.datagenerator.IssueInfo;
import com.atlassian.jira.plugins.datagenerator.config.OptionsConfiguration;

public class MultiSelectGenerator extends SingleSelectGenerator
{
    public static final FieldValueGenerator.Factory factory;
    
    public MultiSelectGenerator(final OptionsConfiguration config) {
        super(config);
    }
    
    @Override
    public Object generate(final IssueInfo issue, final CustomFieldInfo customFieldInfo) {
        this.init(issue, customFieldInfo);
        if (this.availableOptions == null) {
            return null;
        }
        if (this.availableOptions.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        final int count = Randomizer.randomIntInRange(1, this.availableOptions.size());
        final List<Option> selectedOptions = Randomizer.randomItems(count, this.availableOptions);
        for (final Option option : selectedOptions) {
            if (option != null) {
                if (!this.useStringValue) {
                    break;
                }
                return selectedOptions.stream().map((Function<? super Object, ?>)Option::getValue).collect((Collector<? super Object, ?, List<? super Object>>)Collectors.toList());
            }
        }
        return selectedOptions;
    }
    
    @Override
    public Object convertToDbValue(final Object generatedValue) {
        if (generatedValue == null || this.useStringValue) {
            return generatedValue;
        }
        final Collection<Option> values = (Collection<Option>)generatedValue;
        return values.stream().map(input -> this.customFieldType.getStringFromSingularObject((Object)input)).collect((Collector<? super Object, ?, List<? super Object>>)Collectors.toList());
    }
    
    static {
        factory = new FieldValueGenerator.Factory() {
            @Override
            public FieldValueGenerator create(final GeneratorContext generatorContext) {
                final OptionsConfiguration config = generatorContext.generatorConfiguration.customFields.multiSelect;
                return new MultiSelectGenerator(config);
            }
            
            @Override
            public boolean isEnabled(final GeneratorContext generatorContext) {
                return generatorContext.generatorConfiguration.customFields.multiSelect.enabled;
            }
        };
    }
}
