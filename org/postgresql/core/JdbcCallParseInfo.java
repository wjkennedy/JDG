// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

public class JdbcCallParseInfo
{
    private final String sql;
    private final boolean isFunction;
    
    public JdbcCallParseInfo(final String sql, final boolean isFunction) {
        this.sql = sql;
        this.isFunction = isFunction;
    }
    
    public String getSql() {
        return this.sql;
    }
    
    public boolean isFunction() {
        return this.isFunction;
    }
}
