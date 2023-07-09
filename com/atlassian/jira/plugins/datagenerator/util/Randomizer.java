// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.util;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import javax.annotation.Nullable;
import java.util.List;
import java.util.BitSet;
import java.util.Random;

public class Randomizer
{
    private static final Random RANDOM;
    
    private Randomizer() {
    }
    
    public static BitSet randomBitSet(final int m, final int n) {
        if (m < 0 || n < 0) {
            throw new IllegalArgumentException("m < 0 || n < 0: m=" + m + "; n=" + n);
        }
        final BitSet bits = new BitSet(n);
        if (m >= n) {
            bits.set(0, n);
            return bits;
        }
        for (int i = n - m; i < n; ++i) {
            final int pos = Randomizer.RANDOM.nextInt(i + 1);
            bits.set(bits.get(pos) ? i : pos);
        }
        return bits;
    }
    
    public static boolean probability(final float probability) {
        return Float.isNaN(probability) || Randomizer.RANDOM.nextFloat() < probability;
    }
    
    public static int randomIntInRange(final int minValue, final int maxValue) {
        if (minValue == maxValue) {
            return minValue;
        }
        if (minValue > maxValue) {
            throw new IllegalArgumentException("minValue:" + minValue + " > maxValue:" + maxValue);
        }
        final int range = maxValue - minValue + 1;
        return (range == 0) ? minValue : (minValue + Randomizer.RANDOM.nextInt(range));
    }
    
    public static int randomLimitedGaussian(final int mean) {
        final double normalDistrValue = Randomizer.RANDOM.nextGaussian();
        return (int)(mean + normalDistrValue * mean % mean);
    }
    
    public static int randomLimitedGaussian(final double mean) {
        final double normalDistrValue = Randomizer.RANDOM.nextGaussian();
        return (int)Math.max(0L, Math.round(mean + normalDistrValue * mean));
    }
    
    public static int randomInt(final int n) {
        return Randomizer.RANDOM.nextInt(n);
    }
    
    @Nullable
    public static <T> T randomItem(@Nullable final List<T> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        return source.get(Randomizer.RANDOM.nextInt(source.size()));
    }
    
    @Nullable
    public static <K, V> K randomKey(@Nullable final Map<K, V> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        final Object[] values = source.keySet().toArray();
        return (K)values[Randomizer.RANDOM.nextInt(values.length)];
    }
    
    public static <T> List<T> randomItems(final int m, final List<T> source) {
        if (m < 0) {
            throw new IllegalArgumentException("m < 0: " + m);
        }
        final int n = source.size();
        if (m >= n) {
            return new ArrayList<T>((Collection<? extends T>)source);
        }
        final List<T> list = new ArrayList<T>(m);
        final BitSet bits = randomBitSet(m, n);
        for (int i = bits.nextSetBit(0); i != -1; i = bits.nextSetBit(i + 1)) {
            list.add(source.get(i));
        }
        return list;
    }
    
    static {
        RANDOM = new Random();
    }
}
