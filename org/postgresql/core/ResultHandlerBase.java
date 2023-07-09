// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import org.postgresql.util.internal.Nullness;
import java.util.List;
import java.sql.SQLWarning;
import java.sql.SQLException;

public class ResultHandlerBase implements ResultHandler
{
    private SQLException firstException;
    private SQLException lastException;
    private SQLWarning firstWarning;
    private SQLWarning lastWarning;
    
    @Override
    public void handleResultRows(final Query fromQuery, final Field[] fields, final List<Tuple> tuples, final ResultCursor cursor) {
    }
    
    @Override
    public void handleCommandStatus(final String status, final long updateCount, final long insertOID) {
    }
    
    @Override
    public void secureProgress() {
    }
    
    @Override
    public void handleWarning(final SQLWarning warning) {
        if (this.firstWarning == null) {
            this.lastWarning = warning;
            this.firstWarning = warning;
            return;
        }
        final SQLWarning lastWarning = Nullness.castNonNull(this.lastWarning);
        lastWarning.setNextException(warning);
        this.lastWarning = warning;
    }
    
    @Override
    public void handleError(final SQLException error) {
        if (this.firstException == null) {
            this.lastException = error;
            this.firstException = error;
            return;
        }
        Nullness.castNonNull(this.lastException).setNextException(error);
        this.lastException = error;
    }
    
    @Override
    public void handleCompletion() throws SQLException {
        final SQLException firstException = this.firstException;
        if (firstException != null) {
            throw firstException;
        }
    }
    
    @Override
    public SQLException getException() {
        return this.firstException;
    }
    
    @Override
    public SQLWarning getWarning() {
        return this.firstWarning;
    }
}
