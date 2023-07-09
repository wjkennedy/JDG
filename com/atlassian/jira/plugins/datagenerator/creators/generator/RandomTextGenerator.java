// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.creators.generator;

import java.util.Random;

public class RandomTextGenerator
{
    private static final int ALPHABET = 26;
    private static final int DEFAULT_MIN = 10;
    private static final int DEFAULT_MAX = 50;
    final Random random;
    
    public RandomTextGenerator() {
        this.random = new Random();
    }
    
    public String generateWords(final int min, final int max) {
        final char[] word = new char[min + this.random.nextInt(max - min)];
        for (int j = 0; j < word.length; ++j) {
            word[j] = (char)(97 + this.random.nextInt(26));
        }
        return new String(word);
    }
    
    public String generateWords() {
        return this.generateWords(10, 50);
    }
}
