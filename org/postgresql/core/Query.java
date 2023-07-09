// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import java.util.Map;

public interface Query
{
    ParameterList createParameterList();
    
    String toString(final ParameterList p0);
    
    String getNativeSql();
    
    SqlCommand getSqlCommand();
    
    void close();
    
    boolean isStatementDescribed();
    
    boolean isEmpty();
    
    int getBatchSize();
    
    Map<String, Integer> getResultSetColumnNameIndexMap();
    
    Query[] getSubqueries();
}
