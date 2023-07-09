// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.replication.fluent;

import java.sql.SQLException;
import org.postgresql.replication.ReplicationSlotInfo;
import java.sql.SQLFeatureNotSupportedException;

public interface ChainedCommonCreateSlotBuilder<T extends ChainedCommonCreateSlotBuilder<T>>
{
    T withSlotName(final String p0);
    
    T withTemporaryOption() throws SQLFeatureNotSupportedException;
    
    ReplicationSlotInfo make() throws SQLException;
}
