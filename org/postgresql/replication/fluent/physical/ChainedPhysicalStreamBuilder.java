// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.replication.fluent.physical;

import java.sql.SQLException;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.fluent.ChainedCommonStreamBuilder;

public interface ChainedPhysicalStreamBuilder extends ChainedCommonStreamBuilder<ChainedPhysicalStreamBuilder>
{
    PGReplicationStream start() throws SQLException;
}
