// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.replication.fluent;

import org.postgresql.replication.fluent.physical.PhysicalCreateSlotBuilder;
import org.postgresql.replication.fluent.physical.ChainedPhysicalCreateSlotBuilder;
import org.postgresql.replication.fluent.logical.LogicalCreateSlotBuilder;
import org.postgresql.replication.fluent.logical.ChainedLogicalCreateSlotBuilder;
import org.postgresql.core.BaseConnection;

public class ReplicationCreateSlotBuilder implements ChainedCreateReplicationSlotBuilder
{
    private final BaseConnection baseConnection;
    
    public ReplicationCreateSlotBuilder(final BaseConnection baseConnection) {
        this.baseConnection = baseConnection;
    }
    
    @Override
    public ChainedLogicalCreateSlotBuilder logical() {
        return new LogicalCreateSlotBuilder(this.baseConnection);
    }
    
    @Override
    public ChainedPhysicalCreateSlotBuilder physical() {
        return new PhysicalCreateSlotBuilder(this.baseConnection);
    }
}
