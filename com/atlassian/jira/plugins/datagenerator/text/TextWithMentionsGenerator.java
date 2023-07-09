// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.text;

import java.util.Objects;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import com.atlassian.jira.plugins.datagenerator.util.Randomizer;
import javax.annotation.Nonnull;
import com.google.common.collect.ImmutableList;

public class TextWithMentionsGenerator implements TextGenerator
{
    private final ImmutableList<String> usernames;
    private final float mentionProbability;
    private final int maximumMentions;
    private final float nonEmptyProbability;
    private final int minLength;
    private final int maxLength;
    
    private TextWithMentionsGenerator(@Nonnull final ImmutableList<String> usernames, final float mentionProbability, final int maximumMentions, final float nonEmptyProbability, final int minLength, final int maxLength) {
        this.usernames = usernames;
        this.mentionProbability = mentionProbability;
        this.maximumMentions = maximumMentions;
        this.nonEmptyProbability = nonEmptyProbability;
        this.minLength = minLength;
        this.maxLength = maxLength;
    }
    
    @Override
    public String generateText() {
        return Randomizer.probability(this.nonEmptyProbability) ? this.generateText(Randomizer.randomIntInRange(this.minLength, this.maxLength)) : null;
    }
    
    @Nonnull
    @Override
    public String generateText(final int wordCount) {
        int mentions = 0;
        final StringBuilder sb = new StringBuilder(wordCount * 16).append(StringUtils.capitalize(this.getRandomWord()));
        for (int i = 1; i < wordCount; ++i) {
            if (mentions <= this.maximumMentions && Randomizer.probability(this.mentionProbability)) {
                sb.append(' ').append(this.getRandomMention());
                ++mentions;
            }
            else {
                sb.append(' ').append(this.getRandomWord());
            }
        }
        return sb.toString();
    }
    
    private String getRandomMention() {
        return "[~" + Randomizer.randomItem((List<String>)this.usernames) + "]";
    }
    
    public static final class Builder
    {
        private final ImmutableList<String> usernames;
        private float mentionProbability;
        private int maximumMentions;
        private float nonEmptyProbability;
        private int minLength;
        private int maxLength;
        
        private Builder(@Nonnull final List<String> usernames) {
            this.mentionProbability = 0.02f;
            this.maximumMentions = 5;
            this.nonEmptyProbability = Float.NaN;
            this.minLength = 5;
            this.maxLength = 50;
            this.usernames = (ImmutableList<String>)ImmutableList.copyOf((Collection)Objects.requireNonNull(usernames));
        }
        
        public static Builder withUsernames(final List<String> usernames) {
            return new Builder(usernames);
        }
        
        public Builder mentionProbability(final float mentionProbability) {
            this.mentionProbability = mentionProbability;
            return this;
        }
        
        public Builder maximumMentions(final int maximumMentions) {
            this.maximumMentions = maximumMentions;
            return this;
        }
        
        public Builder nonEmptyProbability(final float nonEmptyProbability) {
            this.nonEmptyProbability = nonEmptyProbability;
            return this;
        }
        
        public Builder minLength(final int minLength) {
            this.minLength = minLength;
            return this;
        }
        
        public Builder maxLength(final int maxLength) {
            this.maxLength = maxLength;
            return this;
        }
        
        public TextWithMentionsGenerator build() {
            return new TextWithMentionsGenerator(this.usernames, this.mentionProbability, this.maximumMentions, this.nonEmptyProbability, this.minLength, this.maxLength, null);
        }
    }
}
