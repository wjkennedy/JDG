// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import org.postgresql.util.CanEstimateSize;

class BaseQueryKey implements CanEstimateSize
{
    public final String sql;
    public final boolean isParameterized;
    public final boolean escapeProcessing;
    
    BaseQueryKey(final String sql, final boolean isParameterized, final boolean escapeProcessing) {
        this.sql = sql;
        this.isParameterized = isParameterized;
        this.escapeProcessing = escapeProcessing;
    }
    
    @Override
    public String toString() {
        return "BaseQueryKey{sql='" + this.sql + '\'' + ", isParameterized=" + this.isParameterized + ", escapeProcessing=" + this.escapeProcessing + '}';
    }
    
    @Override
    public long getSize() {
        if (this.sql == null) {
            return 16L;
        }
        return 16L + this.sql.length() * 2L;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final BaseQueryKey that = (BaseQueryKey)o;
        return this.isParameterized == that.isParameterized && this.escapeProcessing == that.escapeProcessing && ((this.sql != null) ? this.sql.equals(that.sql) : (that.sql == null));
    }
    
    @Override
    public int hashCode() {
        int result = (this.sql != null) ? this.sql.hashCode() : 0;
        result = 31 * result + (this.isParameterized ? 1 : 0);
        result = 31 * result + (this.escapeProcessing ? 1 : 0);
        return result;
    }
}
