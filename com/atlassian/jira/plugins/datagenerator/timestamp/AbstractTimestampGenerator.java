// 
// Decompiled by Procyon v0.6.0
// 

package com.atlassian.jira.plugins.datagenerator.timestamp;

import java.sql.Timestamp;
import java.util.Iterator;

abstract class AbstractTimestampGenerator implements TimestampGenerator
{
    @Override
    public Iterator<Timestamp> iterator() {
        return this;
    }
    
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
