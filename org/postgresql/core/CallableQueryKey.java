// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

class CallableQueryKey extends BaseQueryKey
{
    CallableQueryKey(final String sql) {
        super(sql, true, true);
    }
    
    @Override
    public String toString() {
        return "CallableQueryKey{sql='" + this.sql + '\'' + ", isParameterized=" + this.isParameterized + ", escapeProcessing=" + this.escapeProcessing + '}';
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() * 31;
    }
    
    @Override
    public boolean equals(final Object o) {
        return super.equals(o);
    }
}
