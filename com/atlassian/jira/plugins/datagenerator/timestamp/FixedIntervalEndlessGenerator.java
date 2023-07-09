// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.timestamp;

import java.util.Iterator;
import java.sql.Timestamp;

public class FixedIntervalEndlessGenerator extends AbstractTimestampGenerator
{
    final long interval;
    long currentTs;
    
    public FixedIntervalEndlessGenerator(final long startTs, final long interval) {
        this.interval = interval;
        this.currentTs = startTs;
    }
    
    @Override
    public boolean hasNext() {
        return true;
    }
    
    @Override
    public Timestamp next() {
        final Timestamp timestamp = new Timestamp(this.currentTs);
        this.currentTs += this.interval;
        return timestamp;
    }
}
