// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import org.postgresql.replication.fluent.physical.PhysicalReplicationOptions;
import java.sql.SQLException;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.fluent.logical.LogicalReplicationOptions;

public interface ReplicationProtocol
{
    PGReplicationStream startLogical(final LogicalReplicationOptions p0) throws SQLException;
    
    PGReplicationStream startPhysical(final PhysicalReplicationOptions p0) throws SQLException;
}
