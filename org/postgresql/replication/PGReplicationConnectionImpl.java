// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.replication;

import java.sql.SQLException;
import java.sql.Statement;
import org.postgresql.replication.fluent.ReplicationCreateSlotBuilder;
import org.postgresql.replication.fluent.ChainedCreateReplicationSlotBuilder;
import org.postgresql.replication.fluent.ReplicationStreamBuilder;
import org.postgresql.replication.fluent.ChainedStreamBuilder;
import org.postgresql.core.BaseConnection;

public class PGReplicationConnectionImpl implements PGReplicationConnection
{
    private BaseConnection connection;
    
    public PGReplicationConnectionImpl(final BaseConnection connection) {
        this.connection = connection;
    }
    
    @Override
    public ChainedStreamBuilder replicationStream() {
        return new ReplicationStreamBuilder(this.connection);
    }
    
    @Override
    public ChainedCreateReplicationSlotBuilder createReplicationSlot() {
        return new ReplicationCreateSlotBuilder(this.connection);
    }
    
    @Override
    public void dropReplicationSlot(final String slotName) throws SQLException {
        if (slotName == null || slotName.isEmpty()) {
            throw new IllegalArgumentException("Replication slot name can't be null or empty");
        }
        final Statement statement = this.connection.createStatement();
        try {
            statement.execute("DROP_REPLICATION_SLOT " + slotName);
        }
        finally {
            statement.close();
        }
    }
}
