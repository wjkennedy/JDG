// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import org.checkerframework.dataflow.qual.Pure;
import java.sql.SQLException;

public class PSQLException extends SQLException
{
    private ServerErrorMessage serverError;
    
    @Pure
    public PSQLException(final String msg, final PSQLState state, final Throwable cause) {
        super(msg, (state == null) ? null : state.getState(), cause);
    }
    
    @Pure
    public PSQLException(final String msg, final PSQLState state) {
        super(msg, (state == null) ? null : state.getState());
    }
    
    @Pure
    public PSQLException(final ServerErrorMessage serverError) {
        this(serverError, true);
    }
    
    @Pure
    public PSQLException(final ServerErrorMessage serverError, final boolean detail) {
        super(detail ? serverError.toString() : serverError.getNonSensitiveErrorMessage(), serverError.getSQLState());
        this.serverError = serverError;
    }
    
    @Pure
    public ServerErrorMessage getServerErrorMessage() {
        return this.serverError;
    }
}
