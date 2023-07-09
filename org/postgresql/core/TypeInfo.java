// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import java.util.Iterator;
import java.sql.SQLException;
import org.postgresql.util.PGobject;

public interface TypeInfo
{
    void addCoreType(final String p0, final Integer p1, final Integer p2, final String p3, final Integer p4);
    
    void addDataType(final String p0, final Class<? extends PGobject> p1) throws SQLException;
    
    int getSQLType(final int p0) throws SQLException;
    
    int getSQLType(final String p0) throws SQLException;
    
    int getPGType(final String p0) throws SQLException;
    
    String getPGType(final int p0) throws SQLException;
    
    int getPGArrayElement(final int p0) throws SQLException;
    
    int getPGArrayType(final String p0) throws SQLException;
    
    char getArrayDelimiter(final int p0) throws SQLException;
    
    Iterator<String> getPGTypeNamesWithSQLTypes();
    
    Iterator<Integer> getPGTypeOidsWithSQLTypes();
    
    Class<? extends PGobject> getPGobject(final String p0);
    
    String getJavaClass(final int p0) throws SQLException;
    
    String getTypeForAlias(final String p0);
    
    int getPrecision(final int p0, final int p1);
    
    int getScale(final int p0, final int p1);
    
    boolean isCaseSensitive(final int p0);
    
    boolean isSigned(final int p0);
    
    int getDisplaySize(final int p0, final int p1);
    
    int getMaximumPrecision(final int p0);
    
    boolean requiresQuoting(final int p0) throws SQLException;
    
    boolean requiresQuotingSqlType(final int p0) throws SQLException;
    
    int longOidToInt(final long p0) throws SQLException;
    
    long intOidToLong(final int p0);
}
