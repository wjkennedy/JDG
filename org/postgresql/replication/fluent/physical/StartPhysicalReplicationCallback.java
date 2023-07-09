// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.replication.fluent.physical;

import java.sql.SQLException;
import org.postgresql.replication.PGReplicationStream;

public interface StartPhysicalReplicationCallback
{
    PGReplicationStream start(final PhysicalReplicationOptions p0) throws SQLException;
}
