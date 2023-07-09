// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.replication.fluent.logical;

import org.postgresql.replication.fluent.ChainedCommonStreamBuilder;
import java.util.Iterator;
import org.postgresql.util.internal.Nullness;
import org.postgresql.replication.LogSequenceNumber;
import java.sql.SQLException;
import org.postgresql.replication.PGReplicationStream;
import java.util.Properties;
import org.postgresql.replication.fluent.AbstractStreamBuilder;

public class LogicalStreamBuilder extends AbstractStreamBuilder<ChainedLogicalStreamBuilder> implements ChainedLogicalStreamBuilder, LogicalReplicationOptions
{
    private final Properties slotOptions;
    private StartLogicalReplicationCallback startCallback;
    
    public LogicalStreamBuilder(final StartLogicalReplicationCallback startCallback) {
        this.startCallback = startCallback;
        this.slotOptions = new Properties();
    }
    
    @Override
    protected ChainedLogicalStreamBuilder self() {
        return this;
    }
    
    @Override
    public PGReplicationStream start() throws SQLException {
        return this.startCallback.start(this);
    }
    
    @Override
    public String getSlotName() {
        return this.slotName;
    }
    
    @Override
    public ChainedLogicalStreamBuilder withStartPosition(final LogSequenceNumber lsn) {
        this.startPosition = lsn;
        return this;
    }
    
    @Override
    public ChainedLogicalStreamBuilder withSlotOption(final String optionName, final boolean optionValue) {
        this.slotOptions.setProperty(optionName, String.valueOf(optionValue));
        return this;
    }
    
    @Override
    public ChainedLogicalStreamBuilder withSlotOption(final String optionName, final int optionValue) {
        this.slotOptions.setProperty(optionName, String.valueOf(optionValue));
        return this;
    }
    
    @Override
    public ChainedLogicalStreamBuilder withSlotOption(final String optionName, final String optionValue) {
        this.slotOptions.setProperty(optionName, optionValue);
        return this;
    }
    
    @Override
    public ChainedLogicalStreamBuilder withSlotOptions(final Properties options) {
        for (final String propertyName : options.stringPropertyNames()) {
            this.slotOptions.setProperty(propertyName, Nullness.castNonNull(options.getProperty(propertyName)));
        }
        return this;
    }
    
    @Override
    public LogSequenceNumber getStartLSNPosition() {
        return this.startPosition;
    }
    
    @Override
    public Properties getSlotOptions() {
        return this.slotOptions;
    }
    
    @Override
    public int getStatusInterval() {
        return this.statusIntervalMs;
    }
}
