// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.List;

public class ResultHandlerDelegate implements ResultHandler
{
    private final ResultHandler delegate;
    
    public ResultHandlerDelegate(final ResultHandler delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void handleResultRows(final Query fromQuery, final Field[] fields, final List<Tuple> tuples, final ResultCursor cursor) {
        if (this.delegate != null) {
            this.delegate.handleResultRows(fromQuery, fields, tuples, cursor);
        }
    }
    
    @Override
    public void handleCommandStatus(final String status, final long updateCount, final long insertOID) {
        if (this.delegate != null) {
            this.delegate.handleCommandStatus(status, updateCount, insertOID);
        }
    }
    
    @Override
    public void handleWarning(final SQLWarning warning) {
        if (this.delegate != null) {
            this.delegate.handleWarning(warning);
        }
    }
    
    @Override
    public void handleError(final SQLException error) {
        if (this.delegate != null) {
            this.delegate.handleError(error);
        }
    }
    
    @Override
    public void handleCompletion() throws SQLException {
        if (this.delegate != null) {
            this.delegate.handleCompletion();
        }
    }
    
    @Override
    public void secureProgress() {
        if (this.delegate != null) {
            this.delegate.secureProgress();
        }
    }
    
    @Override
    public SQLException getException() {
        if (this.delegate != null) {
            return this.delegate.getException();
        }
        return null;
    }
    
    @Override
    public SQLWarning getWarning() {
        if (this.delegate != null) {
            return this.delegate.getWarning();
        }
        return null;
    }
}
