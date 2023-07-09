// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.replication.fluent;

import org.postgresql.replication.fluent.physical.ChainedPhysicalStreamBuilder;
import org.postgresql.replication.fluent.logical.ChainedLogicalStreamBuilder;

public interface ChainedStreamBuilder
{
    ChainedLogicalStreamBuilder logical();
    
    ChainedPhysicalStreamBuilder physical();
}
