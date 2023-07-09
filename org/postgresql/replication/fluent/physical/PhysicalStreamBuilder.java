// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.replication.fluent.physical;

import org.postgresql.replication.fluent.ChainedCommonStreamBuilder;
import org.postgresql.replication.LogSequenceNumber;
import java.sql.SQLException;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.fluent.AbstractStreamBuilder;

public class PhysicalStreamBuilder extends AbstractStreamBuilder<ChainedPhysicalStreamBuilder> implements ChainedPhysicalStreamBuilder, PhysicalReplicationOptions
{
    private final StartPhysicalReplicationCallback startCallback;
    
    public PhysicalStreamBuilder(final StartPhysicalReplicationCallback startCallback) {
        this.startCallback = startCallback;
    }
    
    @Override
    protected ChainedPhysicalStreamBuilder self() {
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
    public LogSequenceNumber getStartLSNPosition() {
        return this.startPosition;
    }
    
    @Override
    public int getStatusInterval() {
        return this.statusIntervalMs;
    }
}
