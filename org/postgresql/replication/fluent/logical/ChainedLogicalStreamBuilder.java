// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.replication.fluent.logical;

import java.util.Properties;
import java.sql.SQLException;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.fluent.ChainedCommonStreamBuilder;

public interface ChainedLogicalStreamBuilder extends ChainedCommonStreamBuilder<ChainedLogicalStreamBuilder>
{
    PGReplicationStream start() throws SQLException;
    
    ChainedLogicalStreamBuilder withSlotOption(final String p0, final boolean p1);
    
    ChainedLogicalStreamBuilder withSlotOption(final String p0, final int p1);
    
    ChainedLogicalStreamBuilder withSlotOption(final String p0, final String p1);
    
    ChainedLogicalStreamBuilder withSlotOptions(final Properties p0);
}
