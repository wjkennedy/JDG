// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core.v3.replication;

import java.io.IOException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import java.util.Iterator;
import java.util.Properties;
import org.postgresql.util.internal.Nullness;
import org.postgresql.copy.CopyDual;
import java.util.logging.Level;
import org.postgresql.replication.fluent.physical.PhysicalReplicationOptions;
import java.sql.SQLException;
import org.postgresql.replication.fluent.CommonOptions;
import org.postgresql.replication.ReplicationType;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.fluent.logical.LogicalReplicationOptions;
import org.postgresql.core.PGStream;
import org.postgresql.core.QueryExecutor;
import java.util.logging.Logger;
import org.postgresql.core.ReplicationProtocol;

public class V3ReplicationProtocol implements ReplicationProtocol
{
    private static final Logger LOGGER;
    private final QueryExecutor queryExecutor;
    private final PGStream pgStream;
    
    public V3ReplicationProtocol(final QueryExecutor queryExecutor, final PGStream pgStream) {
        this.queryExecutor = queryExecutor;
        this.pgStream = pgStream;
    }
    
    @Override
    public PGReplicationStream startLogical(final LogicalReplicationOptions options) throws SQLException {
        final String query = this.createStartLogicalQuery(options);
        return this.initializeReplication(query, options, ReplicationType.LOGICAL);
    }
    
    @Override
    public PGReplicationStream startPhysical(final PhysicalReplicationOptions options) throws SQLException {
        final String query = this.createStartPhysicalQuery(options);
        return this.initializeReplication(query, options, ReplicationType.PHYSICAL);
    }
    
    private PGReplicationStream initializeReplication(final String query, final CommonOptions options, final ReplicationType replicationType) throws SQLException {
        V3ReplicationProtocol.LOGGER.log(Level.FINEST, " FE=> StartReplication(query: {0})", query);
        this.configureSocketTimeout(options);
        final CopyDual copyDual = (CopyDual)this.queryExecutor.startCopy(query, true);
        return new V3PGReplicationStream(Nullness.castNonNull(copyDual), options.getStartLSNPosition(), options.getStatusInterval(), replicationType);
    }
    
    private String createStartPhysicalQuery(final PhysicalReplicationOptions options) {
        final StringBuilder builder = new StringBuilder();
        builder.append("START_REPLICATION");
        if (options.getSlotName() != null) {
            builder.append(" SLOT ").append(options.getSlotName());
        }
        builder.append(" PHYSICAL ").append(options.getStartLSNPosition().asString());
        return builder.toString();
    }
    
    private String createStartLogicalQuery(final LogicalReplicationOptions options) {
        final StringBuilder builder = new StringBuilder();
        builder.append("START_REPLICATION SLOT ").append(options.getSlotName()).append(" LOGICAL ").append(options.getStartLSNPosition().asString());
        final Properties slotOptions = options.getSlotOptions();
        if (slotOptions.isEmpty()) {
            return builder.toString();
        }
        builder.append(" (");
        boolean isFirst = true;
        for (final String name : slotOptions.stringPropertyNames()) {
            if (isFirst) {
                isFirst = false;
            }
            else {
                builder.append(", ");
            }
            builder.append('\"').append(name).append('\"').append(" ").append('\'').append(slotOptions.getProperty(name)).append('\'');
        }
        builder.append(")");
        return builder.toString();
    }
    
    private void configureSocketTimeout(final CommonOptions options) throws PSQLException {
        if (options.getStatusInterval() == 0) {
            return;
        }
        try {
            final int previousTimeOut = this.pgStream.getSocket().getSoTimeout();
            int minimalTimeOut;
            if (previousTimeOut > 0) {
                minimalTimeOut = Math.min(previousTimeOut, options.getStatusInterval());
            }
            else {
                minimalTimeOut = options.getStatusInterval();
            }
            this.pgStream.getSocket().setSoTimeout(minimalTimeOut);
            this.pgStream.setMinStreamAvailableCheckDelay(0);
        }
        catch (final IOException ioe) {
            throw new PSQLException(GT.tr("The connection attempt failed.", new Object[0]), PSQLState.CONNECTION_UNABLE_TO_CONNECT, ioe);
        }
    }
    
    static {
        LOGGER = Logger.getLogger(V3ReplicationProtocol.class.getName());
    }
}
