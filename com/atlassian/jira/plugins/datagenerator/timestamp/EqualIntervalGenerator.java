// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.timestamp;

import java.util.Iterator;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;
import com.atlassian.jira.plugins.datagenerator.config.GeneratorConfiguration;

public class EqualIntervalGenerator extends AbstractTimestampGenerator
{
    final int eventCount;
    int count;
    final long startTs;
    final long endTs;
    
    public EqualIntervalGenerator(final GeneratorConfiguration configuration, final int issueCount) {
        this.eventCount = issueCount;
        this.endTs = System.currentTimeMillis();
        this.startTs = this.endTs - TimeUnit.DAYS.toMillis(configuration.period);
    }
    
    @Override
    public boolean hasNext() {
        return this.count < this.eventCount;
    }
    
    @Override
    public Timestamp next() {
        return new Timestamp(this.startTs + (this.endTs - this.startTs) * this.count++ / this.eventCount);
    }
}
