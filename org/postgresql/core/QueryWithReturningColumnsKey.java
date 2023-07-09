// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import java.util.Arrays;

class QueryWithReturningColumnsKey extends BaseQueryKey
{
    public final String[] columnNames;
    private int size;
    
    QueryWithReturningColumnsKey(final String sql, final boolean isParameterized, final boolean escapeProcessing, String[] columnNames) {
        super(sql, isParameterized, escapeProcessing);
        if (columnNames == null) {
            columnNames = new String[] { "*" };
        }
        this.columnNames = columnNames;
    }
    
    @Override
    public long getSize() {
        int size = this.size;
        if (size != 0) {
            return size;
        }
        size = (int)super.getSize();
        if (this.columnNames != null) {
            size += (int)16L;
            for (final String columnName : this.columnNames) {
                size += (int)(columnName.length() * 2L);
            }
        }
        this.size = size;
        return size;
    }
    
    @Override
    public String toString() {
        return "QueryWithReturningColumnsKey{sql='" + this.sql + '\'' + ", isParameterized=" + this.isParameterized + ", escapeProcessing=" + this.escapeProcessing + ", columnNames=" + Arrays.toString(this.columnNames) + '}';
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final QueryWithReturningColumnsKey that = (QueryWithReturningColumnsKey)o;
        return Arrays.equals(this.columnNames, that.columnNames);
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(this.columnNames);
        return result;
    }
}
