// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import java.io.IOException;
import java.sql.SQLException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;
import org.postgresql.core.v3.ConnectionFactoryImpl;
import org.postgresql.PGProperty;
import java.util.Properties;
import org.postgresql.util.HostSpec;

public abstract class ConnectionFactory
{
    public static QueryExecutor openConnection(final HostSpec[] hostSpecs, final String user, final String database, final Properties info) throws SQLException {
        final String protoName = PGProperty.PROTOCOL_VERSION.get(info);
        if (protoName == null || protoName.isEmpty() || "3".equals(protoName)) {
            final ConnectionFactory connectionFactory = new ConnectionFactoryImpl();
            final QueryExecutor queryExecutor = connectionFactory.openConnectionImpl(hostSpecs, user, database, info);
            if (queryExecutor != null) {
                return queryExecutor;
            }
        }
        throw new PSQLException(GT.tr("A connection could not be made using the requested protocol {0}.", protoName), PSQLState.CONNECTION_UNABLE_TO_CONNECT);
    }
    
    public abstract QueryExecutor openConnectionImpl(final HostSpec[] p0, final String p1, final String p2, final Properties p3) throws SQLException;
    
    protected void closeStream(final PGStream newStream) {
        if (newStream != null) {
            try {
                newStream.close();
            }
            catch (final IOException ex) {}
        }
    }
}
