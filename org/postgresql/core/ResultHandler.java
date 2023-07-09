// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.List;

public interface ResultHandler
{
    void handleResultRows(final Query p0, final Field[] p1, final List<Tuple> p2, final ResultCursor p3);
    
    void handleCommandStatus(final String p0, final long p1, final long p2);
    
    void handleWarning(final SQLWarning p0);
    
    void handleError(final SQLException p0);
    
    void handleCompletion() throws SQLException;
    
    void secureProgress();
    
    SQLException getException();
    
    SQLWarning getWarning();
}
