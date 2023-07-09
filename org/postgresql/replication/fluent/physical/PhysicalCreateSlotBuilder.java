// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.replication.fluent.physical;

import org.postgresql.replication.fluent.ChainedCommonCreateSlotBuilder;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.ReplicationType;
import org.postgresql.util.internal.Nullness;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import org.postgresql.replication.ReplicationSlotInfo;
import org.postgresql.core.BaseConnection;
import org.postgresql.replication.fluent.AbstractCreateSlotBuilder;

public class PhysicalCreateSlotBuilder extends AbstractCreateSlotBuilder<ChainedPhysicalCreateSlotBuilder> implements ChainedPhysicalCreateSlotBuilder
{
    public PhysicalCreateSlotBuilder(final BaseConnection connection) {
        super(connection);
    }
    
    @Override
    protected ChainedPhysicalCreateSlotBuilder self() {
        return this;
    }
    
    @Override
    public ReplicationSlotInfo make() throws SQLException {
        if (this.slotName == null || this.slotName.isEmpty()) {
            throw new IllegalArgumentException("Replication slotName can't be null");
        }
        final Statement statement = this.connection.createStatement();
        ResultSet result = null;
        ReplicationSlotInfo slotInfo = null;
        try {
            final String sql = String.format("CREATE_REPLICATION_SLOT %s %s PHYSICAL", this.slotName, this.temporaryOption ? "TEMPORARY" : "");
            statement.execute(sql);
            result = statement.getResultSet();
            if (result == null || !result.next()) {
                throw new PSQLException(GT.tr("{0} returned no results", new Object[0]), PSQLState.OBJECT_NOT_IN_STATE);
            }
            slotInfo = new ReplicationSlotInfo(Nullness.castNonNull(result.getString("slot_name")), ReplicationType.PHYSICAL, LogSequenceNumber.valueOf(Nullness.castNonNull(result.getString("consistent_point"))), result.getString("snapshot_name"), result.getString("output_plugin"));
        }
        finally {
            if (result != null) {
                result.close();
            }
            statement.close();
        }
        return slotInfo;
    }
}
