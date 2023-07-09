// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.List;
import java.sql.Statement;
import org.postgresql.PGStatement;

public interface BaseStatement extends PGStatement, Statement
{
    ResultSet createDriverResultSet(final Field[] p0, final List<Tuple> p1) throws SQLException;
    
    ResultSet createResultSet(final Query p0, final Field[] p1, final List<Tuple> p2, final ResultCursor p3) throws SQLException;
    
    boolean executeWithFlags(final String p0, final int p1) throws SQLException;
    
    boolean executeWithFlags(final CachedQuery p0, final int p1) throws SQLException;
    
    boolean executeWithFlags(final int p0) throws SQLException;
}
