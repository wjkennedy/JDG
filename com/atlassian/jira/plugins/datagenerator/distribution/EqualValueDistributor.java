// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.distribution;

import com.google.common.collect.Lists;
import com.google.common.base.Preconditions;
import java.util.List;

public class EqualValueDistributor implements Distributor
{
    @Override
    public List<Integer> distribute(final int elements, final int buckets) {
        Preconditions.checkArgument(elements > 0);
        Preconditions.checkArgument(buckets > 0);
        int elementsInBucket = 1;
        final List<Integer> distributionVector = Lists.newArrayList();
        if (buckets < elements) {
            elementsInBucket = elements / buckets;
        }
        for (int currentBucket = 0; currentBucket < buckets; ++currentBucket) {
            distributionVector.add(elementsInBucket);
        }
        return distributionVector;
    }
}
