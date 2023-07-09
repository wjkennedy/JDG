// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.distribution;

import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;

public class MaxPercentageDistributor implements Distributor
{
    public static final String MAX_PERCENTAGE_DISTRIBUTION = "max.percentage.distribution";
    private static final Logger log;
    private final Random random;
    private final double maxPercentageInBucket;
    private final int distributionTailTreshold;
    
    public MaxPercentageDistributor(final double maxPercentageInBucket, final int distributionTailTreshold) {
        this.random = new Random();
        if (maxPercentageInBucket <= 0.0 || maxPercentageInBucket > 100.0) {
            throw new IllegalArgumentException("Percentage of elements in one bucket must be in range of [0.0; 100.0]");
        }
        this.maxPercentageInBucket = maxPercentageInBucket;
        this.distributionTailTreshold = distributionTailTreshold;
    }
    
    @Override
    public List<Integer> distribute(final int elements, final int buckets) {
        Preconditions.checkArgument(elements > 0);
        Preconditions.checkArgument(buckets > 0);
        if (this.maxPercentageInBucket / 100.0 * elements * buckets < elements) {
            MaxPercentageDistributor.log.warn("Number of distributed elements will be significantly lower than expected. Set max percentage param to higher value or increase number of buckets.");
        }
        final List<Integer> distributionVector = new ArrayList<Integer>();
        int elementsToDistribute = elements;
        final int maxElementsInBucket = (int)(elementsToDistribute * (this.maxPercentageInBucket / 100.0));
        for (double currentBucket = 0.0; currentBucket < buckets; ++currentBucket) {
            if (elementsToDistribute < this.distributionTailTreshold) {
                distributionVector.add(this.random.nextInt(this.distributionTailTreshold) + 1);
            }
            else {
                final int elementsInBucket = maxElementsInBucket - this.variance(currentBucket, maxElementsInBucket);
                distributionVector.add(this.normalize(elementsInBucket, maxElementsInBucket));
                elementsToDistribute -= elementsInBucket;
            }
        }
        return distributionVector;
    }
    
    private int variance(final double currentBucket, final double maxElementsInBucket) {
        return (int)(maxElementsInBucket * this.random.nextDouble() + currentBucket * this.random.nextGaussian() * this.random.nextDouble());
    }
    
    private int normalize(final int elementsInBucket, final int maxElementsInBucket) {
        if (elementsInBucket < 1) {
            return 1;
        }
        if (elementsInBucket > maxElementsInBucket) {
            return maxElementsInBucket;
        }
        return elementsInBucket;
    }
    
    static {
        log = LoggerFactory.getLogger((Class)MaxPercentageDistributor.class);
    }
}
