// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql;

import java.sql.SQLException;

public interface PGResultSetMetaData
{
    String getBaseColumnName(final int p0) throws SQLException;
    
    String getBaseTableName(final int p0) throws SQLException;
    
    String getBaseSchemaName(final int p0) throws SQLException;
    
    int getFormat(final int p0) throws SQLException;
}
