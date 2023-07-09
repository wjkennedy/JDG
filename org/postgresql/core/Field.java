// 
// Decompiled by Procyon v0.6.0
// 

package org.postgresql.core;

import org.checkerframework.dataflow.qual.Pure;
import org.postgresql.jdbc.FieldMetadata;

public class Field
{
    public static final int TEXT_FORMAT = 0;
    public static final int BINARY_FORMAT = 1;
    private final int length;
    private final int oid;
    private final int mod;
    private String columnLabel;
    private int format;
    private final int tableOid;
    private final int positionInTable;
    private FieldMetadata metadata;
    private int sqlType;
    private String pgType;
    private static final String NOT_YET_LOADED;
    
    public Field(final String name, final int oid, final int length, final int mod) {
        this(name, oid, length, mod, 0, 0);
    }
    
    public Field(final String name, final int oid) {
        this(name, oid, 0, -1);
    }
    
    public Field(final String columnLabel, final int oid, final int length, final int mod, final int tableOid, final int positionInTable) {
        this.format = 0;
        this.pgType = Field.NOT_YET_LOADED;
        this.columnLabel = columnLabel;
        this.oid = oid;
        this.length = length;
        this.mod = mod;
        this.tableOid = tableOid;
        this.positionInTable = positionInTable;
        this.metadata = ((tableOid == 0) ? new FieldMetadata(columnLabel) : null);
    }
    
    @Pure
    public int getOID() {
        return this.oid;
    }
    
    public int getMod() {
        return this.mod;
    }
    
    public String getColumnLabel() {
        return this.columnLabel;
    }
    
    public int getLength() {
        return this.length;
    }
    
    public int getFormat() {
        return this.format;
    }
    
    public void setFormat(final int format) {
        this.format = format;
    }
    
    public int getTableOid() {
        return this.tableOid;
    }
    
    public int getPositionInTable() {
        return this.positionInTable;
    }
    
    public FieldMetadata getMetadata() {
        return this.metadata;
    }
    
    public void setMetadata(final FieldMetadata metadata) {
        this.metadata = metadata;
    }
    
    @Override
    public String toString() {
        return "Field(" + ((this.columnLabel != null) ? this.columnLabel : "") + "," + Oid.toString(this.oid) + "," + this.length + "," + ((this.format == 0) ? 'T' : 'B') + ")";
    }
    
    public void setSQLType(final int sqlType) {
        this.sqlType = sqlType;
    }
    
    public int getSQLType() {
        return this.sqlType;
    }
    
    public void setPGType(final String pgType) {
        this.pgType = pgType;
    }
    
    public String getPGType() {
        return this.pgType;
    }
    
    public boolean isTypeInitialized() {
        return this.pgType != Field.NOT_YET_LOADED;
    }
    
    public void upperCaseLabel() {
        this.columnLabel = this.columnLabel.toUpperCase();
    }
    
    static {
        NOT_YET_LOADED = new String("pgType is not yet loaded");
    }
}
