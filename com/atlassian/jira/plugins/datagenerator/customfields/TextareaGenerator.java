// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.customfields;

import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import com.atlassian.jira.plugins.datagenerator.CustomFieldInfo;
import com.atlassian.jira.plugins.datagenerator.IssueInfo;
import com.atlassian.jira.plugins.datagenerator.text.DictionaryTextGenerator;
import com.atlassian.jira.plugins.datagenerator.text.TextGenerator;

public class TextareaGenerator extends AbstractFieldValueGenerator
{
    private final TextGenerator textGenerator;
    public static final FieldValueGenerator.Factory factory;
    
    public TextareaGenerator(final int minLength, final int maxLength) {
        this.textGenerator = new DictionaryTextGenerator(minLength, maxLength);
    }
    
    @Override
    public String generate(final IssueInfo issue, final CustomFieldInfo customFieldInfo) {
        return this.textGenerator.generateText();
    }
    
    @Override
    public String fieldType() {
        return "textvalue";
    }
    
    static {
        factory = new FieldValueGenerator.Factory() {
            @Override
            public FieldValueGenerator create(final GeneratorContext generatorContext) {
                return new TextareaGenerator(generatorContext.generatorConfiguration.customFields.freeText.minLength, generatorContext.generatorConfiguration.customFields.freeText.maxLength);
            }
            
            @Override
            public boolean isEnabled(final GeneratorContext generatorContext) {
                return generatorContext.generatorConfiguration.customFields.freeText.enabled;
            }
        };
    }
}
