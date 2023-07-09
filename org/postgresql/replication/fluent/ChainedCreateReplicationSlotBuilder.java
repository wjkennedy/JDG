// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.replication.fluent;

import org.postgresql.replication.fluent.physical.ChainedPhysicalCreateSlotBuilder;
import org.postgresql.replication.fluent.logical.ChainedLogicalCreateSlotBuilder;

public interface ChainedCreateReplicationSlotBuilder
{
    ChainedLogicalCreateSlotBuilder logical();
    
    ChainedPhysicalCreateSlotBuilder physical();
}
