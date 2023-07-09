// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.util;

import java.sql.SQLWarning;

public class PSQLWarning extends SQLWarning
{
    private final ServerErrorMessage serverError;
    
    public PSQLWarning(final ServerErrorMessage err) {
        super(err.toString(), err.getSQLState());
        this.serverError = err;
    }
    
    @Override
    public String getMessage() {
        return this.serverError.getMessage();
    }
    
    public ServerErrorMessage getServerErrorMessage() {
        return this.serverError;
    }
}
