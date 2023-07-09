// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import java.sql.SQLWarning;

class PSQLWarningWrapper
{
    private final SQLWarning firstWarning;
    private SQLWarning lastWarning;
    
    PSQLWarningWrapper(final SQLWarning warning) {
        this.firstWarning = warning;
        this.lastWarning = warning;
    }
    
    void addWarning(final SQLWarning sqlWarning) {
        this.lastWarning.setNextWarning(sqlWarning);
        this.lastWarning = sqlWarning;
    }
    
    SQLWarning getFirstWarning() {
        return this.firstWarning;
    }
}
