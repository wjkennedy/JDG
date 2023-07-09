// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.text;

import javax.annotation.Nullable;
import com.atlassian.jira.plugins.datagenerator.util.Randomizer;
import com.atlassian.jira.plugins.datagenerator.config.Issues;
import java.util.List;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;
import java.io.IOException;
import java.util.Collection;
import org.apache.commons.io.IOUtils;
import com.google.common.collect.ImmutableList;

public interface TextGenerator
{
    public static final ImmutableList<String> words = readWords();
    
    default ImmutableList<String> readWords() {
        try {
            return (ImmutableList<String>)ImmutableList.copyOf((Collection)IOUtils.readLines(DictionaryTextGenerator.class.getResourceAsStream("words.txt"), "UTF-8"));
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    default TextGenerator getTextGenerator(final GeneratorConfiguration generatorConfiguration, final List<String> users) {
        final Issues issuesCfg = generatorConfiguration.issues;
        if (generatorConfiguration.includeUserMentions) {
            return TextWithMentionsGenerator.Builder.withUsernames(users).minLength(issuesCfg.commentMinLength).maxLength(issuesCfg.commentMaxLength).build();
        }
        return new DictionaryTextGenerator(issuesCfg.commentMinLength, issuesCfg.commentMaxLength);
    }
    
    default String getRandomWord() {
        return Randomizer.randomItem((List<String>)TextGenerator.words);
    }
    
    @Nullable
    String generateText();
    
    String generateText(final int p0);
}
