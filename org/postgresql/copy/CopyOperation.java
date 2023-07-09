// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.copy;

import java.sql.SQLException;

public interface CopyOperation
{
    int getFieldCount();
    
    int getFormat();
    
    int getFieldFormat(final int p0);
    
    boolean isActive();
    
    void cancelCopy() throws SQLException;
    
    long getHandledRowCount();
}
