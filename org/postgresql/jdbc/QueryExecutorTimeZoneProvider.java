// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import org.postgresql.core.QueryExecutor;
import java.util.TimeZone;
import org.postgresql.core.Provider;

class QueryExecutorTimeZoneProvider implements Provider<TimeZone>
{
    private final QueryExecutor queryExecutor;
    
    QueryExecutorTimeZoneProvider(final QueryExecutor queryExecutor) {
        this.queryExecutor = queryExecutor;
    }
    
    @Override
    public TimeZone get() {
        return this.queryExecutor.getTimeZone();
    }
}
