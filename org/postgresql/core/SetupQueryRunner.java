// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import java.sql.SQLWarning;
import java.sql.SQLException;
import java.util.List;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.GT;

public class SetupQueryRunner
{
    public static Tuple run(final QueryExecutor executor, final String queryString, final boolean wantResults) throws SQLException {
        final Query query = executor.createSimpleQuery(queryString);
        final SimpleResultHandler handler = new SimpleResultHandler();
        int flags = 1041;
        if (!wantResults) {
            flags |= 0x6;
        }
        try {
            executor.execute(query, null, handler, 0, 0, flags);
        }
        finally {
            query.close();
        }
        if (!wantResults) {
            return null;
        }
        final List<Tuple> tuples = handler.getResults();
        if (tuples == null || tuples.size() != 1) {
            throw new PSQLException(GT.tr("An unexpected result was returned by a query.", new Object[0]), PSQLState.CONNECTION_UNABLE_TO_CONNECT);
        }
        return tuples.get(0);
    }
    
    private static class SimpleResultHandler extends ResultHandlerBase
    {
        private List<Tuple> tuples;
        
        List<Tuple> getResults() {
            return this.tuples;
        }
        
        @Override
        public void handleResultRows(final Query fromQuery, final Field[] fields, final List<Tuple> tuples, final ResultCursor cursor) {
            this.tuples = tuples;
        }
        
        @Override
        public void handleWarning(final SQLWarning warning) {
        }
    }
}
