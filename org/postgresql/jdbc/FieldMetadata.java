// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.jdbc;

import org.postgresql.util.CanEstimateSize;

public class FieldMetadata implements CanEstimateSize
{
    final String columnName;
    final String tableName;
    final String schemaName;
    final int nullable;
    final boolean autoIncrement;
    
    public FieldMetadata(final String columnName) {
        this(columnName, "", "", 2, false);
    }
    
    FieldMetadata(final String columnName, final String tableName, final String schemaName, final int nullable, final boolean autoIncrement) {
        this.columnName = columnName;
        this.tableName = tableName;
        this.schemaName = schemaName;
        this.nullable = nullable;
        this.autoIncrement = autoIncrement;
    }
    
    @Override
    public long getSize() {
        return this.columnName.length() * 2 + this.tableName.length() * 2 + this.schemaName.length() * 2 + 4L + 1L;
    }
    
    @Override
    public String toString() {
        return "FieldMetadata{columnName='" + this.columnName + '\'' + ", tableName='" + this.tableName + '\'' + ", schemaName='" + this.schemaName + '\'' + ", nullable=" + this.nullable + ", autoIncrement=" + this.autoIncrement + '}';
    }
    
    public static class Key
    {
        final int tableOid;
        final int positionInTable;
        
        Key(final int tableOid, final int positionInTable) {
            this.positionInTable = positionInTable;
            this.tableOid = tableOid;
        }
        
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final Key key = (Key)o;
            return this.tableOid == key.tableOid && this.positionInTable == key.positionInTable;
        }
        
        @Override
        public int hashCode() {
            int result = this.tableOid;
            result = 31 * result + this.positionInTable;
            return result;
        }
        
        @Override
        public String toString() {
            return "Key{tableOid=" + this.tableOid + ", positionInTable=" + this.positionInTable + '}';
        }
    }
}
