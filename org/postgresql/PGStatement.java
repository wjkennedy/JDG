// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql;

import java.sql.SQLException;

public interface PGStatement
{
    public static final long DATE_POSITIVE_INFINITY = 9223372036825200000L;
    public static final long DATE_NEGATIVE_INFINITY = -9223372036832400000L;
    public static final long DATE_POSITIVE_SMALLER_INFINITY = 185543533774800000L;
    public static final long DATE_NEGATIVE_SMALLER_INFINITY = -185543533774800000L;
    
    long getLastOID() throws SQLException;
    
    @Deprecated
    void setUseServerPrepare(final boolean p0) throws SQLException;
    
    boolean isUseServerPrepare();
    
    void setPrepareThreshold(final int p0) throws SQLException;
    
    int getPrepareThreshold();
}
