// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.text;

import org.apache.commons.lang.StringUtils;
import com.atlassian.jira.plugins.datagenerator.util.Randomizer;

public class DictionaryTextGenerator implements TextGenerator
{
    private final float nonEmptyProbability;
    private final int sentenceMinLength;
    private final int sentenceMaxLength;
    
    public DictionaryTextGenerator(final int sentenceMinLength, final int sentenceMaxLength) {
        this(Float.NaN, sentenceMinLength, sentenceMaxLength);
    }
    
    public DictionaryTextGenerator(final float nonEmptyProbability, final int sentenceMinLength, final int sentenceMaxLength) {
        this.nonEmptyProbability = nonEmptyProbability;
        this.sentenceMinLength = sentenceMinLength;
        this.sentenceMaxLength = sentenceMaxLength;
    }
    
    @Override
    public String generateText() {
        return Randomizer.probability(this.nonEmptyProbability) ? this.generateText(Randomizer.randomIntInRange(this.sentenceMinLength, this.sentenceMaxLength)) : null;
    }
    
    @Override
    public String generateText(final int wordCount) {
        final StringBuilder sb = new StringBuilder(wordCount * 16).append(StringUtils.capitalize(this.getRandomWord()));
        for (int i = 1; i < wordCount; ++i) {
            sb.append(' ').append(this.getRandomWord());
        }
        return sb.toString();
    }
}
