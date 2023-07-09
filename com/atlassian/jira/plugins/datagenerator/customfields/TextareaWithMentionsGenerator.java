// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.customfields;

import java.util.Collection;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import com.atlassian.jira.plugins.datagenerator.GeneratorContext;
import com.atlassian.jira.plugins.datagenerator.CustomFieldInfo;
import com.atlassian.jira.plugins.datagenerator.IssueInfo;
import com.atlassian.jira.plugins.datagenerator.text.TextWithMentionsGenerator;
import java.util.List;
import com.atlassian.jira.plugins.datagenerator.text.TextGenerator;

public class TextareaWithMentionsGenerator extends AbstractFieldValueGenerator
{
    private final TextGenerator textGenerator;
    public static final FieldValueGenerator.Factory factory;
    
    private TextareaWithMentionsGenerator(final List<String> usernames, final int minLength, final int maxLength) {
        this.textGenerator = TextWithMentionsGenerator.Builder.withUsernames(usernames).minLength(minLength).maxLength(maxLength).build();
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
                Collection<String> mentionables;
                if (generatorContext.reporters != null) {
                    mentionables = generatorContext.reporters.keySet();
                }
                else {
                    mentionables = Collections.singletonList(generatorContext.userName);
                }
                return new TextareaWithMentionsGenerator((List)ImmutableList.copyOf((Collection)mentionables), generatorContext.generatorConfiguration.customFields.freeText.minLength, generatorContext.generatorConfiguration.customFields.freeText.maxLength, null);
            }
            
            @Override
            public boolean isEnabled(final GeneratorContext generatorContext) {
                return false;
            }
        };
    }
}
